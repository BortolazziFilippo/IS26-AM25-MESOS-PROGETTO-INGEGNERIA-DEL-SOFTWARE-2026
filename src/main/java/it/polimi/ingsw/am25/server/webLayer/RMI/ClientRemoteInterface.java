package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.SimpleTimeZone;

public interface ClientRemoteInterface extends Remote {
    // Game updates
    void initializeGame(ERA currentEra, GAME_PHASE gamePhase,String PlayerToPlace,String PlayerToPlay) throws RemoteException;
    void gameWinners(List<PlayerDTO> playerDTOSWinner)throws RemoteException;
    void playerAdded(PlayerDTO playerAdded) throws RemoteException;
    void eraChanged(ERA newEra)throws RemoteException;
    void gamePhaseChanged(GAME_PHASE gamePhase)throws RemoteException;
    void playerToPlaceChanged(PlayerDTO playerChanged)throws RemoteException;
    void playerToPlayChanged(PlayerDTO playerChanged)throws RemoteException;
    void actionAvailableChanged(ActionDTO action) throws RemoteException;
    // Market updates
    void initializeMarket(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings)throws RemoteException;
    void topCardRemoved(int position)throws RemoteException;
    void topBuildRemoved(int position)throws RemoteException;
    void bottomCardRemoved(int position)throws RemoteException;
    void bottomBuildRemoved(int position)throws RemoteException;
    void topCardRefreshed(List<CardDTO> topCards)throws RemoteException;
    void topBuildingRefreshed(List<BuildingDTO> topBuildingCards)throws RemoteException;
    // Player updates
    void playerUpdateFood(String nickname, int food)throws RemoteException;
    void playerUpdatePP(String nickname, int PP)throws RemoteException;
    void addedCardToTribe(String nickname, CardDTO cardDTO)throws RemoteException;
    // Board updates
    void boardInitialize(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList)throws RemoteException;
    void playerPlacedOnOffertile(String PlayerNickname,int offertilePosition)throws RemoteException;
    void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile)throws RemoteException;

    // draw one more card
    void askExtraDraw() throws RemoteException;

    void showErrorMessage(String errorMessage) throws RemoteException;

}
