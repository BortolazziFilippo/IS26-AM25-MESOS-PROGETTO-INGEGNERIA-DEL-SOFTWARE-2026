package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class ResolvedEventMessage implements ServerToClientMessage {
    private final int eventID;
    private final EVENT_TYPE eventType;

    public ResolvedEventMessage(int eventID, EVENT_TYPE eventType) {
        this.eventID = eventID;
        this.eventType = eventType;
    }

    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.eventResolved(eventID, eventType);
    }
}
