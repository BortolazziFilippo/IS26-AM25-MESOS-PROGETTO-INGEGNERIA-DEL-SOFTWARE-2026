package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.ActionDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class ActionAvailableChangedMessage implements ServerToClientMessage {
    private final ActionDTO actionDTO;

    /**
     * Creates a message carrying updated action availability.
     * @param actionDTO the new action descriptor.
     */
    public ActionAvailableChangedMessage(ActionDTO actionDTO) {
        this.actionDTO = actionDTO;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#actionAvailableChanged}. */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.actionAvailableChanged(actionDTO);
    }
}
