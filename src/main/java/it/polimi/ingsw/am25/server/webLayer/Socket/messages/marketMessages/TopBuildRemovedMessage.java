package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that notifies the client a building was removed from the top market row.
 */
public class TopBuildRemovedMessage implements ServerToClientMessage {
    private final int position;

    /**
     * Creates a message indicating that a building was removed from the top row.
     *
     * @param position the index of the removed building.
     */
    public TopBuildRemovedMessage(int position) {
        this.position = position;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#topBuildRemoved}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.topBuildRemoved(position);
    }

}
