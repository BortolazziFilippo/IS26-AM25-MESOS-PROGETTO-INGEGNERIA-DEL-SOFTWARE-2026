package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that notifies all clients when a player reconnects.
 */
public class PlayerReconnectedMessage implements ServerToClientMessage {
    private final String nickname;

    /**
     * Creates a reconnection notification for the given player.
     * @param nickname the nickname of the reconnected player.
     */
    public PlayerReconnectedMessage(String nickname) {
        this.nickname = nickname;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#playerReconnected}. */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerReconnected(nickname);
    }
}
