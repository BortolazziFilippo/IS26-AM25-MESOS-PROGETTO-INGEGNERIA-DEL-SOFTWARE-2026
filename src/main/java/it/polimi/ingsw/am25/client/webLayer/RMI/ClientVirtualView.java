package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.client.GUI.GUIObserver;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Client-side implementation of {@link ClientRemoteInterface}. Receives game-state
 * push notifications from the server and stores them in volatile fields that the TUI
 * reads. Exposes lock objects ({@link #gameStartLock}, {@link #turnLock}) so the TUI
 * can block until the relevant state change arrives.
 */
public class ClientVirtualView extends UnicastRemoteObject implements ClientRemoteInterface{
    /** Latest list of winning players; set when the game ends. */
    private  List<PlayerDTO> winners;
    /** The era the game is currently in. */
    private volatile ERA currentEra;
    /** The phase the game is currently in. */
    private volatile GAME_PHASE currentGamePhase;
    /** Nickname of the player whose turn it is to place their totem. */
    private volatile String playerToPlace;
    /** Nickname of the player whose turn it is to resolve actions. */
    private volatile String playerToPlay;
    /** Remaining top-row draw count for the current player's offer tile. */
    private volatile int drawTop;
    /** Remaining bottom-row draw count for the current player's offer tile. */
    private volatile int drawBot;

    private Map<Integer, List<String>> leaderboards;
    /** Map of all players in the game, keyed by nickname. */
    private final Map<String,PlayerDTO> playersMap= new ConcurrentHashMap<>();
    private final List<String> resolvedEvents =  new ArrayList<>();

    // MARKET DTO
    /** Current top card row of the market. */
    private  List<CardDTO> topCards;
    /** Current bottom card row of the market. */
    private  List<CardDTO> bottomCards;
    /** Current top building row of the market. */
    private  List<BuildingDTO> topBuildings;
    /** Current bottom building row of the market. */
    private  List<BuildingDTO> bottomBuildings;

    // BOARD DTO
    /** Current list of offer tiles on the board. */
    private  List<OffertileDTO> offerTileList;
    /** Current list of default tiles on the board. */
    private  List<DefaultTileDTO> defaultTileList;
    /** Maps offer tile position (0-based) to the nickname of the player currently on it. */
    private final Map<Integer, String> offerTileOccupants = new ConcurrentHashMap<>();
    /** Ordered list of players on the default tile (index = slot position). */
    private volatile List<PlayerDTO> defaultTileOrder = new ArrayList<>();

    // --- LOCKS ---
    /** Lock used to block the TUI until the game transitions from lobby to the first placing phase. */
    public final Object gameStartLock = new Object();
    /** {@code true} once the first {@link #gamePhaseChanged} with {@code PLACING_PHASE} is received. */
    public boolean isGameStarted = false;
    /** Set to {@code true} when the server sends an error; checked by the TUI after waking up. */
    public volatile boolean connectionError = false;
    /** The last error message received from the server, or {@code null} if none. */
    public volatile String lastErrorMessage = null;
    private final Object stateLock = new Object();
    /** Lock used to pause the TUI between turns; notified on phase change, turn change, or error. */
    public final Object turnLock = new Object();
    /** {@code true} when the server has asked this client to pick an extra card (draw-one-more effect). */
    public boolean needsExtraDraw = false;
    /** Top card row snapshot sent with the draw-one-more request; shows round-closing cards. */
    private List<CardDTO> extraDrawCards = new ArrayList<>();
    /** Top building row snapshot sent with the draw-one-more request; shows round-closing buildings. */
    private List<BuildingDTO> extraDrawBuildings = new ArrayList<>();
    // ----------------------------------------------------------------------
//  GUI integration. La TUI usa i lock; la GUI registra un osservatore qui.
//  Le callback dal server, oltre al solito notifyAll sui lock per la TUI,
//  chiameranno il metodo corrispondente sull'osservatore se presente.
//  L'osservatore GUI è responsabile di passare al thread JavaFX
//  (Platform.runLater) prima di toccare i nodi grafici.
// ----------------------------------------------------------------------
    private final java.util.concurrent.CopyOnWriteArrayList<GUIObserver> guiObservers = new java.util.concurrent.CopyOnWriteArrayList<>();

    public void addGUIObserver(GUIObserver observer) {
        guiObservers.add(observer);
    }

    public void removeGUIObserver(GUIObserver observer) {
        guiObservers.remove(observer);
    }

    private void updateObservers(java.util.function.Consumer<GUIObserver> action) {
        for (GUIObserver observer : guiObservers) {
            try {
                action.accept(observer);
            } catch (Throwable t) {
                System.out.println("[GUI observer error] " + t.getMessage());
            }
        }
    }

    // --- DISCONNECTION TRACKING ---
    /** Set of nicknames of players that have disconnected during the game. */
    private final Set<String> disconnectedPlayers = ConcurrentHashMap.newKeySet();
    /**
     * Queue of nicknames whose disconnection has not yet been displayed to the user.
     * The TUI drains this queue each iteration and prints a notification.
     */
    private final Queue<String> recentDisconnections = new ConcurrentLinkedQueue<>();
    /**
     * Queue of nicknames whose reconnection has not yet been displayed to the user.
     * The TUI drains this queue each iteration and prints a notification.
     */
    private final Queue<String> recentReconnections = new ConcurrentLinkedQueue<>();
    /** {@code true} when the server has been detected as unreachable. */
    public volatile boolean serverDead = false;

    /**
     * Creates a new client virtual view and exports it as an RMI remote object.
     *
     * @throws RemoteException if the RMI export fails.
     */
    public ClientVirtualView() throws RemoteException {
        super();
    }

    // --- GETTERS (Needed by the ClientApp to check whose turn it is) ---
    /**
     * Returns the current game phase.
     * @return the current {@link GAME_PHASE}.
     */
    public GAME_PHASE getGamePhase() { return currentGamePhase; }

    /**
     * Returns the nickname of the player who must place their totem next.
     * @return the placing player's nickname, or {@code null} if not set yet.
     */
    public String getPlayerToPlace() { return playerToPlace; }

    /**
     * Returns the nickname of the player who must resolve their actions next.
     * @return the playing player's nickname, or {@code null} if not set yet.
     */
    public String getPlayerToPlay() { return playerToPlay; }

    /**
     * Returns offer tile size.
     * @return the result of the operation.
     */
    public int getOfferTileSize(){
        synchronized (stateLock){
            return offerTileList.size();
        }

    }
    /**
     * Executes initialize game.
     * @param currentEra parameter currentEra.
     * @param gamePhase parameter gamePhase.
     * @param PlayerToPlace parameter PlayerToPlace.
     * @param PlayerToPlay parameter PlayerToPlay.
     */
    @Override
    public void initializeGame(ERA currentEra, GAME_PHASE gamePhase, String PlayerToPlace, String PlayerToPlay) throws RemoteException {
        this.currentEra=currentEra;
        this.currentGamePhase=gamePhase;
        this.playerToPlace=null;
        this.playerToPlay=null;
    }

    /**
     * Executes game winners.
     * @param playerDTOSWinner parameter playerDTOSWinner.
     */
    @Override
    public void gameWinners(List<PlayerDTO> playerDTOSWinner) throws RemoteException {
        synchronized (stateLock){
            this.winners=playerDTOSWinner;
        }
        synchronized (turnLock){
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onWinners(playerDTOSWinner));
    }

    /**
     * metodo getter per i vincitori
     * @return
     */

    public List<PlayerDTO> getWinners() {
        synchronized (stateLock){
            return winners;
        }
    }

    /**
     * Executes player added.
     * @param playerAdded parameter playerAdded.
     */
    @Override
    public void playerAdded(PlayerDTO playerAdded) throws RemoteException {
        this.playersMap.put(playerAdded.getNickName(),playerAdded);
    }

    /**
     * Executes era changed.
     * @param newEra parameter newEra.
     */
    @Override
    public void eraChanged(ERA newEra) throws RemoteException {
        this.currentEra=newEra;

    }

    /**
     * Executes game phase changed.
     * @param gamePhase parameter gamePhase.
     */
    @Override
    public void gamePhaseChanged(GAME_PHASE gamePhase) throws RemoteException {
        this.currentGamePhase=gamePhase;
        if (gamePhase == GAME_PHASE.PLACING_PHASE || gamePhase == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            offerTileOccupants.clear();
        }
        // Always unblock the lobby-wait: in normal flow PLACING_PHASE arrives first;
        // for a reconnecting client any phase means the game is already running.
        this.isGameStarted = true;
        synchronized (gameStartLock) {
            gameStartLock.notifyAll();
        }

        // Phase changes often mean a new turn mechanic is starting, wake up the UI to check
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onGamePhaseChanged(gamePhase));
    }

    /**
     * Executes player to place changed.
     * @param playerChanged parameter playerChanged.
     */
    @Override
    public void playerToPlaceChanged(PlayerDTO playerChanged) throws RemoteException {
        this.playerToPlace=playerChanged.getNickName();
        // The server just changed the placing player! Wake up the UI.
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onPlayerToPlaceChanged(playerChanged.getNickName()));
    }

    /**
     * Executes player to play changed.
     * @param playerChanged parameter playerChanged.
     */
    @Override
    public void playerToPlayChanged(PlayerDTO playerChanged) throws RemoteException {
        // FIXED: This used to be .toString(), changed it to .getNickName()
        this.playerToPlay=playerChanged.getNickName();
        // The server just changed the playing player! Wake up the UI.
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onPlayerToPlayChanged(playerChanged.getNickName()));
    }

    /**
     * Executes initialize market.
     * @param topCards parameter topCards.
     * @param bottomCards parameter bottomCards.
     * @param topBuildings parameter topBuildings.
     */
    @Override
    public void initializeMarket(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings) throws RemoteException {
        // Ensure that lists are mutable
        synchronized (stateLock){
            this.topCards = new ArrayList<>(topCards);
            this.bottomCards = new ArrayList<>(bottomCards);
            this.topBuildings = new ArrayList<>(topBuildings);
        }
        updateObservers(obs -> obs.onMarketInitialized(topCards, bottomCards, topBuildings));
    }

    /**
     * Executes added card to tribe.
     * @param nickname parameter nickname.
     * @param cardDTO parameter cardDTO.
     */
    @Override
    public void addedCardToTribe(String nickname, CardDTO cardDTO) throws RemoteException {
        PlayerDTO temp = playersMap.get(nickname);
        if (temp == null) {
            temp = new PlayerDTO(nickname, 0, 0, null);
        }

        temp.addCardToTribe(cardDTO);
        playersMap.put(nickname, temp);
        updateObservers(obs -> obs.onCardAddedToTribe(nickname, cardDTO));
    }

    /**
     * Executes top card removed.
     * @param position parameter position.
     */
    @Override
    public void topCardRemoved(int position) throws RemoteException {
        synchronized (stateLock){
            this.topCards.remove(position);
        }
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onTopCardRemoved(position));
    }

    /**
     * Executes top build removed.
     * @param position parameter position.
     */
    @Override
    public void topBuildRemoved(int position) throws RemoteException {
        synchronized (stateLock){
            this.topBuildings.remove(position);
        }
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onTopBuildRemoved(position));
    }

    /**
     * Executes bottom card removed.
     * @param position parameter position.
     */
    @Override
    public void bottomCardRemoved(int position) throws RemoteException {
        synchronized (stateLock){
            this.bottomCards.remove(position);
        }
        updateObservers(obs -> obs.onBottomCardRemoved(position));
    }

    /**
     * Executes bottom build removed.
     * @param position parameter position.
     */
    @Override
    public void bottomBuildRemoved(int position) throws RemoteException {
        synchronized (stateLock){
            this.bottomBuildings.remove(position);
        }
        updateObservers(obs -> obs.onBottomBuildRemoved(position));
    }

    /**
     * Executes top building refreshed.
     * @param topBuildingCards parameter topBuildingCards.
     */
    @Override
    public void topBuildingRefreshed(List<BuildingDTO> topBuildingCards) throws RemoteException {
        synchronized (stateLock){
            if (this.topBuildings != null) {
                this.bottomBuildings = new ArrayList<>(this.topBuildings);
            }
            this.topBuildings = new ArrayList<>(topBuildingCards);
        }
        updateObservers(obs -> obs.onTopBuildingRefreshed(topBuildingCards));
    }

    /**
     * Executes top card refreshed.
     * @param topCards parameter topCards.
     */
    @Override
    public void topCardRefreshed(List<CardDTO> topCards) throws RemoteException {
        synchronized (stateLock){
            if (this.topCards != null) {
                this.bottomCards = new ArrayList<>(this.topCards);
            }
            this.topCards = new ArrayList<>(topCards);
        }
        updateObservers(obs -> obs.onTopCardRefreshed(topCards));
    }

    /**
     * Executes player update food.
     * @param nickname parameter nickname.
     * @param food parameter food.
     */
    @Override
    public void playerUpdateFood(String nickname, int food) throws RemoteException {
        PlayerDTO temp = playersMap.get(nickname);
        temp.setFood(food);
        playersMap.put(temp.getNickName(),temp);
        updateObservers(obs -> obs.onPlayerFoodChanged(nickname, food));
    }

    /**
     * Executes player update pp.
     * @param nickname parameter nickname.
     * @param PP parameter PP.
     */
    @Override
    public void playerUpdatePP(String nickname, int PP) throws RemoteException {
        PlayerDTO temp = playersMap.get(nickname);
        temp.setPrestigePoint(PP);
        playersMap.put(temp.getNickName(),temp);
        updateObservers(obs -> obs.onPlayerPPChanged(nickname, PP));
    }

    /**
     * Executes board initialize.
     * @param offerTileList parameter offerTileList.
     * @param defaultTileList parameter defaultTileList.
     */
    @Override
    public void boardInitialize(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) throws RemoteException {
        synchronized (stateLock){
            this.offerTileList=offerTileList;
            this.defaultTileList=defaultTileList;
        }
        updateObservers(obs -> obs.onBoardInitialized(offerTileList, defaultTileList));
    }

    /**
     * Executes player placed on offertile.
     * @param PlayerNickname parameter PlayerNickname.
     * @param offertilePosition parameter offertilePosition.
     */
    @Override
    public void playerPlacedOnOffertile(String PlayerNickname, int offertilePosition) throws RemoteException {
        int fromSlot = -1;
        for (int i = 0; i < defaultTileOrder.size(); i++) {
            PlayerDTO p = defaultTileOrder.get(i);
            if (p != null && Objects.equals(p.getNickName(), PlayerNickname)) { fromSlot = i; break; }
        }
        defaultTileOrder.replaceAll(p -> p != null && Objects.equals(p.getNickName(), PlayerNickname) ? null : p);
        offerTileOccupants.put(offertilePosition, PlayerNickname);
        List<PlayerDTO> updatedOrder = new ArrayList<>(defaultTileOrder);
        final int slot = fromSlot;
        updateObservers(obs -> obs.onDefaultTileOrderChanged(updatedOrder));
        updateObservers(obs -> obs.onPlayerPlacedOnOfferTile(PlayerNickname, offertilePosition, slot));
    }


    /**
     * Returns the current list of offer tiles on the board.
     * @return the offer tile list.
     */
    public List<OffertileDTO> getOfferTileList() {
        synchronized (stateLock) {
            return offerTileList;
        }
    }

    /**
     * Returns the current list of default tiles on the board.
     * @return the default tile list.
     */
    public List<DefaultTileDTO> getDefaultTileList() {
        synchronized (stateLock) {
            return defaultTileList;
        }
    }

    /**
     * Returns a map from offer tile position (0-based) to the nickname of the player on it.
     * @return the occupants map.
     */
    public Map<Integer, String> getOfferTileOccupants() {
        return new HashMap<>(offerTileOccupants);
    }

    /**
     * Executes order on default tile.
     * @param orderOnDefaultTile parameter orderOnDefaultTile.
     */
    @Override
    public void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile) throws RemoteException {
        this.defaultTileOrder = new ArrayList<>(orderOnDefaultTile);
        List<PlayerDTO> snapshot = new ArrayList<>(orderOnDefaultTile);
        updateObservers(obs -> obs.onDefaultTileOrderChanged(snapshot));
    }

    /**
     * Returns the current ordered list of players on the default tile (index = slot position).
     * @return ordered player list.
     */
    public List<PlayerDTO> getDefaultTileOrder() {
        return new ArrayList<>(defaultTileOrder);
    }

    /**
     * Executes ask extra draw, storing the end-of-round market snapshot for the TUI.
     */
    @Override
    public void askExtraDraw(List<CardDTO> snapshotCards, List<BuildingDTO> snapshotBuildings) throws RemoteException {
        synchronized (turnLock) {
            this.extraDrawCards = new ArrayList<>(snapshotCards);
            this.extraDrawBuildings = new ArrayList<>(snapshotBuildings);
            this.needsExtraDraw = true;
            turnLock.notifyAll(); // Wake up the TUI!
        }
        List<CardDTO> cardsCopy = new ArrayList<>(snapshotCards);
        List<BuildingDTO> buildingsCopy = new ArrayList<>(snapshotBuildings);
        updateObservers(obs -> obs.onAskExtraDraw(cardsCopy, buildingsCopy));
    }

    /** Returns the top card row snapshot for the current extra draw request. */
    public List<CardDTO> getExtraDrawCards() {
        synchronized (stateLock) {
            return extraDrawCards == null ? new ArrayList<>() : new ArrayList<>(extraDrawCards);
        }
    }

    /** Returns the top building row snapshot for the current extra draw request. */
    public List<BuildingDTO> getExtraDrawBuildings() {
        synchronized (stateLock) {
            return extraDrawBuildings == null ? new ArrayList<>() : new ArrayList<>(extraDrawBuildings);
        }
    }

    /** Returns the size of the extra draw card snapshot. */
    public int getExtraDrawCardSize() {
        synchronized (stateLock) {
            return extraDrawCards == null ? 0 : extraDrawCards.size();
        }
    }

    /** Returns the size of the extra draw building snapshot. */
    public int getExtraDrawBuildingSize() {
        synchronized (stateLock) {
            return extraDrawBuildings == null ? 0 : extraDrawBuildings.size();
        }
    }

    /**
     * Executes action available changed.
     * @param action parameter action.
     */
    @Override
    public void actionAvailableChanged(ActionDTO action) throws RemoteException {
        synchronized (turnLock){
            this.drawBot=action.getDrawBot();
            this.drawTop=action.getDrawTop();
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onActionAvailableChanged(action.getDrawTop(), action.getDrawBot()));

    }

    /**
     * Receives an error message from the server, stores it, and wakes up any waiting threads.
     * @param message the error description sent by the server.
     */
    @Override
    public void showErrorMessage(String message) throws RemoteException {
        this.lastErrorMessage = message;
        synchronized (gameStartLock) {
            this.connectionError = true;
            gameStartLock.notifyAll();
        }
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
    }
    /**
     * Returns top card size.
     * @return the result of the operation.
     */
    public int getTopCardSize() {
        synchronized (stateLock){
            if (this.topCards == null) return 0;
            return this.topCards.size();
        }

    }

    /**
     * Returns bottom card size.
     * @return the result of the operation.
     */
    public int getBottomCardSize() {
        synchronized (stateLock){
            if (this.bottomCards == null) return 0;
            return this.bottomCards.size();
        }

    }

    /**
     * Returns top building size.
     * @return the result of the operation.
     */
    public int getTopBuildingSize() {
        synchronized (stateLock){
            if (this.topBuildings == null) return 0;
            return this.topBuildings.size();
        }

    }

    /**
     * Returns bottom building size.
     * @return the result of the operation.
     */
    public int getBottomBuildingSize() {
        synchronized (stateLock){
            if (this.bottomBuildings == null) return 0;
            return this.bottomBuildings.size();
        }

    }

    /**
     * Returns top card type.
     * @param position parameter position.
     * @return the result of the operation.
     */
    public CARD_TYPE getTopCardType(int position){
        synchronized (stateLock){
            return this.topCards.get(position).getCardType();
        }

    }
    /**
     * Returns bottom card type.
     * @param position parameter position.
     * @return the result of the operation.
     */
    public CARD_TYPE getBottomCardType(int position){
        synchronized (stateLock){
            return this.bottomCards.get(position).getCardType();
        }

    }

    /**
     * Returns an unmodifiable snapshot of all players currently in the game,
     * sorted alphabetically by nickname.
     * @return list of {@link PlayerDTO} snapshots.
     */
    public List<PlayerDTO> getPlayers() {
        return playersMap.values().stream()
                .sorted(Comparator.comparing(PlayerDTO::getNickName))
                .collect(Collectors.toList());
    }

    /**
     * Returns draw bot.
     * @return the result of the operation.
     */
    public int getDrawBot() {
        return drawBot;
    }
    /**
     * Returns draw top.
     * @return the result of the operation.
     */
    public int getDrawTop(){
        return drawTop;
    }

    /**
     * Returns top cards.
     * @return the result of the operation.
     */
    public List<CardDTO> getTopCards() {
        synchronized (stateLock){
            return (this.topCards == null) ? new ArrayList<>() : this.topCards;
        }

    }

    /**
     * Returns bottom cards.
     * @return the result of the operation.
     */
    public List<CardDTO> getBottomCards() {
        synchronized (stateLock){
            return (this.bottomCards == null) ? new ArrayList<>() : this.bottomCards;
        }

    }

    /**
     * Returns top buildings.
     * @return the result of the operation.
     */
    public List<BuildingDTO> getTopBuildings() {
        synchronized (stateLock){
            return (this.topBuildings == null) ? new ArrayList<>() : this.topBuildings;
        }

    }

    /**
     * Returns bottom buildings.
     * @return the result of the operation.
     */
    public List<BuildingDTO> getBottomBuildings() {
        synchronized (stateLock){
            return (this.bottomBuildings == null) ? new ArrayList<>() : this.bottomBuildings;
        }

    }

    @Override
    public void eventResolved(int eventID, EVENT_TYPE eventType) throws RemoteException {
        String description = "Evento #" + eventID + " (" + eventType + ") risolto";
        synchronized (stateLock){
            resolvedEvents.add(description);
        }
        synchronized (turnLock){
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onEventResolved(eventID, eventType));
    }

    @Override
    public void sendRank(Map<Integer, List<String>> leaderboards) throws
            RemoteException {
        synchronized (stateLock) {
            this.leaderboards = leaderboards;
        }
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
    }

    public Map<Integer, List<String>> getLeaderboards() {
        synchronized (stateLock) {
            return leaderboards;
        }
    }

    public void clearLeaderboards() {
        synchronized (stateLock) {
            this.leaderboards = null;
        }
    }

    public List<String> getResolvedEvents() {
        synchronized (stateLock){
            return new ArrayList<>(resolvedEvents);
        }
    }

    public void clearResolvedEvents() {
        synchronized (stateLock){
            resolvedEvents.clear();
        }
    }

    // --- DISCONNECTION ---

    /**
     * Called by the server (via RMI stub or Socket proxy) when a player has disconnected.
     * Adds the player to the disconnected set and wakes up the TUI turn lock.
     * @param nickname the disconnected player's nickname.
     * @throws RemoteException if the RMI call fails.
     */
    @Override
    public void playerDisconnected(String nickname) throws RemoteException {
        disconnectedPlayers.add(nickname);
        recentDisconnections.add(nickname);   // TUI will drain and display this
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onPlayerDisconnected(nickname));
    }

    @Override
    public void playerReconnected(String nickname) throws RemoteException {
        disconnectedPlayers.remove(nickname);
        recentReconnections.add(nickname);
        updateObservers(obs -> obs.onPlayerReconnected(nickname));
    }

    /**
     * Returns {@code true} if the given player is known to have disconnected.
     * @param nickname the player's nickname.
     * @return whether the player is disconnected.
     */
    public boolean isPlayerDisconnected(String nickname) {
        return disconnectedPlayers.contains(nickname);
    }

    /**
     * Drains and returns all player nicknames whose disconnection has not yet
     * been displayed to the user. Each nickname appears at most once.
     * @return list of recently-disconnected player nicknames (may be empty).
     */
    public List<String> drainRecentDisconnections() {
        List<String> result = new ArrayList<>();
        String n;
        while ((n = recentDisconnections.poll()) != null) {
            result.add(n);
        }
        return result;
    }

    /**
     * Drains and returns all player nicknames whose reconnection has not yet been
     * displayed to the user. Each nickname appears at most once.
     * @return list of recently-reconnected player nicknames (may be empty).
     */
    public List<String> drainRecentReconnections() {
        List<String> result = new ArrayList<>();
        String n;
        while ((n = recentReconnections.poll()) != null) {
            result.add(n);
        }
        return result;
    }

    /**
     * Called when the server becomes unreachable (socket drop, RMI timeout, etc.).
     * Sets the {@code serverDead} flag and wakes up any waiting TUI threads so they
     * can exit cleanly.
     */
    public void handleServerDeath() {
        this.serverDead = true;
        synchronized (gameStartLock) {
            gameStartLock.notifyAll();
        }
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
        updateObservers(obs -> obs.onServerDead());
    }

    /**
     * Returns {@code true} if the server has been detected as unreachable.
     * @return server-dead flag.
     */
    public boolean isServerDead() {
        return serverDead;
    }

}