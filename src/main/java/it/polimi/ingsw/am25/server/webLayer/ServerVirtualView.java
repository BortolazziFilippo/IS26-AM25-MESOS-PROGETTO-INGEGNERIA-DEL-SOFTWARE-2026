package it.polimi.ingsw.am25.server.webLayer;

import it.polimi.ingsw.am25.client.webLayer.PongWatchdog;
import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Observers.BoardObserver;
import it.polimi.ingsw.am25.server.model.Observers.GameObserver;
import it.polimi.ingsw.am25.server.model.Observers.MarketObserver;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Player.Totem;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesConstant;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server-side per-client view. Listens to all model observers and forwards each event
 * to the client's {@link ClientRemoteInterface} stub (RMI or Socket proxy).
 * Maintains a local snapshot of the game state so it can send full DTOs when needed.
 */
public class ServerVirtualView implements BoardObserver, GameObserver, MarketObserver, PlayerObserver {
    private static final String LOG_PREFIX = "[SERVER][VIEW]";
    private final String nickname;
    private ClientRemoteInterface clientStub;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     * Called when an RMI error is detected in a notification task, triggering disconnection.
     */
    private Runnable disconnectCallback;
    // --- HEARTBEAT ---
    private final AtomicInteger missedPings = new AtomicInteger(0);
    private volatile boolean connected = true;
    //_________________________________________________________________________________________
    private List<PlayerDTO> winners;
    private ERA currentEra;
    private GAME_PHASE currentGamePhase;
    private String playerToPlace;
    private String playerToPlay;
    //_________________________________________________________________________________________
    Map<String, PlayerDTO> playersMap = new HashMap<>();
    /**
     * Accumulated tribe cards per player, used to resync reconnecting clients.
     */
    private final Map<String, List<CardDTO>> tribeSnapshot = new HashMap<>();
    //_________________________________________________________________________________________
    //MARKET DTO
    private List<CardDTO> topCards;
    private List<CardDTO> bottomCards;
    private List<BuildingDTO> topBuildings;
    private List<BuildingDTO> bottomBuildings;
    private List<CardDTO> extraDrawSnapshotCards = new ArrayList<>();
    private List<BuildingDTO> extraDrawSnapshotBuildings = new ArrayList<>();
    //_________________________________________________________________________________________
    //BOARD DTO
    private List<OffertileDTO> offerTileList;
    private List<DefaultTileDTO> defaultTileList;

    /**
     * Creates a new server virtual view instance.
     *
     * @param clientStub         the RMI stub or Socket proxy to push notifications to.
     * @param nickname           the nickname of the player this view represents.
     * @param disconnectCallback called when an RMI error is detected, to trigger disconnection handling.
     */
    public ServerVirtualView(ClientRemoteInterface clientStub, String nickname, Runnable disconnectCallback) {
        this.clientStub = clientStub;
        this.nickname = nickname;
        this.disconnectCallback = disconnectCallback;
    }

    // --- HEARTBEAT API ---

    /**
     * Returns the nickname of the player represented by this view.
     *
     * @return the player's nickname.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Returns the RMI stub or Socket proxy used to send notifications to the client.
     *
     * @return the remote interface of the client associated with this view.
     */
    public ClientRemoteInterface getClientStub() {
        return clientStub;
    }

    /**
     * Returns whether the client is currently connected to the server.
     *
     * @return {@code true} if the client is connected, {@code false} if it has disconnected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Records reception of a ping from the client, resetting the missed-ping counter.
     * Called by the heartbeat watchdog to confirm that the client is still active.
     */
    public void receivePing() {
        missedPings.set(0);
    }

    /**
     * Increments the missed-ping counter and returns the new value.
     * Called on every watchdog tick; if it exceeds the threshold the player is declared disconnected.
     *
     * @return the updated number of consecutive missed pings.
     */
    public int incrementMissedPings() {
        return missedPings.incrementAndGet();
    }

