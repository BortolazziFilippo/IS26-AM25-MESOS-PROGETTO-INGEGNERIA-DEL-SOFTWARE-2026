package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class ErrorMessage implements ServerToClientMessage {
    private final String errorMessage;

    public ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.showErrorMessage(errorMessage);
    }
}
