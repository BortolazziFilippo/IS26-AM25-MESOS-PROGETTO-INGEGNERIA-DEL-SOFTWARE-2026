package it.polimi.ingsw.am25.client.webLayer.Socket;

import it.polimi.ingsw.am25.client.Utilities.ClientUtilitiesFunction;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.messages.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

/**
 * Client-side proxy that implements {@link ServerRemoteInterface} over a Socket connection.
 * Each call is serialized as a {@link ClientToServerMessage} and sent to the server,
 * allowing the client to treat Socket and RMI connections identically.
 *
 * <p>All writes go through the private {@link #send(ClientToServerMessage)} helper, which
 * synchronizes on the output stream to prevent concurrent writes from corrupting the stream
 * and calls {@link ObjectOutputStream#reset()} to prevent Java's object-reference cache
 * from sending stale data.
 */
public class ServerSocketProxy implements ServerRemoteInterface {
    private static final String LOG_PREFIX = "[CLIENT][SOCKET_PROXY]";

    private final ObjectOutputStream out;
    private final ClientVirtualView clientHandler;

    /**
     * Creates a new proxy that forwards calls to the server via the given output stream.
     *
     * @param out           the output stream connected to the server socket.
     * @param clientHandler the local client view used to wait for server confirmations.
     */
    public ServerSocketProxy(ObjectOutputStream out, ClientVirtualView clientHandler) {
        this.out = out;
        this.clientHandler = clientHandler;
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "ServerSocketProxy initialized.");
    }

    // -------------------------------------------------------------------------
    // Private helper — ALL writes must go through here
    // -------------------------------------------------------------------------

    /**
     * Serializes a message to the server socket stream.
     * Synchronizes on the output stream to prevent concurrent write corruption,
     * and resets the stream to avoid Java's serialization reference cache re-sending
     * stale object graphs.
     *
     * @param message the message to send.
     * @throws RemoteException if any I/O error occurs.
     */
    private void send(ClientToServerMessage message) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(message);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            throw new RemoteException("Network error: could not send message to server.", e);
        }
    }

    // -------------------------------------------------------------------------
    // ServerRemoteInterface implementation
    // -------------------------------------------------------------------------

    /**
     * Sends a heartbeat ping to the server so it knows this client is still alive.
     *
     * @param player the calling player (identifies the sender to the server).
     */
    @Override
    public void ping(PlayerDTO player) throws RemoteException {
        send(new PingMessage(player));
    }

    /**
     * Sends a placing-player request to the server.
     *
     * @param playerToPlace the player who is placing.
     * @param position      the tile position chosen.
     */
    @Override
    public void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending placingPlayer request for " + playerToPlace.getNickName() + " at position " + position + ".");
        send(new PlacingPlayerMessage(playerToPlace, position));
    }

    /**
     * Sends a request to draw a card from the top market list.
     *
     * @param player   the acting player.
     * @param cardType the type of card to draw.
     * @param position the position in the top list.
     */
    @Override
    public void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, ActionNotAvailable {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending selectCardFromTopList request for " + player.getNickName() + ", type " + cardType + ", position " + position + ".");
        send(new SelectCardFromTopListMessage(player, cardType, position));
    }

    /**
     * Sends a request to draw a card from the bottom market list.
     *
     * @param player   the acting player.
     * @param cardType the type of card to draw.
     * @param position the position in the bottom list.
     */
    @Override
    public void selectCardFromBottomList(PlayerDTO player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, RemoteException {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending selectCardFromBottomList request for " + player.getNickName() + ", type " + cardType + ", position " + position + ".");
        send(new SelectCardFromBottomListMessage(player, cardType, position));
    }

    /**
     * Sends a request to draw an extra card and blocks until the server confirms success
     * or sends back an error.
     *
     * @param player   the acting player.
     * @param cardType the type of card to draw.
     * @param position the position in the list.
     */
    @Override
    public void selectExtraCard(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        int prevCount = (cardType == CARD_TYPE.BUILDING)
                ? clientHandler.getTopBuildingSize()
                : clientHandler.getTopCardSize();

        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending selectExtraCard request for " + player.getNickName() + ", type " + cardType + ", position " + position + ".");
        send(new SelectExtraCardMessage(player, cardType, position));

        // Block until the server either removes a card (success) or sends an error.
        synchronized (clientHandler.turnLock) {
            while (!clientHandler.connectionError) {
                int currentCount = (cardType == CARD_TYPE.BUILDING)
                        ? clientHandler.getTopBuildingSize()
                        : clientHandler.getTopCardSize();
                if (currentCount < prevCount) break; // card removed = success
                try {
                    clientHandler.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (clientHandler.connectionError) {
            throw new RemoteException(clientHandler.lastErrorMessage);
        }
    }

    /**
     * Sends a request indicating the player skips their turn.
     *
     * @param playerDTO the player who does nothing.
     */
    @Override
    public void playerDoNothing(PlayerDTO playerDTO) throws Exception {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending playerDoNothing request for " + playerDTO.getNickName() + ".");
        send(new PlayerDoNothingMessage(playerDTO));
    }

    /**
     * Sends a skip-extra-draw request: the player declines the bonus draw without
     * selecting any card.
     *
     * @param player the player declining the extra draw.
     */
    @Override
    public void skipExtraDraw(PlayerDTO player) throws RemoteException {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending skipExtraDraw request for " + player.getNickName() + ".");
        send(new SkipExtraDrawMessage(player));
    }

    /**
     * Sends a request to add the player to the current lobby.
     * The {@code clientRemoteInterface} is not serialized — the server already has the socket reference.
     *
     * @param playerDTO             the player joining the game.
     * @param clientRemoteInterface ignored for socket transport.
     */
    @Override
    public void addPlayer(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameFullException, GameReadyToStartException, NameOrColorAlreadyTakenException {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending addPlayer request for " + playerDTO.getNickName() + ".");
        send(new AddPlayerMessage(playerDTO));
    }

    /**
     * Sends a request to create a new game.
     * The {@code clientRemoteInterface} is not serialized — the server already has the socket reference.
     *
     * @param playerHost            the hosting player.
     * @param PlayerNumber          the required number of players.
     * @param clientRemoteInterface ignored for socket transport.
     */
    @Override
    public void createGame(PlayerDTO playerHost, int PlayerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException, IllegalStateException {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending createGame request for host " + playerHost.getNickName() + " with " + PlayerNumber + " players.");
        send(new CreateGameMessage(playerHost, PlayerNumber));
    }

    /**
     * Sends the global leaderboard request to the server for the given player count.
     * The {@code clientRemoteInterface} is not serialized — the server already uses
     * the existing socket connection to reply.
     *
     * @param playerNumber          the number of players in the just-finished game (as a string).
     * @param clientRemoteInterface ignored for socket transport.
     * @throws RemoteException if sending the message fails.
     */
    @Override
    public void askForRank(String playerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending rank to player");
        send(new askForRankMessage(playerNumber));
    }

    /**
     * Sends a request to load a saved game to the server.
     * The {@code clientRemoteInterface} is not serialized.
     *
     * @param player                the DTO of the first player initiating the load.
     * @param clientRemoteInterface ignored for socket transport.
     * @throws RemoteException            if sending the message fails.
     * @throws GameAlreadyLoadedException if a game is already being loaded.
     * @throws NoGameToLoadException      if there is no saved game to load.
     */
    @Override
    public void loadGame(PlayerDTO player, ClientRemoteInterface clientRemoteInterface)
            throws RemoteException, GameAlreadyLoadedException, NoGameToLoadException {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending loadGame request for " + player.getNickName() + ".");
        send(new LoadGameMessage(player));
    }

    /**
     * Sends a request to join a game that is already being loaded.
     * The {@code clientRemoteInterface} is not serialized.
     *
     * @param player                the DTO of the player rejoining the saved game.
     * @param clientRemoteInterface ignored for socket transport.
     * @throws RemoteException           if sending the message fails.
     * @throws IllegalStateException     if the server state does not allow the operation.
     * @throws GameReadyToStartException if the last required player has just joined.
     */
    @Override
    public void joinGameLoaded(PlayerDTO player, ClientRemoteInterface clientRemoteInterface)
            throws RemoteException, IllegalStateException, GameReadyToStartException {
        ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Sending joinGameLoaded request for " + player.getNickName() + ".");
        send(new JoinGameLoadedMessage(player));
    }
}
