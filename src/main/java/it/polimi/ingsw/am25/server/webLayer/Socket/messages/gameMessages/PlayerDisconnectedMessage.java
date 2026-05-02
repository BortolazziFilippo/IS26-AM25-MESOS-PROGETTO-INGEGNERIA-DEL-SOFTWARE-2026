package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that notifies all clients when a player disconnects.
 */
public class PlayerDisconnectedMessage implements ServerToClientMessage {
    private final String nickname;

    /**
     * Creates a disconnection notification for the given player.
     * @param nickname the nickname of the disconnected player.
     */
    public PlayerDisconnectedMessage(String nickname) {
        this.nickname = nickname;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#playerDisconnected}. */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerDisconnected(nickname);
    }
}
