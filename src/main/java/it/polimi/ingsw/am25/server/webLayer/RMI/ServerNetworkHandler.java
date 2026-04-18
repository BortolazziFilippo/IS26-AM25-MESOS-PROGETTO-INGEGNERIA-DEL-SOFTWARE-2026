package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.EmptyMarketException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotEnoughFoodException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotSelectableCardException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.TileOccupiedException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

public class ServerNetworkHandler extends UnicastRemoteObject implements ServerRemoteInterface {

    public ServerNetworkHandler() throws RemoteException{
        super();
    }

    @Override
    public void createGame(PlayerDTO playerHost, int PlayerNumber) throws RemoteException, IllegalStateException {

    }

    @Override
    public void addPlayer(PlayerDTO playerDTO) throws RemoteException {

    }

    @Override
    public void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException {

    }

    @Override
    public void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {

    }

    @Override
    public void selectCardFromBottomList(PlayerDTO player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, RemoteException {

    }

    @Override
    public void playerDoNothing(PlayerDTO playerDTO) throws RuntimeException, Exception {

    }
}
