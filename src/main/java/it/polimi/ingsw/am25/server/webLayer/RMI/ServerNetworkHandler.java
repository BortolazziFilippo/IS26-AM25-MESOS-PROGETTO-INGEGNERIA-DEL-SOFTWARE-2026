package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Controller.Controller;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RMI server-side entry point for all Mesos client actions. Manages the game lobby,
 * registers client stubs, and delegates game logic to the {@link Controller}.
 * Also used as the target for Socket clients via {@link it.polimi.ingsw.am25.server.webLayer.Socket.SocketClientHandler}.
 */
public class ServerNetworkHandler extends UnicastRemoteObject implements ServerRemoteInterface {
    private static final String LOG_PREFIX = "[SERVER][NETWORK]";
    private final List<ServerVirtualView> waitingPlayers = new ArrayList<>();
    private final List<PlayerDTO> playerDTOS=new ArrayList<>();
    private Controller controller;
    private int requiredPlayers = 0;
    private boolean isGameStarted = false;
    /**
     * Initializes the RMI network handler and exports it as a remote object,
     * making it reachable by Mesos clients via the RMI registry.
     * @throws RemoteException if the RMI export fails.
     */
    public ServerNetworkHandler() throws RemoteException{
        super();
    }

    /**
     * Creates a new Mesos game lobby hosted by the given player.
     * Registers the host's RMI stub so the server can push game events (board updates,
     * phase changes, market refreshes, etc.) back to them during the game.
     *
     * @param playerHost            the host's data (nickname and totem color).
     * @param playerNumber          the number of players required to start the game (2–5).
     * @param clientRemoteInterface the host's RMI stub, saved to notify them of game events.
     * @throws IllegalStateException if a lobby is already open on this server.
     */
    @Override
    public synchronized void createGame(PlayerDTO playerHost, int playerNumber,ClientRemoteInterface clientRemoteInterface) throws RemoteException, IllegalStateException {
        if (requiredPlayers > 0) {
            throw new IllegalStateException("Game already started");
        }
        this.requiredPlayers = playerNumber;
        // Create the VirtualView for the host and bind their remote interface.
        ServerVirtualView hostView = new ServerVirtualView(clientRemoteInterface, playerHost.getNickName());
        waitingPlayers.add(hostView);
        playerDTOS.add(playerHost);

        logServerEvent("Game created by '" + playerHost.getNickName() + "' for " + playerNumber + " players");
    }

