package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Client-to-server Socket message requesting that the player skip the extra draw
 * granted by the draw-one-more building effect without selecting any card.
 */
public class SkipExtraDrawMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;

    /**
     * Creates a message indicating the player declines the extra draw.
     * @param playerDTO the player skipping the extra draw.
     */
    public SkipExtraDrawMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    /** Dispatches this message by calling {@link ServerRemoteInterface#skipExtraDraw}. */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.skipExtraDraw(playerDTO);
    }
}
