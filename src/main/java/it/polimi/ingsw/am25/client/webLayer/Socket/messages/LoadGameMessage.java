package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Socket message sent by the first reconnecting player to load a saved game.
 */
public class LoadGameMessage implements ClientToServerMessage {
    /** The DTO of the first player requesting the game load. */
    private final PlayerDTO playerDTO;

    /**
     * Creates the socket message to initiate loading of a saved game.
     *
     * @param playerDTO the DTO of the first player requesting the game load.
     */
    public LoadGameMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    /**
     * Executes the message by invoking {@link ServerRemoteInterface#loadGame} on the server.
     *
     * @param serverRemoteInterface the remote server interface on which to execute the call.
     * @param clientRemoteInterface the client reference to pass to the server.
     * @throws Exception if a communication error occurs.
     */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.loadGame(playerDTO, clientRemoteInterface);
    }
}
