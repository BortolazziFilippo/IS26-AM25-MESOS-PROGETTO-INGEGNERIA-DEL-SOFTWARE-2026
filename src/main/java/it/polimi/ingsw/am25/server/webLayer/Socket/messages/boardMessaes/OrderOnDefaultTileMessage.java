package it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

public class OrderOnDefaultTileMessage implements ServerToClientMessage {
    private final List<PlayerDTO> orderOnDefaultTile;

    public OrderOnDefaultTileMessage(List<PlayerDTO> orderOnDefaultTile) {
        this.orderOnDefaultTile = orderOnDefaultTile;
    }

    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.orderOnDefaultTile(orderOnDefaultTile);
    }
}
