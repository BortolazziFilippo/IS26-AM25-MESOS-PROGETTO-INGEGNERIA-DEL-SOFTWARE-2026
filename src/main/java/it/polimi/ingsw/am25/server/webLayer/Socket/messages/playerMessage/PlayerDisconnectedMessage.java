package it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that notifies the client that a player has disconnected.
 * The client uses this to mark the player as DISCONNESSO in the PlayerStatusTUI.
 */
public class PlayerDisconnectedMessage implements ServerToClientMessage {
    /** The nickname of the player who disconnected. */
    private final String nickname;

    /**
     * Creates a disconnection notification for the given player.
     *
     * @param nickname the nickname of the player who disconnected.
     */
    public PlayerDisconnectedMessage(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#playerDisconnected}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerDisconnected(nickname);
    }
}
