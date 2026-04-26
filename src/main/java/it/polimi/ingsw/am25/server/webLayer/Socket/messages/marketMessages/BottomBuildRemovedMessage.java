package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class BottomBuildRemovedMessage implements ServerToClientMessage {
    private final int position;

    /**
     * Creates a message indicating that a building was removed from the bottom row.
     * @param position the index of the removed building.
     */
    public BottomBuildRemovedMessage(int position) {
        this.position = position;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#bottomBuildRemoved}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.bottomBuildRemoved(position);
    }


}
