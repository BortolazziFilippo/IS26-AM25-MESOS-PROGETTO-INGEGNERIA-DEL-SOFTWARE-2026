package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientVirtualView extends UnicastRemoteObject implements ClientRemoteInterface{
    private  List<PlayerDTO> winners;
    private volatile ERA currentEra;
    private volatile GAME_PHASE currentGamePhase;
    private volatile String playerToPlace;
    private volatile String playerToPlay;
    private volatile int drawTop;
    private volatile int drawBot;
    private final Map<String,PlayerDTO> playersMap= new ConcurrentHashMap<>();

    // MARKET DTO
    private  List<CardDTO> topCards;
    private  List<CardDTO> bottomCards;
    private  List<BuildingDTO> topBuildings;
    private  List<BuildingDTO> bottomBuildings;

    // BOARD DTO
    private  List<OffertileDTO> offerTileList;
    private  List<DefaultTileDTO> defaultTileList;

    // --- LOCKS ---
    public final Object gameStartLock = new Object();
    public boolean isGameStarted = false;
    public volatile boolean connectionError=false;
    private final Object stateLock=new Object();

    // We use this lock to pause the player when it's not their turn!
    public final Object turnLock = new Object();
    public boolean needsExtraDraw = false;

    /**
     * Creates a new client virtual view instance.
     */
    public ClientVirtualView() throws RemoteException {
        super();
    }

    // --- GETTERS (Needed by the ClientApp to check whose turn it is) ---
    public GAME_PHASE getGamePhase() { return currentGamePhase; }
    public String getPlayerToPlace() { return playerToPlace; }
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
        if (gamePhase == GAME_PHASE.PLACING_PHASE) {
            this.isGameStarted = true;
            synchronized (gameStartLock) {
                gameStartLock.notifyAll();
            }
        }

        // Phase changes often mean a new turn mechanic is starting, wake up the UI to check
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
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

    }

    /**
     * Executes player placed on offertile.
     * @param PlayerNickname parameter PlayerNickname.
     * @param offertilePosition parameter offertilePosition.
     */
    @Override
    public void playerPlacedOnOffertile(String PlayerNickname, int offertilePosition) throws RemoteException {
    }

    /**
     * Executes order on default tile.
     * @param orderOnDefaultTile parameter orderOnDefaultTile.
     */
    @Override
    public void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile) throws RemoteException {
    }

    /**
     * Executes ask extra draw.
     */
    @Override
    public void askExtraDraw() throws RemoteException {
        synchronized (turnLock) {
            this.needsExtraDraw = true;
            turnLock.notifyAll(); // Wake up the TUI!
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


    }

    @Override
    public void showErrorMessage(String message) throws RemoteException {
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
}