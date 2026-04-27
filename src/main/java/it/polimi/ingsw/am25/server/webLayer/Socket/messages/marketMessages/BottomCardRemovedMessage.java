package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Socket message that notifies the client that a tribe card was removed from the previous-round (bottom) market row.
 */
public class BottomCardRemovedMessage implements ServerToClientMessage {
    private final int position;

    /**
     * Creates a message indicating that a card was removed from the bottom row.
     * @param position the index of the removed card.
     */
    public BottomCardRemovedMessage(int position) {
        this.position = position;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#bottomCardRemoved}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.bottomCardRemoved(position);
    }

}
