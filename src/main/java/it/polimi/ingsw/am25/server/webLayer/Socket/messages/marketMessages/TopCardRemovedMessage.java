package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that notifies the client a card was removed from the top market row.
 */
public class TopCardRemovedMessage implements ServerToClientMessage {
    /** The index of the card removed from the top market row. */
    private final int position;

    /**
     * Creates a message indicating that a card was removed from the top row.
     *
     * @param position the index of the removed card.
     */
    public TopCardRemovedMessage(int position) {
        this.position = position;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#topCardRemoved}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.topCardRemoved(position);
    }
}
