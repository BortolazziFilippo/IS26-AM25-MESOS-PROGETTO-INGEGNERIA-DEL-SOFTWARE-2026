package it.polimi.ingsw.am25.server.webLayer.Socket.messages.drawOneMoreCard;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class AskExtraDrawMessage implements ServerToClientMessage {
    public AskExtraDrawMessage() {
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.askExtraDraw();
    }
}
