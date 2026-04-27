package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.rmi.RemoteException;

public class ResolvedEventMessage implements ServerToClientMessage {
    private String message;

    public ResolvedEventMessage(String message) {
        this.message = message;
    }

    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.eventResolved(this.message);
    }
}
