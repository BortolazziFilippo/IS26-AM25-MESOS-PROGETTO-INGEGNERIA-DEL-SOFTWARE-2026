package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Client-to-server heartbeat message.
 * Sent periodically by the client so the server knows the player is still connected.
 * On receipt the server resets the missed-ping counter for this player.
 */
public class PingMessage implements ClientToServerMessage {
    private final PlayerDTO player;

    /**
     * Creates a ping message for the given player.
     *
     * @param player the player sending the heartbeat.
     */
    public PingMessage(PlayerDTO player) {
        this.player = player;
    }

    /**
     * Dispatches this message by calling {@link ServerRemoteInterface#ping}.
     */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.ping(player);
        clientRemoteInterface.pong();
    }
}
