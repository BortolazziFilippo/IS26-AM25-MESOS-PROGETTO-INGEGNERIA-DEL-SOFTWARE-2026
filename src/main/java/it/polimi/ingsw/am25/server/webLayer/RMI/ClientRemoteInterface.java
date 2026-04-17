package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientRemoteInterface extends Remote {
    //Methods update game:
    void initializeGame(ERA currentEra, GAME_PHASE gamePhase,String PlayerToPlace,String PlayerToPlay) throws RemoteException;
    void gameWinners(List<PlayerDTO> playerDTOSWinner)throws RemoteException;
    void playerAdded(PlayerDTO playerAdded) throws RemoteException;
    void EraChanged(ERA newEra)throws RemoteException;
    void GamePhaseChanged(GAME_PHASE gamePhase)throws RemoteException;
    void playerToPlaceChanged(PlayerDTO playerChanged)throws RemoteException;
    void playerToPlayChanged(PlayerDTO playerChanged)throws RemoteException;
    //Methods update Market;
    void initializeMarket(List<Card> topCards, List<Card> bottomCards, List<BuildingCard> topBuildings)throws RemoteException;
    void topCardRemoved(int position)throws RemoteException;
    void topBuildRemoved(int position)throws RemoteException;
    void bottomCardRemoved(int postion)throws RemoteException;
    void bottomBuildRemoved(int position)throws RemoteException;
    void topCardRefreshed(List<Card> topCards)throws RemoteException;
    void topBuildingRefreshed(List<Card> topBuildingCards)throws RemoteException;
    //Methods update player
    void playerUpdateFood(String nickname, int food)throws RemoteException;
    void playerUpdatePP(String nickname, int PP)throws RemoteException;
    //method update board
    void boardInitialize(List<OfferTile> offerTileList, List<DefaultTile> defaultTileList)throws RemoteException;
    void playerPlacedOnOffertile(String PlayerNickname,int offertilePosition)throws RemoteException;
    void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile)throws RemoteException;



}
