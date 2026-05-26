package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.webLayer.DTOs.ActionDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Socket message that notifies the client that the available actions for the current turn have changed.
 */
public class ActionAvailableChangedMessage implements ServerToClientMessage {
    /** The updated action descriptor for the current turn. */
    private final ActionDTO actionDTO;

    /**
     * Creates a message carrying updated action availability.
     *
     * @param actionDTO the new action descriptor.
     */
    public ActionAvailableChangedMessage(ActionDTO actionDTO) {
        this.actionDTO = actionDTO;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#actionAvailableChanged}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.actionAvailableChanged(actionDTO);
    }
}
