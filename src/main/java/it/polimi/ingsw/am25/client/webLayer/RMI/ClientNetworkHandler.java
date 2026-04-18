package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClientNetworkHandler extends UnicastRemoteObject implements ClientRemoteInterface {

    public ClientNetworkHandler() throws RemoteException {
        super();
    }

    @Override
    public RemoteRef getRef() {
        return super.getRef();
    }

    @Override
    public void initializeGame(ERA currentEra, GAME_PHASE gamePhase, String PlayerToPlace, String PlayerToPlay) throws RemoteException {

    }

    @Override
    public void gameWinners(List<PlayerDTO> playerDTOSWinner) throws RemoteException {

    }

    @Override
    public void playerAdded(PlayerDTO playerAdded) throws RemoteException {

    }

    @Override
    public void EraChanged(ERA newEra) throws RemoteException {

    }

    @Override
    public void GamePhaseChanged(GAME_PHASE gamePhase) throws RemoteException {

    }

    @Override
    public void playerToPlaceChanged(PlayerDTO playerChanged) throws RemoteException {

    }

    @Override
    public void playerToPlayChanged(PlayerDTO playerChanged) throws RemoteException {

    }

    @Override
    public void initializeMarket(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings) throws RemoteException {

    }

    @Override
    public void topCardRemoved(int position) throws RemoteException {

    }

    @Override
    public void topBuildRemoved(int position) throws RemoteException {

    }

    @Override
    public void bottomCardRemoved(int position) throws RemoteException {

    }

    @Override
    public void bottomBuildRemoved(int position) throws RemoteException {

    }

    @Override
    public void topCardRefreshed(List<CardDTO> topCards) throws RemoteException {

    }

    @Override
    public void topBuildingRefreshed(List<BuildingDTO> topBuildingCards) throws RemoteException {

    }

    @Override
    public void playerUpdateFood(String nickname, int food) throws RemoteException {

    }

    @Override
    public void playerUpdatePP(String nickname, int PP) throws RemoteException {

    }

    @Override
    public void boardInitialize(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) throws RemoteException {

    }

    @Override
    public void playerPlacedOnOffertile(String PlayerNickname, int offertilePosition) throws RemoteException {

    }

    @Override
    public void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile) throws RemoteException {

    }
}
