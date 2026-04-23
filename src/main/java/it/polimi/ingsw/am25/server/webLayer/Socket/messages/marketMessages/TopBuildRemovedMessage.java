package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class TopBuildRemovedMessage implements ServerToClientMessage {
    private final int position;

    public TopBuildRemovedMessage(int position) {
        this.position = position;
    }

    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.topBuildRemoved(position);
    }

}
