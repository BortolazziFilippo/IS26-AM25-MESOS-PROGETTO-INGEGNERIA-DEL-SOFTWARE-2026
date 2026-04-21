package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Controller.Controller;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerNetworkHandler extends UnicastRemoteObject implements ServerRemoteInterface {
    private static final String LOG_PREFIX = "[SERVER][NETWORK]";
    private final List<ServerVirtualView> waitingPlayers = new ArrayList<>();
    private List<PlayerDTO> playerDTOS=new ArrayList<>();
    private Controller controller;
    private int requiredPlayers = 0;
    private boolean isGameStarted = false;
    public ServerNetworkHandler() throws RemoteException{
        super();
    }

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
    private void setupAndStartGame() {
        logServerEvent("All players ready. Setting up the game...");
        Controller controller = new Controller();

        // CRUCIAL SAVE: We save the controller so other RMI methods
        // (like selectCard, placingPlayer, etc.) can use it to perform actions!
        this.controller = controller;

        // 1. Extract the Host data (they are always at position 0)
        PlayerDTO hostDTO = playerDTOS.get(0);
        ServerVirtualView hostView = waitingPlayers.get(0);

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

        logServerEvent("All players added to the model. Synchronizing initial state to clients...");
        // ----------------------------------------------------------------
        // 5. MASS INITIAL SYNCHRONIZATION ON CLIENTS
        // ----------------------------------------------------------------
        for (ServerVirtualView view : waitingPlayers) {
            // Pass the complete list of PlayerDTOs to EVERY VirtualView
            view.forceInitialPlayersSync(this.playerDTOS);
        }
        logServerEvent("All clients synced. The game is ready!");
        controller.controllerGameStar();
    }

    @Override
    public synchronized void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException {
        Player playerTemp=new Player(playerToPlace.getNickName(),playerToPlace.getColorTotem());
        controller.placingPlayer(playerTemp,position);
    }

    @Override
    public synchronized void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        Player playerTemp=new Player(player);
        controller.selectCardFromTopList(playerTemp,cardType,position);
    }

    @Override
    public synchronized void selectCardFromBottomList(PlayerDTO player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, RemoteException {
        Player playerTemp=new Player(player);
        controller.selectCardFromBottomList(playerTemp,cardType,position);
    }

    @Override
    public synchronized void playerDoNothing(PlayerDTO playerDTO) throws RuntimeException, Exception {
        Player playerTemp=new Player(playerDTO);
        controller.playerDoNothing(playerTemp);
    }

    @Override
    public void selectExtraCard(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException {
        try {
            controller.selectExtraCard(new  Player( player), cardType, position);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private void logServerEvent(String message) {
        System.out.println(LOG_PREFIX + " " + message);
    }
}
