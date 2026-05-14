package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Controller.Controller;
import it.polimi.ingsw.am25.server.model.DBmanager.DBManager;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesConstant;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.Socket.ClientSocketProxy;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RMI server-side entry point for all Mesos client actions. Manages the game lobby,
 * registers client stubs, and delegates game logic to the {@link Controller}.
 * Also used as the target for Socket clients via {@link it.polimi.ingsw.am25.server.webLayer.Socket.SocketClientHandler}.
 */
public class ServerNetworkHandler extends UnicastRemoteObject implements ServerRemoteInterface {
    private static final String LOG_PREFIX = "[SERVER][NETWORK]";
    private final List<ServerVirtualView> waitingPlayers = new ArrayList<>();
    /**
     * Fast nickname → view lookup used by ping() without holding the global lock.
     */
    private final ConcurrentHashMap<String, ServerVirtualView> viewsByNickname = new ConcurrentHashMap<>();
    private final List<PlayerDTO> playerDTOS = new ArrayList<>();
    private Controller controller;
    private int requiredPlayers = 0;
    private boolean isGameStarted = false;
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);
    private volatile ScheduledExecutorService watchdogScheduler;

    /**
     * Initializes the RMI network handler and exports it as a remote object,
     * making it reachable by Mesos clients via the RMI registry.
     *
     * @throws RemoteException if the RMI export fails.
     */
    public ServerNetworkHandler() throws RemoteException {
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
    public synchronized void createGame(PlayerDTO playerHost, int playerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException, IllegalStateException {
        if (requiredPlayers > 0) {
            throw new IllegalStateException("Game already started");
        }
        this.requiredPlayers = playerNumber;
        // Create the VirtualView for the host and bind their remote interface.
        String hostNick = playerHost.getNickName();
        ServerVirtualView hostView = new ServerVirtualView(clientRemoteInterface, hostNick,
                () -> notifyPlayerDisconnected(hostNick));
        waitingPlayers.add(hostView);
        viewsByNickname.put(hostNick, hostView);
        playerDTOS.add(playerHost);
        hostView.pushPlayerAdded(playerHost);
        logServerEvent("Game created by '" + playerHost.getNickName() + "' for " + playerNumber + " players");
    }

    /**
     * Adds a player to the existing Mesos lobby and registers their RMI stub.
     * If this player fills the last required slot, the game starts automatically.
     *
     * @param playerDTO             the joining player's data (nickname and totem color).
     * @param clientRemoteInterface the player's RMI stub, saved to notify them of game events.
     * @throws GameFullException                if no lobby exists yet (no host has called {@link #createGame}).
     * @throws GameStartedException             if the game is already running.
     * @throws NameOrColorAlreadyTakenException if the chosen nickname or totem color is already taken by another player.
     */
    @Override
    public synchronized void addPlayer(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameFullException, GameReadyToStartException, NameOrColorAlreadyTakenException {
        if (requiredPlayers == 0) {
            throw new GameFullException("Nessuna partita creata!");
        }
        if (isGameStarted) {
            // Allow a disconnected player to rejoin with the same nickname
            ServerVirtualView disconnectedView = null;
            for (ServerVirtualView view : waitingPlayers) {
                if (view.getNickname().equals(playerDTO.getNickName()) && !view.isConnected()) {
                    disconnectedView = view;
                    break;
                }
            }
            if (disconnectedView != null) {
                handleReconnection(playerDTO.getNickName(), clientRemoteInterface);
                return;
            }
            throw new GameStartedException("Partita già in corso!");
        }
        if (playerDTOS.stream().anyMatch(player -> Objects.equals(player.getNickName(), playerDTO.getNickName()) || player.getColorTotem() == playerDTO.getColorTotem())) {
            throw new NameOrColorAlreadyTakenException("Errore: Nickname o colore del Totem già in uso!");
        }

        // Create the VirtualView for the new player and bind their remote interface.
        String joinNick = playerDTO.getNickName();
        ServerVirtualView playerView = new ServerVirtualView(clientRemoteInterface, joinNick,
                () -> notifyPlayerDisconnected(joinNick));
        waitingPlayers.add(playerView);
        viewsByNickname.put(joinNick, playerView);
        playerDTOS.add(playerDTO);
        logServerEvent("Player '" + playerDTO.getNickName() + "' joined (" + waitingPlayers.size() + "/" + requiredPlayers + ")");


        for (PlayerDTO existing : playerDTOS) {
            playerView.pushPlayerAdded(existing);
        }
        for (int i = 0; i < waitingPlayers.size() - 1; i++) {
            waitingPlayers.get(i).pushPlayerAdded(playerDTO);
        }

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
        startWatchdog();
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
        Player playerTemp = new Player(playerToPlace.getNickName(), playerToPlace.getColorTotem());
        controller.placingPlayer(playerTemp, position);
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
        Player playerTemp = new Player(player);
        controller.selectCardFromTopList(playerTemp, cardType, position);
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
        Player playerTemp = new Player(player);
        controller.selectCardFromBottomList(playerTemp, cardType, position);
    }

    /**
     * Signals that the player voluntarily skips their draw action for this turn,
     * without selecting any card from the market.
     *
     * @param playerDTO the player who is skipping their action.
     */
    @Override
    public synchronized void playerDoNothing(PlayerDTO playerDTO) throws Exception {
        Player playerTemp = new Player(playerDTO);
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

    /**
     * Retrieves leaderboards from the database for all player-count configurations from 2 up to
     * the given number, and sends them to the requesting client via {@link ClientRemoteInterface#sendRank}.
     * If the database is unreachable for a given size, the corresponding entry is replaced with an error message.
     *
     * @param playerNumber          the maximum number of players for which to load the leaderboard,
     *                              provided as a string and converted internally to an integer.
     * @param clientRemoteInterface the RMI stub (or Socket proxy) of the client to send the leaderboards to.
     * @throws RemoteException if an RMI communication error occurs.
     */
    @Override
    public synchronized void askForRank(String playerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException {
        int number = UtilitiesFunction.stringToIntegerBinder(playerNumber);
        Map<Integer, List<String>> leaderboards = new HashMap<>();
        for (int i = 2; i <= number; i++) {
            try {
                leaderboards.put(i, DBManager.getLeaderboard(i));
            } catch (SQLException | IOException e) {
                logServerEvent("DB non raggiungibile per classifica " + i + " giocatori: " + e.getMessage());
                leaderboards.put(i, List.of("Classifica non disponibile"));
            }
        }
        clientRemoteInterface.sendRank(leaderboards);
    }

    /**
     * Receives a heartbeat ping from a client and resets that player's missed-ping counter.
     * Uses a ConcurrentHashMap lookup so it does not need the global lock.
     *
     * @param player the player sending the ping (identified by nickname).
     */
    @Override
    public void ping(PlayerDTO player) throws RemoteException {
        ServerVirtualView view = viewsByNickname.get(player.getNickName());
        if (view != null) view.receivePing();
    }

    /**
     * Called by {@link it.polimi.ingsw.am25.server.webLayer.Socket.SocketClientHandler}
     * when a socket client's stream drops. Finds the virtual view matching the given proxy
     * and triggers disconnection handling.
     *
     * @param proxy the {@link ClientRemoteInterface} proxy whose socket closed.
     */
    public synchronized void handleSocketClientDisconnection(ClientRemoteInterface proxy) {
        String nickname = null;
        for (ServerVirtualView view : waitingPlayers) {
            if (view.getClientStub() == proxy) {
                nickname = view.getNickname();
                break;
            }
        }
        if (nickname != null) {
            notifyPlayerDisconnected(nickname);
        }
    }

    /**
     * Central disconnection handler. Marks the view as disconnected, broadcasts
     * a {@code playerDisconnected} notification to all clients, then tells the
     * controller to update the game model and advance the turn if needed.
     *
     * @param nickname the disconnected player's nickname.
     */
    public synchronized void notifyPlayerDisconnected(String nickname) {
        if (!isGameStarted || controller == null) return;

        // Find the view; guard against double-disconnection
        ServerVirtualView disconnectedView = null;
        for (ServerVirtualView view : waitingPlayers) {
            if (view.getNickname().equals(nickname)) {
                disconnectedView = view;
                break;
            }
        }
        if (disconnectedView == null || !disconnectedView.isConnected()) return;

        // Stop sending notifications to the dead client
        disconnectedView.markDisconnected();
        if (disconnectedView.getClientStub() instanceof ClientSocketProxy proxy) {
            proxy.closeConnection();
        }
        logServerEvent("Player '" + nickname + "' has disconnected.");

        // 1. Tell all surviving connected clients about the disconnection
        for (ServerVirtualView view : waitingPlayers) {
            if (view.isConnected()) {
                view.notifyPlayerDisconnected(nickname);
            }
        }

        // 2. Update game model and advance turn / end game if necessary
        controller.notifyPlayerDisconnected(nickname);

        // 3. Se non ci sono più giocatori connessi, spegni il server.
        if (waitingPlayers.stream().noneMatch(ServerVirtualView::isConnected)) {
            initiateShutdown();
        }
    }

    /**
     * Starts the counter-based heartbeat watchdog. Every second each view's
     * missed-ping counter is incremented. When a view reaches 3 consecutive missed pings
     * (~3 seconds total) the player is declared disconnected.
     */
    private void startWatchdog() {
        watchdogScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-watchdog");
            t.setDaemon(true);
            return t;
        });
        watchdogScheduler.scheduleAtFixedRate(this::watchdogTick,
                UtilitiesConstant.HEARTBEAT_WATCHDOG_INITIAL_DELAY_S,
                UtilitiesConstant.HEARTBEAT_WATCHDOG_INTERVAL_S,
                TimeUnit.SECONDS);
        logServerEvent("Heartbeat watchdog started (tick=" + UtilitiesConstant.HEARTBEAT_WATCHDOG_INTERVAL_S
                + "s, threshold=" + UtilitiesConstant.HEARTBEAT_MISSED_PING_THRESHOLD + " missed pings).");
    }

    private void watchdogTick() {
        for (ServerVirtualView view : new ArrayList<>(waitingPlayers)) {
            if (!view.isConnected()) continue;
            int missed = view.incrementMissedPings();
            if (missed >= UtilitiesConstant.HEARTBEAT_MISSED_PING_THRESHOLD) {
                logServerEvent("Player '" + view.getNickname()
                        + "' missed " + missed + " consecutive pings — declaring disconnected.");
                notifyPlayerDisconnected(view.getNickname());
            }
        }
    }

    /**
     * Handles a player reconnecting to an in-progress game. Swaps the client stub,
     * decrements the rank counter that was incremented at disconnect time, notifies
     * all other clients, updates the game model, and re-syncs the full game state.
     *
     * @param nickname the reconnecting player's nickname.
     * @param newStub  the new RMI stub or Socket proxy for the reconnected client.
     */
    private void handleReconnection(String nickname, ClientRemoteInterface newStub) {
        ServerVirtualView view = viewsByNickname.get(nickname);
        if (view == null || view.isConnected()) return;

        view.reconnect(newStub, () -> notifyPlayerDisconnected(nickname));

        // Notify all other connected clients that this player is back.
        for (ServerVirtualView v : waitingPlayers) {
            if (!v.getNickname().equals(nickname) && v.isConnected()) {
                v.notifyPlayerReconnected(nickname);
            }
        }

        // Update the game model so the player is back in the turn queues.
        controller.notifyPlayerReconnected(nickname);

        // Push all accumulated game state to the reconnecting client.
        view.resyncClient();

        logServerEvent("Player '" + nickname + "' has reconnected.");
    }

    /**
     * Shuts down the server once all players have disconnected
     * Guards against multiple shutdown threads with an {@code AtomicBoolean}.
     */
    private void initiateShutdown() {
        if (shutdownInitiated.compareAndSet(false, true)) {
            if (watchdogScheduler != null) watchdogScheduler.shutdownNow();
            Thread shutdown = new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                UtilitiesFunction.logInfo(LOG_PREFIX, "Tutti i client si sono disconnessi. Server in chiusura.");
                System.exit(0);
            });
            shutdown.setDaemon(true);
            shutdown.start();
        }
    }

    /**
     * Starts loading a saved game from disk. The first player to invoke this method becomes the
     * "loader": the controller is initialised with the persisted data and their view is registered
     * while waiting for the other players to reconnect via {@link #joinGameLoaded}.
     *
     * @param playerDTO             the data of the player requesting the load (nickname and totem colour).
     * @param clientRemoteInterface the RMI stub (or Socket proxy) of the loading client.
     * @throws RemoteException            if an RMI communication error occurs.
     * @throws GameAlreadyLoadedException if an active controller already exists (game already in progress or loaded).
     * @throws NoGameToLoadException      if no saved game exists to load.
     */
    @Override
    public synchronized void loadGame(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameAlreadyLoadedException, NoGameToLoadException {
        if (controller != null) {
            throw new GameAlreadyLoadedException("Partita già caricata/in corso");
        }
        Controller newController = new Controller();
        Player player = new Player(playerDTO);
        try {
            newController.loadGame(player);
        } catch (NoGameToLoadException e) {
            throw new NoGameToLoadException("Non ci sono partite da caricare");
        } catch (GameAlreadyLoadedException e) {
            throw new GameAlreadyLoadedException("Partita già caricata/in corso");
        } catch (IllegalStateException e) {
            throw new RemoteException("Nickname non trovato nella partita salvata");
        }

        // Commit the controller and set up the loaded lobby
        this.controller = newController;
        this.requiredPlayers = controller.getAllPlayers().size();
        for (Player p : controller.getAllPlayers()) {
            playerDTOS.add(new PlayerDTO(p));
        }

        String nick = player.getNickname();
        ServerVirtualView clientView = new ServerVirtualView(clientRemoteInterface, nick,
                () -> notifyPlayerDisconnected(nick));
        waitingPlayers.add(clientView);
        viewsByNickname.put(nick, clientView);
        logServerEvent("Game loaded by '" + nick + "'. Waiting for " + (requiredPlayers - 1) + " more player(s).");
    }

    /**
     * Allows a player to rejoin a game that is being loaded, after the first player has invoked
     * {@link #loadGame}. When all expected players have reconnected, the game resumes automatically.
     *
     * @param playerDTO             the data of the reconnecting player (nickname and totem colour).
     * @param clientRemoteInterface the RMI stub (or Socket proxy) of the reconnecting client.
     * @throws RemoteException       if an RMI communication error occurs, or if the nickname is
     *                               not present in the saved game.
     * @throws IllegalStateException if no game is currently being loaded.
     * @throws GameReadyToStartException (not propagated externally) when the last player reconnects
     *                               and the game is started internally.
     */
    @Override
    public synchronized void joinGameLoaded(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, IllegalStateException, GameReadyToStartException {
        if (controller == null) {
            throw new IllegalStateException("Nessuna partita in caricamento");
        }
        Player player = new Player(playerDTO);
        String nick = player.getNickname();
        ServerVirtualView clientView = new ServerVirtualView(clientRemoteInterface, nick,
                () -> notifyPlayerDisconnected(nick));

        try {
            controller.reconnectLoadedPlayer(player);
        } catch (IllegalStateException e) {
            throw new RemoteException("Nickname non trovato nella partita salvata");
        } catch (GameReadyToStartException e) {
            // All players reconnected — register last view and start
            waitingPlayers.add(clientView);
            viewsByNickname.put(nick, clientView);
            isGameStarted = true;
            startLoadedGame();
            return;
        }

        // Not all players yet — just register the view
        waitingPlayers.add(clientView);
        viewsByNickname.put(nick, clientView);
        logServerEvent("Player '" + nick + "' joined loaded game (" + waitingPlayers.size() + "/" + requiredPlayers + ").");
    }

    /**
     * Bootstraps a resumed game once all expected players have reconnected.
     * Links observers, syncs state to all clients, and starts the heartbeat watchdog.
     */
    private void startLoadedGame() {
        logServerEvent("All players reconnected. Resuming loaded game...");
        waitingPlayers.forEach(controller::linkObserver);
        controller.crossRegisterPlayerObservers(waitingPlayers);
        for (ServerVirtualView view : waitingPlayers) {
            view.forceInitialPlayersSync(playerDTOS);
        }
        controller.resumeGame();
        startWatchdog();
    }

    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }
}
