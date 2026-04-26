package it.polimi.ingsw.am25.client.webLayer.Socket;

import it.polimi.ingsw.am25.client.Utilities.ClientUtilitiesFunction;
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
    private static final String LOG_PREFIX = "[CLIENT][SOCKET_PROXY]";

    private final ObjectOutputStream out;

    /**
     * Creates a new proxy that forwards calls to the server via the given output stream.
     * @param out the output stream connected to the server socket.
     */
    public ServerSocketProxy(ObjectOutputStream out) {
        this.out = out;
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "ServerSocketProxy initialized.");
    }

    /**
     * Sends a placing-player request to the server.
     * @param playerToPlace the player who is placing.
     * @param position the tile position chosen.
     */
    @Override
    public void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException {
        try {
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending placingPlayer request for " + playerToPlace.getNickName() + " at position " + position + ".");
            out.writeObject(new PlacingPlayerMessage(playerToPlace, position));
            out.flush();
        } catch (IOException e) {
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Network error in placingPlayer: " + e.getMessage());
            throw new RemoteException("Network error: could not send placing request to server.", e);
        }
    }

    /**
     * Sends a request to draw a card from the top market list.
     * @param player the acting player.
     * @param cardType the type of card to draw.
     * @param position the position in the top list.
     */
    @Override
    public void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, ActionNotAvailable {
        try {
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending selectCardFromTopList request for " + player.getNickName() + ", type " + cardType + ", position " + position + ".");
            out.writeObject(new SelectCardFromTopListMessage(player, cardType, position));
            out.flush();
        } catch (IOException e) {
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Network error in selectCardFromTopList: " + e.getMessage());
            throw new RemoteException("Network error: could not send top card selection to server.", e);
        }
    }

    /**
     * Sends a request to draw a card from the bottom market list.
     * @param player the acting player.
     * @param cardType the type of card to draw.
     * @param position the position in the bottom list.
     */
    @Override
    public void selectCardFromBottomList(PlayerDTO player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, RemoteException {
        try {
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending selectCardFromBottomList request for " + player.getNickName() + ", type " + cardType + ", position " + position + ".");
            out.writeObject(new SelectCardFromBottomListMessage(player, cardType, position));
            out.flush();
        } catch (IOException e) {
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Network error in selectCardFromBottomList: " + e.getMessage());
            throw new RemoteException("Network error: could not send bottom card selection to server.", e);
        }
    }

    /**
     * Sends a request to draw an extra card.
     * @param player the acting player.
     * @param cardType the type of card to draw.
     * @param position the position in the list.
     */
    @Override
    public void selectExtraCard(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        try {
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending selectExtraCard request for " + player.getNickName() + ", type " + cardType + ", position " + position + ".");
            out.writeObject(new SelectExtraCardMessage(player, cardType, position));
            out.flush();
        } catch (IOException e) {
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Errore rete in selectExtraCard: " + e.getMessage());
            throw new RemoteException("Errore di rete: impossibile pescare la carta extra.", e);
        }
    }

    /**
     * Sends a request indicating the player skips their turn.
     * @param playerDTO the player who does nothing.
     */
    @Override
    public void playerDoNothing(PlayerDTO playerDTO) throws Exception {
        try {
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending playerDoNothing request for " + playerDTO.getNickName() + ".");
            out.writeObject(new PlayerDoNothingMessage(playerDTO));
            out.flush();
        } catch (IOException e) {
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Network error in playerDoNothing: " + e.getMessage());
            throw new RemoteException("Network error: could not send skip-turn request to server.", e);
        }
    }

    /**
     * Sends a request to add the player to the current lobby.
     * The {@code clientRemoteInterface} is not serialized — the server already has the socket reference.
     * @param playerDTO the player joining the game.
     * @param clientRemoteInterface ignored for socket transport.
     */
    @Override
    public void addPlayer(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameFullException, GameReadyToStartException, NameOrColorAlreadyTakenException {
        try {
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending addPlayer request for " + playerDTO.getNickName() + ".");
            out.writeObject(new AddPlayerMessage(playerDTO));
            out.flush();
        } catch (IOException e) {
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Network error in addPlayer: " + e.getMessage());
            throw new RemoteException("Network error: could not send join-game request to server.", e);
        }
    }

    /**
     * Sends a request to create a new game.
     * The {@code clientRemoteInterface} is not serialized — the server already has the socket reference.
     * @param playerHost the hosting player.
     * @param PlayerNumber the required number of players.
     * @param clientRemoteInterface ignored for socket transport.
     */
    @Override
    public void createGame(PlayerDTO playerHost, int PlayerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException, IllegalStateException {
        try {
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending createGame request for host " + playerHost.getNickName() + " with " + PlayerNumber + " players.");
            out.writeObject(new CreateGameMessage(playerHost, PlayerNumber));
            out.flush();
        } catch (IOException e) {
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Network error in createGame: " + e.getMessage());
            throw new RemoteException("Network error: could not send create-game request to server.", e);
        }
    }
}
