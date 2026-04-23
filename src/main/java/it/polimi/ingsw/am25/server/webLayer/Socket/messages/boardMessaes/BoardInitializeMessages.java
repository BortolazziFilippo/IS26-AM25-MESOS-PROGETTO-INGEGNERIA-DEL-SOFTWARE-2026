package it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.DefaultTileDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.OffertileDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

public class BoardInitializeMessages implements ServerToClientMessage {
    private final List<OffertileDTO> offertileDTOS;
    private final List<DefaultTileDTO> defaultTileDTOS;

    public BoardInitializeMessages(List<OffertileDTO> offertileDTOS, List<DefaultTileDTO> defaultTileDTOS) {
        this.offertileDTOS = offertileDTOS;
        this.defaultTileDTOS = defaultTileDTOS;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.boardInitialize(offertileDTOS,defaultTileDTOS);
    }
}