    /**
     * Adds a player to the existing Mesos lobby and registers their RMI stub.
     * If this player fills the last required slot, the game starts automatically.
     *
     * @param playerDTO             the joining player's data (nickname and totem color).
     * @param clientRemoteInterface the player's RMI stub, saved to notify them of game events.
     * @throws GameFullException               if no lobby exists yet (no host has called {@link #createGame}).
     * @throws GameStartedException            if the game is already running.
     * @throws NameOrColorAlreadyTakenException if the chosen nickname or totem color is already taken by another player.
     */
    @Override
    public synchronized void addPlayer(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameFullException,GameReadyToStartException,NameOrColorAlreadyTakenException{
        if (requiredPlayers == 0) {
            throw new GameFullException("Nessuna partita creata!");
        }
        if (isGameStarted) {
            throw new GameStartedException("Partita già in corso!");
        }
        if (playerDTOS.stream().anyMatch(player -> Objects.equals(player.getNickName(), playerDTO.getNickName()) || player.getColorTotem() == playerDTO.getColorTotem())) {
            throw new NameOrColorAlreadyTakenException("Errore: Nickname o colore del Totem già in uso!");
        }

        // Create the VirtualView for the new player and bind their remote interface.
        ServerVirtualView playerView = new ServerVirtualView(clientRemoteInterface, playerDTO.getNickName());
        waitingPlayers.add(playerView);
        playerDTOS.add(playerDTO);
        logServerEvent("Player '" + playerDTO.getNickName() + "' joined (" + waitingPlayers.size() + "/" + requiredPlayers + ")");

        // All players are in — start the game!
        if (waitingPlayers.size() == requiredPlayers) {
            isGameStarted = true;
            setupAndStartGame();
        }
    }
    /**
     * Bootstraps the Mesos game once all players have joined.
     * Instantiates the {@link Controller}, builds {@link Player} objects for every participant,
     * registers all {@link ServerVirtualView}s as observers, pushes the initial board/market/player
     * state to every client, and kicks off the first placing phase.
     * Called automatically when the lobby reaches {@code requiredPlayers}.
     */
    private void setupAndStartGame() {
        logServerEvent("All players ready. Setting up the game...");
        Controller controller = new Controller();

        // CRUCIAL SAVE: We save the controller so other RMI methods
        // (like selectCard, placingPlayer, etc.) can use it to perform actions!
        this.controller = controller;

        // 1. Extract the Host data (they are always at position 0)
        PlayerDTO hostDTO = playerDTOS.getFirst();
        ServerVirtualView hostView = waitingPlayers.getFirst();

        // 2. Create the Host player and initialize the game in the Model
        Player playerHost = new Player(hostDTO.getNickName(), hostDTO.getColorTotem(), hostView);
        controller.createGame(playerHost, requiredPlayers);

        // 3. Link ALL VirtualViews (including the Host!) to the Controller's global observers
        waitingPlayers.forEach(controller::linkObserver);

        // 4. Add the other players.
        // We use a classic "for" loop STARTING FROM 1, so we skip the Host (who is at index 0)
        for (int i = 1; i < playerDTOS.size(); i++) {
            PlayerDTO currentDTO = playerDTOS.get(i);
            ServerVirtualView currentView = waitingPlayers.get(i);

            // Create and add the new player by pairing their DTO with their View
            Player newPlayer = new Player(currentDTO.getNickName(), currentDTO.getColorTotem(), currentView);
            controller.addPlayer(newPlayer);
        }

        // ----------------------------------------------------------------
        // 5. CROSS-REGISTER: every view observes every player's tribe changes.
        //    By default each Player only notifies its own ServerVirtualView, so
        //    other clients would never receive addedCardToTribe events for cards
        //    drawn by other players. After this call every draw is broadcast to
        //    all connected clients.
        // ----------------------------------------------------------------
        controller.crossRegisterPlayerObservers(waitingPlayers);

        logServerEvent("All players added to the model. Synchronizing initial state to clients...");
        // ----------------------------------------------------------------
        // 6. MASS INITIAL SYNCHRONIZATION ON CLIENTS
        // ----------------------------------------------------------------
        for (ServerVirtualView view : waitingPlayers) {
            // Pass the complete list of PlayerDTOs to EVERY VirtualView
            view.forceInitialPlayersSync(this.playerDTOS);
        }
        logServerEvent("All clients synced. The game is ready!");
        controller.controllerGameStar();
    }

    /**
     * Places the player's totem on the offer tile at the given board position
     * during the placing phase. The chosen tile determines how many draws from
     * the top and bottom market rows the player will have in the resolve-action phase.
     *
     * @param playerToPlace the player placing their totem.
     * @param position      the index of the target offer tile on the board.
     * @throws TileOccupiedException     if another player's totem is already on that tile.
     * @throws IndexOutOfBoundsException if {@code position} is out of range.
     */
    @Override
    public synchronized void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException {
        Player playerTemp=new Player(playerToPlace.getNickName(),playerToPlace.getColorTotem());
        controller.placingPlayer(playerTemp,position);
    }

    /**
     * Lets the player pick a card from the current-round (top) market row during the
     * resolve-action phase. Tribe member cards (Hunter, Gatherer, Builder, etc.) are added
     * to the player's tribe for free; building cards cost food equal to their price.
     *
     * @param player   the player making the selection.
     * @param cardType the type of card being selected (tribe member or {@link CARD_TYPE#BUILDING}).
     * @param position the index of the card in the top row.
     * @throws NotEnoughFoodException     if the player lacks the food to buy a building.
     * @throws NotSelectableCardException if the card at that position is an event card and cannot be picked.
     * @throws EmptyMarketException       if the top row has no selectable cards.
     * @throws IndexOutOfBoundsException  if {@code position} is out of range.
     */
    @Override
    public synchronized void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        Player playerTemp=new Player(player);
        controller.selectCardFromTopList(playerTemp,cardType,position);
    }

    /**
     * Lets the player pick a card from the previous-round (bottom) market row during the
     * resolve-action phase.
     *
     * @param player   the player making the selection.
     * @param cardType the type of card being selected (tribe member or {@link CARD_TYPE#BUILDING}).
     * @param position the index of the card in the bottom row.
     * @throws NotEnoughFoodException     if the player lacks the food to buy a building.
     * @throws NotSelectableCardException if the card at that position is an event card and cannot be picked.
     * @throws EmptyMarketException       if the bottom row has no selectable cards.
     * @throws IndexOutOfBoundsException  if {@code position} is out of range.
     */
    @Override
    public synchronized void selectCardFromBottomList(PlayerDTO player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, RemoteException {
        Player playerTemp=new Player(player);
        controller.selectCardFromBottomList(playerTemp,cardType,position);
    }

    /**
     * Signals that the player voluntarily skips their draw action for this turn,
     * without selecting any card from the market.
     *
     * @param playerDTO the player who is skipping their action.
     */
    @Override
    public synchronized void playerDoNothing(PlayerDTO playerDTO) throws Exception {
        Player playerTemp=new Player(playerDTO);
        controller.playerDoNothing(playerTemp);
    }

    /**
     * Selects the bonus card granted by the draw-one-more mechanic (triggered after
     * the server calls {@code askExtraDraw} on the client). The player picks one additional
     * card from either market row on top of their normal action.
     * Any checked exception from the controller is rethrown as a {@link RemoteException}.
     *
     * @param player   the player claiming the extra card.
     * @param cardType the type of card being selected (tribe member or {@link CARD_TYPE#BUILDING}).
     * @param position the index of the card in the relevant market row.
     */
    @Override
    public synchronized void selectExtraCard(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException {
        try {
            controller.selectExtraCard(new Player(player), cardType, position);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * Skips the extra draw for the given player without selecting any card,
     * releasing the server thread that was blocking on the extra-draw lock.
     *
     * @param player the player declining the extra draw.
     */
    @Override
    public synchronized void skipExtraDraw(PlayerDTO player) throws RemoteException {
        try {
            controller.skipExtraDraw(new Player(player));
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }
}
