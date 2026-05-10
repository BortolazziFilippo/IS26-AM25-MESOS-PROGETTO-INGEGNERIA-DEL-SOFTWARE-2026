package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Client-to-server Socket message that requests a totem placement on an offer tile.
 */
public class PlacingPlayerMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;
    private final int position;

    /**
     * Creates a message requesting that the player be placed at the given tile position.
     *
     * @param playerDTO the player who is placing.
     * @param position  the tile position chosen.
     */
    public PlacingPlayerMessage(PlayerDTO playerDTO, int position) {
        this.playerDTO = playerDTO;
        this.position = position;
    }

    /**
     * Dispatches this message by calling {@link ServerRemoteInterface#placingPlayer}.
     */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.placingPlayer(playerDTO, position);
    }
}
