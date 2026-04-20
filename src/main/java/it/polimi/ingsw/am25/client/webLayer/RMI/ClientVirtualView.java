package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientVirtualView extends UnicastRemoteObject implements ClientRemoteInterface{
    private List<PlayerDTO> winners;
    private ERA currentEra;
    private GAME_PHASE currentGamePhase;
    private String playerToPlace;
    private String playerToPlay;

    Map<String,PlayerDTO> playersMap= new HashMap<>();

    // MARKET DTO
    private List<CardDTO> topCards;
    private List<CardDTO> bottomCards;
    private List<BuildingDTO> topBuildings;
    private List<BuildingDTO> bottomBuildings;

    // BOARD DTO
    private List<OffertileDTO> offerTileList;
    private List<DefaultTileDTO> defaultTileList;

    // --- LOCKS ---
    public final Object gameStartLock = new Object();
    public boolean isGameStarted = false;

    // We use this lock to pause the player when it's not their turn!
    public final Object turnLock = new Object();

    public ClientVirtualView() throws RemoteException {
        super();
    }

    // --- GETTERS (Needed by the ClientApp to check whose turn it is) ---
    public GAME_PHASE getGamePhase() { return currentGamePhase; }
    public String getPlayerToPlace() { return playerToPlace; }
    public String getPlayerToPlay() { return playerToPlay; }

    public int getOfferTileSize(){
        return offerTileList.size();
    }
    @Override
    public void initializeGame(ERA currentEra, GAME_PHASE gamePhase, String PlayerToPlace, String PlayerToPlay) throws RemoteException {
        this.currentEra=currentEra;
        this.currentGamePhase=gamePhase;
        this.playerToPlace=null;
        this.playerToPlay=null;
    }

    @Override
    public void gameWinners(List<PlayerDTO> playerDTOSWinner) throws RemoteException {
        this.winners=playerDTOSWinner;
    }

    @Override
    public void playerAdded(PlayerDTO playerAdded) throws RemoteException {
        this.playersMap.put(playerAdded.getNickName(),playerAdded);
    }

    @Override
    public void eraChanged(ERA newEra) throws RemoteException {
        this.currentEra=newEra;
    }

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

    @Override
    public void playerToPlaceChanged(PlayerDTO playerChanged) throws RemoteException {
        this.playerToPlace=playerChanged.getNickName();
        // The server just changed the placing player! Wake up the UI.
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
    }

    @Override
    public void playerToPlayChanged(PlayerDTO playerChanged) throws RemoteException {
        // FIXED: This used to be .toString(), changed it to .getNickName()
        this.playerToPlay=playerChanged.getNickName();
        // The server just changed the playing player! Wake up the UI.
        synchronized (turnLock) {
            turnLock.notifyAll();
        }
    }

    @Override
    public void initializeMarket(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings) throws RemoteException {
        System.out.println("CHiamato metodo initialize market");
        this.topCards=topCards;
        this.bottomCards=bottomCards;
        this.topBuildings=topBuildings;
    }

    @Override
    public void topCardRemoved(int position) throws RemoteException {
        this.topCards.remove(position);
    }

    @Override
    public void topBuildRemoved(int position) throws RemoteException {
        this.topBuildings.remove(position);
    }

    @Override
    public void bottomCardRemoved(int position) throws RemoteException {
        this.bottomCards.remove(position);
    }

    @Override
    public void bottomBuildRemoved(int position) throws RemoteException {
        this.bottomBuildings.remove(position);
    }

    @Override
    public void topBuildingRefreshed(List<BuildingDTO> topBuildingCards) throws RemoteException {
        this.bottomBuildings=List.copyOf(this.topBuildings);
        this.topBuildings=topBuildingCards;
    }

    @Override
    public void topCardRefreshed(List<CardDTO> topCards) throws RemoteException {
        this.bottomCards=List.copyOf(topCards);
        this.topCards=topCards;
    }

    @Override
    public void playerUpdateFood(String nickname, int food) throws RemoteException {
        PlayerDTO temp = playersMap.get(nickname);
        temp.setFood(food);
        playersMap.put(temp.getNickName(),temp);
    }

    @Override
    public void playerUpdatePP(String nickname, int PP) throws RemoteException {
        PlayerDTO temp = playersMap.get(nickname);
        temp.setPrestigePoint(PP);
        playersMap.put(temp.getNickName(),temp);
    }

    @Override
    public void boardInitialize(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) throws RemoteException {
        this.offerTileList=offerTileList;
        this.defaultTileList=defaultTileList;
    }

    @Override
    public void playerPlacedOnOffertile(String PlayerNickname, int offertilePosition) throws RemoteException {
    }

    @Override
    public void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile) throws RemoteException {
    }
}