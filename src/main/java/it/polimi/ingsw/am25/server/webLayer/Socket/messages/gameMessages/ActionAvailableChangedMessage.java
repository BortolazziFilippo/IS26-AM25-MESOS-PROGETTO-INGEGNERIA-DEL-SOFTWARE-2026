package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.ActionDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class ActionAvailableChangedMessage implements ServerToClientMessage {
    private final ActionDTO actionDTO;

    public ActionAvailableChangedMessage(ActionDTO actionDTO) {
        this.actionDTO = actionDTO;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.actionAvailableChanged(actionDTO);
    }
}