    /**
     * Sends a keepalive pong to the client. Called by the server watchdog every tick
     * so the client's {@link PongWatchdog} can
     * detect server death if pongs stop arriving.
     */
    public void sendPong() {
        submitTask(() -> clientStub.pong());
    }

    /**
     * Marks the client as disconnected and shuts down the notification executor.
     * After this call, any task submitted via {@link #submitTask} is silently discarded.
     */
    public void markDisconnected() {
        connected = false;
        executor.shutdownNow();
    }

    /**
     * Swaps in a new client stub and restarts the executor so this view can be
     * reused after the player reconnects.
     *
     * @param newStub               the new RMI stub or Socket proxy.
     * @param newDisconnectCallback disconnect callback bound to the new connection.
     */
    public synchronized void reconnect(ClientRemoteInterface newStub, Runnable newDisconnectCallback) {
        this.clientStub = newStub;
        this.disconnectCallback = newDisconnectCallback;
        this.executor = Executors.newSingleThreadExecutor();
        this.connected = true;
        // Start negative so the watchdog needs ~21s (7 ticks × 3s) before it can
        // declare this player disconnected again — same window as the startup grace.
        this.missedPings.set(-4);
    }

    /**
     * Sends all stored state snapshots to the reconnecting client so they can
     * resume without missing any game state that was pushed while they were offline.
     */
    public void resyncClient() {
        // 1. All players with their full tribe snapshot included in the DTO
        List<PlayerDTO> players = new ArrayList<>(playersMap.values());
        for (PlayerDTO dto : players) {
            PlayerDTO enriched = new PlayerDTO(dto.getNickName(), dto.getFood(), dto.getPrestigePoint(), dto.getColorTotem());
            tribeSnapshot.getOrDefault(dto.getNickName(), List.of()).forEach(enriched::addCardToTribe);
            submitTask(() -> clientStub.playerAdded(enriched));
        }
        // 2. Board (offer tiles + default tiles, including totem positions)
        if (offerTileList != null) {
            List<OffertileDTO> board = new ArrayList<>(offerTileList);
            List<DefaultTileDTO> defs = new ArrayList<>(defaultTileList);
            submitTask(() -> clientStub.boardInitialize(board, defs));
        }
        // 3. Market (top cards + bottom cards + top buildings + bottom buildings)
        if (topCards != null) {
            List<CardDTO> top = new ArrayList<>(topCards);
            List<CardDTO> bot = bottomCards != null ? new ArrayList<>(bottomCards) : new ArrayList<>();
            List<BuildingDTO> bld = topBuildings != null ? new ArrayList<>(topBuildings) : new ArrayList<>();
            List<BuildingDTO> botBld = bottomBuildings != null ? new ArrayList<>(bottomBuildings) : new ArrayList<>();
            submitTask(() -> clientStub.initializeMarket(top, bot, bld, botBld));
        }
        // 4. Updated food / PP for every player
        for (PlayerDTO dto : players) {
            final String nick = dto.getNickName();
            final int food = dto.getFood();
            final int pp = dto.getPrestigePoint();
            submitTask(() -> clientStub.playerUpdateFood(nick, food));
            submitTask(() -> clientStub.playerUpdatePP(nick, pp));
        }
        // 5. Era and game phase (phase change is what unlocks the TUI waiting loop)
        ERA era = currentEra;
        if (era != null) submitTask(() -> clientStub.eraChanged(era));
        GAME_PHASE phase = currentGamePhase;
        if (phase != null) submitTask(() -> clientStub.gamePhaseChanged(phase));
        // 6. Whose turn it is
        String toPlace = playerToPlace;
        String toPlay = playerToPlay;
        if (toPlace != null && playersMap.containsKey(toPlace)) {
            PlayerDTO dto = playersMap.get(toPlace);
            submitTask(() -> clientStub.playerToPlaceChanged(dto));
        } else if (toPlay != null && playersMap.containsKey(toPlay)) {
            PlayerDTO dto = playersMap.get(toPlay);
            submitTask(() -> clientStub.playerToPlayChanged(dto));
        }
    }

