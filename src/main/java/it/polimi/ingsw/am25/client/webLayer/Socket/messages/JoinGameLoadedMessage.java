package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Socket message sent by a reconnecting player to join a game being loaded.
 */
public class JoinGameLoadedMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;

    /**
     * Creates the socket message to join a game that is being loaded.
     *
     * @param playerDTO the DTO of the player rejoining the saved game.
     */
    public JoinGameLoadedMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    /**
     * Executes the message by invoking {@link ServerRemoteInterface#joinGameLoaded} on the server.
     *
     * @param serverRemoteInterface the remote server interface on which to execute the call.
     * @param clientRemoteInterface the client reference to pass to the server.
     * @throws Exception if a communication error occurs.
     */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.joinGameLoaded(playerDTO, clientRemoteInterface);
    }
}
