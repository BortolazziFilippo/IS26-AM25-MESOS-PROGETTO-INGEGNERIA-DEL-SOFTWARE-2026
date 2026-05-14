package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class ResolvedEventMessage implements ServerToClientMessage {
    private final int eventID;
    private final EVENT_TYPE eventType;

    /**
     * Creates a resolved-event message to send to the client via socket.
     *
     * @param eventID   the unique identifier of the resolved event.
     * @param eventType the type of the resolved event.
     */
    public ResolvedEventMessage(int eventID, EVENT_TYPE eventType) {
        this.eventID = eventID;
        this.eventType = eventType;
    }

    /**
     * Delivers this message to the client by invoking
     * {@link ClientRemoteInterface#eventResolved} with the event ID and type.
     *
     * @param clientRemoteInterface the remote interface of the client to deliver the message to.
     * @throws Exception if an error occurs during the remote invocation.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.eventResolved(eventID, eventType);
    }
}
