package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client heartbeat response. Sent after every ping so the client's
 * socket read timeout does not fire during idle game phases.
 */
public class PongMessage implements ServerToClientMessage {

    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.pong();
    }
}
