package it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes;

import it.polimi.ingsw.am25.server.webLayer.DTOs.DefaultTileDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.OffertileDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

/**
 * Socket message that sends the initial board state (offer tiles and default tiles) to the client at game start.
 */
public class BoardInitializeMessages implements ServerToClientMessage {
    private final List<OffertileDTO> offertileDTOS;
    private final List<DefaultTileDTO> defaultTileDTOS;

    /**
     * Creates a message carrying the initial board state.
     *
     * @param offertileDTOS   the offer tiles.
     * @param defaultTileDTOS the default tiles.
     */
    public BoardInitializeMessages(List<OffertileDTO> offertileDTOS, List<DefaultTileDTO> defaultTileDTOS) {
        this.offertileDTOS = offertileDTOS;
        this.defaultTileDTOS = defaultTileDTOS;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#boardInitialize}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.boardInitialize(offertileDTOS, defaultTileDTOS);
    }
}