    /**
     * Notifies the client that a player has disconnected from the game.
     *
     * @param disconnectedNickname the nickname of the player who disconnected.
     */
    public void notifyPlayerDisconnected(String disconnectedNickname) {
        if (!connected) return;
        submitTask(() -> clientStub.playerDisconnected(disconnectedNickname));
    }

    /**
     * Notifies the client that a previously disconnected player has reconnected.
     *
     * @param reconnectedNickname the nickname of the player who reconnected.
     */
    public void notifyPlayerReconnected(String reconnectedNickname) {
        if (!connected) return;
        submitTask(() -> clientStub.playerReconnected(reconnectedNickname));
    }

    /**
     * Updates a player's prestige-point score in the local snapshot and notifies the client.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.PlayerObserver}.
     *
     * @param nickname the nickname of the updated player.
     * @param newPP    the new prestige-point total.
     */
    @Override
    public void notifyPPChanged(String nickname, int newPP) {
        PlayerDTO pl = playersMap.get(nickname);
        pl.setPrestigePoint(newPP);
        playersMap.put(nickname, pl);
        submitTask(() -> clientStub.playerUpdatePP(nickname, newPP));
    }

    /**
     * Updates a player's food amount in the local snapshot and notifies the client.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.PlayerObserver}.
     *
     * @param nickname the nickname of the updated player.
     * @param newFood  the new food total.
     */
    @Override
    public void notifyFoodChanged(String nickname, int newFood) {
        PlayerDTO pl = playersMap.get(nickname);
        pl.setFood(newFood);
        playersMap.put(nickname, pl);
        submitTask(() -> clientStub.playerUpdateFood(nickname, newFood));
    }

    /**
     * Sends this client the full list of players in the lobby/game,
     * also updating the local snapshot map. Called at game start
     * to ensure all clients have a consistent initial state.
     *
     * @param allPlayers the list of all {@link PlayerDTO} instances to synchronise on the client.
     */
    public void forceInitialPlayersSync(List<PlayerDTO> allPlayers) {
        for (PlayerDTO player : allPlayers) {
            this.playersMap.put(player.getNickName(), player);
            try {
                clientStub.playerAdded(player);
            } catch (Exception e) {
                logServerError("Failed to sync player '" + nickname + "'");
            }
        }
    }

    /**
     * Deprecated observer callback: updates the local player snapshot without notifying the client.
     * Superseded by {@link #notifyPPChanged} and {@link #notifyFoodChanged} for granular updates.
     *
     * @param nickname       the player's nickname.
     * @param totem          the player's totem (colour).
     * @param food           the player's current food amount.
     * @param prestigePoint  the player's current prestige points.
     * @param tribe          the list of tribe cards belonging to the player.
     * @param buildingCards  the list of building cards belonging to the player.
     */
    @Override
    @Deprecated
    public void onPlayerChanged(String nickname, Totem totem, int food, int prestigePoint, List<Card> tribe, List<BuildingCard> buildingCards) {
        playersMap.put(nickname, new PlayerDTO(nickname, food, prestigePoint, totem.color()));
    }

