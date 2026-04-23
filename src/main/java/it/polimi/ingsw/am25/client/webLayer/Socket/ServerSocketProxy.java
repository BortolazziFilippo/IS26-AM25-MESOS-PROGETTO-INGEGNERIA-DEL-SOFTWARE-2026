package it.polimi.ingsw.am25.client.webLayer.Socket;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.messages.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

public class ServerSocketProxy implements ServerRemoteInterface {

    private final ObjectOutputStream out;

    public ServerSocketProxy(ObjectOutputStream out) {
        this.out = out;
    }

    @Override
    public void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException {
        try {
            out.writeObject(new PlacingPlayerMessage(playerToPlace, position));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException("Errore di rete: impossibile comunicare il piazzamento al server.", e);
        }
    }

    @Override
    public void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, ActionNotAvailable {
        try {
            out.writeObject(new SelectCardFromTopListMessage(player, cardType, position));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException("Errore di rete: impossibile pescare la carta superiore.", e);
        }
    }

    @Override
    public void selectCardFromBottomList(PlayerDTO player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, RemoteException {
        try {
            out.writeObject(new SelectCardFromBottomListMessage(player, cardType, position));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException("Errore di rete: impossibile pescare la carta inferiore.", e);
        }
    }

    @Override
    public void selectExtraCard(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        try {
            out.writeObject(new SelectExtraCardMessage(player, cardType, position));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException("Errore di rete: impossibile pescare la carta extra.", e);
        }
    }

    @Override
    public void playerDoNothing(PlayerDTO playerDTO) throws Exception {
        try {
            out.writeObject(new PlayerDoNothingMessage(playerDTO));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException("Errore di rete: impossibile passare il turno.", e);
        }
    }

    @Override
    public void addPlayer(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameFullException, GameReadyToStartException, NameOrColorAlreadyTakenException {
        try {
            // FONDAMENTALE: clientRemoteInterface NON viene passato nel costruttore del messaggio!
            out.writeObject(new AddPlayerMessage(playerDTO));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException("Errore di rete: impossibile unirsi alla partita.", e);
        }
    }

    @Override
    public void createGame(PlayerDTO playerHost, int PlayerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException, IllegalStateException {
        try {
            // FONDAMENTALE: clientRemoteInterface NON viene passato nel costruttore del messaggio!
            out.writeObject(new CreateGameMessage(playerHost, PlayerNumber));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException("Errore di rete: impossibile creare la partita.", e);
        }
    }
}