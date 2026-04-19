package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRemoteInterface extends Remote {
    void createGame(PlayerDTO playerHost, int PlayerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException,IllegalStateException;
    void addPlayer(PlayerDTO playerDTO,ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameFullException,GameReadyToStartException,NameOrColorAlreadyTakenException;
    void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException;
    void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position)throws RemoteException,IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException;
    void selectCardFromBottomList(PlayerDTO player,CARD_TYPE cardType, int position)throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException,RemoteException;
    void playerDoNothing(PlayerDTO playerDTO) throws RuntimeException,Exception;

}
