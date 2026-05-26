package it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes;

import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

/**
 * Server-to-client Socket message that notifies the client of the updated player order on the default tiles.
 */
public class OrderOnDefaultTileMessage implements ServerToClientMessage {
    /** The updated ordered list of players on the default tile. */
    private final List<PlayerDTO> orderOnDefaultTile;

    /**
     * Creates a message carrying the player order on the default tile.
     *
     * @param orderOnDefaultTile ordered list of players.
     */
    public OrderOnDefaultTileMessage(List<PlayerDTO> orderOnDefaultTile) {
        this.orderOnDefaultTile = orderOnDefaultTile;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#orderOnDefaultTile}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.orderOnDefaultTile(orderOnDefaultTile);
    }
}