    /**
     * Updates the market snapshot and sends the full new configuration
     * (top and bottom rows of cards and buildings) to the client.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.MarketObserver}.
     *
     * @param topCards        the cards in the top market row.
     * @param bottomCards     the cards in the bottom market row.
     * @param topBuildings    the buildings in the top market row.
     * @param bottomBuildings the buildings in the bottom market row.
     */
    @Override
    public void onMarketChanged(List<Card> topCards, List<Card> bottomCards, List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {
        this.topCards = new ArrayList<>(topCards.stream().map(Card::toDTO).toList());
        this.bottomCards = new ArrayList<>(bottomCards.stream().map(Card::toDTO).toList());
        this.topBuildings = new ArrayList<>(topBuildings.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
        this.bottomBuildings = new ArrayList<>(bottomBuildings.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
        submitTask(() -> clientStub.initializeMarket(this.topCards, this.bottomCards, this.topBuildings, this.bottomBuildings));
    }

    /**
     * Moves the top market row to the bottom, updates the top row with the new cards,
     * and notifies the client.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.MarketObserver}.
     *
     * @param topCards the new cards that make up the top market row.
     */
    @Override
    public void onTopCardRefreshed(List<Card> topCards) {
        if (this.topCards != null) {
            this.bottomCards = new ArrayList<>(this.topCards);
        }
        this.topCards = new ArrayList<>(topCards.stream().map(Card::toDTO).toList());
        submitTask(() -> clientStub.topCardRefreshed(new ArrayList<>(this.topCards)));
    }

    /**
     * Updates the board snapshot and sends the new configuration of offer tiles
     * and default tiles to the client.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.BoardObserver}.
     *
     * @param offerTileList   the updated offer tiles with totem positions.
     * @param defaultTileList the updated default tiles.
     */
    @Override
    public void onBoardChanged(List<OfferTile> offerTileList, List<DefaultTile> defaultTileList) {
        this.offerTileList = offerTileList.stream().map(OffertileDTO::new).toList();
        this.defaultTileList = defaultTileList.stream().map(DefaultTileDTO::new).toList();
        submitTask(() -> clientStub.boardInitialize(this.offerTileList, this.defaultTileList));
    }

    /**
     * Notifies the client of the order in which players are on the default tile
     * at the end of the placing phase.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.BoardObserver}.
     *
     * @param playerOrder the ordered list of players on the default tile.
     */
    @Override
    public void playerToDefaultTile(List<Player> playerOrder) {
        List<PlayerDTO> order = playerOrder.stream().map(PlayerDTO::new).toList();
        submitTask(() -> clientStub.orderOnDefaultTile(order));
    }

    /**
     * Notifies the client that a player has placed their totem on an offer tile.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.BoardObserver}.
     *
     * @param player       the player who performed the placement.
     * @param tilePosition the index of the chosen offer tile.
     */
    @Override
    public void playerPlacedOnOffertile(Player player, int tilePosition) {
        submitTask(() -> clientStub.playerPlacedOnOffertile(player.getNickname(), tilePosition));
    }

    /**
     * Saves the list of winners in the local snapshot and notifies the client that the game is over.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}.
     *
     * @param winners the list of winning players.
     */
    @Override
    public void gameWinners(List<Player> winners) {
        this.winners = winners.stream().map(PlayerDTO::new).toList();
        submitTask(() -> clientStub.gameWinners(this.winners));
    }

    /**
     * Updates the era and game-phase snapshots, clears the current-turn fields,
     * and sends the initial game state to the client.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}.
     *
     * @param currentEra    the current era of the game.
     * @param players       the list of all players (not used directly in this callback).
     * @param gamePhase     the current game phase.
     * @param playerToPlace the player who must place their totem (not used directly here).
     * @param playerToPlay  the player who must perform their action (not used directly here).
     */
    @Override
    public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase, Player playerToPlace, Player playerToPlay) {
        this.currentEra = currentEra;
        this.currentGamePhase = gamePhase;
        this.playerToPlace = null;
        this.playerToPlay = null;
        submitTask(() -> clientStub.initializeGame(this.currentEra, this.currentGamePhase, null, null));
    }

    /**
     * Adds the player to the local snapshot map and notifies the client of the new lobby entry.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}.
     *
     * @param playerAdded the player who joined the game or lobby.
     */
    @Override
    public void onPlayerAdded(Player playerAdded) {
        PlayerDTO player = new PlayerDTO(playerAdded);
        playersMap.put(playerAdded.getNickname(), player);
        submitTask(() -> clientStub.playerAdded(player));
    }

    /**
     * Variant of {@link #onPlayerAdded(Player)} that accepts a {@link PlayerDTO} directly.
     * Used by {@code ServerNetworkHandler} to notify clients during the lobby phase,
     * before any {@link Player} model objects exist.
     *
     * @param player the DTO of the player who just joined the lobby.
     */
    public void pushPlayerAdded(PlayerDTO player) {
        playersMap.put(player.getNickName(), player);
        submitTask(() -> clientStub.playerAdded(player));
    }

    /**
     * Updates the current era in the local snapshot and notifies the client of the era change.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}.
     *
     * @param currentEra the new game era.
     */
    @Override
    public void onEraChanged(ERA currentEra) {
        this.currentEra = currentEra;
        submitTask(() -> clientStub.eraChanged(this.currentEra));
    }

    /**
     * Updates the game phase in the local snapshot and notifies the client of the phase change.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}.
     *
     * @param gamePhase the new game phase.
     */
    @Override
    public void onGamePhaseChanged(GAME_PHASE gamePhase) {
        this.currentGamePhase = gamePhase;
        submitTask(() -> clientStub.gamePhaseChanged(currentGamePhase));
    }

    /**
     * Updates the current player for the placing phase and notifies the client.
     * Clears the action-phase player field since the two phases are mutually exclusive.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}.
     *
     * @param newPlayerToPlace the player who must place their totem.
     */
    @Override
    public void onPlayerToPlaceChanged(Player newPlayerToPlace) {
        this.playerToPlay = null;
        this.playerToPlace = newPlayerToPlace.getNickname();
        submitTask(() -> clientStub.playerToPlaceChanged(new PlayerDTO(newPlayerToPlace)));
    }

    /**
     * Updates the current player for the action phase and notifies the client.
     * Clears the placing-phase player field since the two phases are mutually exclusive.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}.
     *
     * @param newPlayerToPlay the player who must perform their game action.
     */
    @Override
    public void onPlayerToPlayChanged(Player newPlayerToPlay) {
        this.playerToPlace = null;
        this.playerToPlay = newPlayerToPlay.getNickname();
        submitTask(() -> clientStub.playerToPlayChanged(new PlayerDTO(newPlayerToPlay)));
    }

    /**
     * Saves the end-of-turn market snapshot used for the extra draw.
     * Does not immediately notify the client: the data is sent in {@link #requestExtraDraw}
     * only to the player entitled to the additional card.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.MarketObserver}.
     *
     * @param snapshotCards     the cards available in the end-of-turn snapshot.
     * @param snapshotBuildings the buildings available in the end-of-turn snapshot.
     */
    @Override
    public void onExtraDrawSnapshotReady(List<Card> snapshotCards, List<BuildingCard> snapshotBuildings) {
        this.extraDrawSnapshotCards = new ArrayList<>(snapshotCards.stream().map(Card::toDTO).toList());
        this.extraDrawSnapshotBuildings = new ArrayList<>(snapshotBuildings.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
    }

    /**
     * Moves the top building row to the bottom, updates the top row with the new buildings,
     * and notifies the client.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.MarketObserver}.
     *
     * @param topBuildingCards the new buildings that make up the top market row.
     */
    @Override
    public void onTopBuildingRefreshed(List<BuildingCard> topBuildingCards) {
        if (this.topBuildings != null) {
            this.bottomBuildings = new ArrayList<>(this.topBuildings);
        }
        this.topBuildings = new ArrayList<>(topBuildingCards.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
        submitTask(() -> clientStub.topBuildingRefreshed(this.topBuildings));
    }

    /**
     * Removes the card or building at the given position from the top market row
     * in the local snapshot and notifies the client of the removal.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.MarketObserver}.
     *
     * @param position the index of the card/building removed from the top row.
     * @param cardType the type of element removed: {@link CARD_TYPE#BUILDING} for a building,
     *                 any other value for a tribe card.
     */
    @Override
    public void onCardRemovedFromTop(int position, CARD_TYPE cardType) {
        if (cardType == CARD_TYPE.BUILDING) {
            this.topBuildings.remove(position);
            submitTask(() -> clientStub.topBuildRemoved(position));
        } else {
            this.topCards.remove(position);
            submitTask(() -> clientStub.topCardRemoved(position));
        }
    }

    /**
     * Removes the card or building at the given position from the bottom market row
     * in the local snapshot and notifies the client of the removal.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.MarketObserver}.
     *
     * @param position the index of the card/building removed from the bottom row.
     * @param cardType the type of element removed: {@link CARD_TYPE#BUILDING} for a building,
     *                 any other value for a tribe card.
     */
    @Override
    public void onCardRemovedFromBottom(int position, CARD_TYPE cardType) {
        if (cardType == CARD_TYPE.BUILDING) {
            this.bottomBuildings.remove(position);
            submitTask(() -> clientStub.bottomBuildRemoved(position));
        } else {
            this.bottomCards.remove(position);
            submitTask(() -> clientStub.bottomCardRemoved(position));
        }
    }

    /**
     * Adds the card to the player's tribe snapshot and notifies the client.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.PlayerObserver}.
     *
     * @param playername the nickname of the player who received the card.
     * @param cardAdded  the card added to the player's tribe.
     */
    @Override
    public void notifyCardAddedToTribe(String playername, Card cardAdded) {
        CardDTO dto = cardAdded.toDTO();
        tribeSnapshot.computeIfAbsent(playername, k -> new ArrayList<>()).add(dto);
        submitTask(() -> clientStub.addedCardToTribe(playername, dto));
    }

    /**
     * Sends the extra-draw request to the client only if the nickname matches the player
     * of this view. The request includes the previously saved market snapshot.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.PlayerObserver}.
     *
     * @param nickname the nickname of the player entitled to the extra draw.
     */
    @Override
    public void requestExtraDraw(String nickname) {
        if (!this.nickname.equals(nickname)) return;
        List<CardDTO> cards = new ArrayList<>(extraDrawSnapshotCards);
        List<BuildingDTO> buildings = new ArrayList<>(extraDrawSnapshotBuildings);
        submitTask(() -> clientStub.askExtraDraw(cards, buildings));
    }

    /**
     * Notifies the client of the change in available actions (number of draws from the top
     * and bottom rows) based on the chosen offer tile.
     * Observer callback from {@link it.polimi.ingsw.am25.server.model.Observers.BoardObserver}.
     *
     * @param drawTop    the number of cards drawable from the top market row.
     * @param drawBottom the number of cards drawable from the bottom market row.
     */
    @Override
    public void actionOfferTileChanged(int drawTop, int drawBottom) {
        submitTask(() -> clientStub.actionAvailableChanged(new ActionDTO(drawTop, drawBottom)));
    }

    /**
     * Submits a notification task. If the client's RMI call throws {@link RemoteException},
     * the error is logged and the disconnect callback is invoked to trigger proper
     * disconnection handling. Tasks submitted after {@link #markDisconnected()} are silently
     * discarded.
     */
    private void submitTask(ThrowingRunnable task) {
        if (!connected) return;
        try {
            executor.submit(() -> {
                int attempt = 0;
                while (true) {
                    try {
                        task.run();
                        return;
                    } catch (RemoteException e) {
                        attempt++;
                        if (attempt > UtilitiesConstant.NETWORK_CALLBACK_MAX_RETRIES) {
                            logServerError("RMI error for '" + nickname + "' after " + attempt + " attempts: " + e.getMessage() + " — triggering disconnection.");
                            disconnectCallback.run();
                            return;
                        }
                        logServerError("RMI error for '" + nickname + "' (attempt " + attempt + "/" + UtilitiesConstant.NETWORK_CALLBACK_MAX_RETRIES + "): " + e.getMessage() + " — retrying...");
                        try {
                            Thread.sleep(UtilitiesConstant.NETWORK_CALLBACK_RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            disconnectCallback.run();
                            return;
                        }
                    }
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
            // Executor was already shut down (player disconnected) — nothing to do.
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws RemoteException;
    }

    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }

    /**
     * Notifica il client che un evento è stato risolto, indicando l'ID dell'evento e il tipo.
     * Callback dell'observer {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}.
     *
     * @param eventID   l'identificatore univoco dell'evento risolto.
     * @param eventType il tipo dell'evento risolto.
     */
    @Override
    public void eventSolved(int eventID, EVENT_TYPE eventType) {
        submitTask(() -> clientStub.eventResolved(eventID, eventType));
    }
}
