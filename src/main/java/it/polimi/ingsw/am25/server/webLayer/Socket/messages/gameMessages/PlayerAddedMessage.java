package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class PlayerAddedMessage implements ServerToClientMessage {
    private final PlayerDTO playerAdded;

    public PlayerAddedMessage(PlayerDTO playerAdded) {
        this.playerAdded = playerAdded;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerAdded(playerAdded);
    }
}
