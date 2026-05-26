package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

/**
 * Server-to-client Socket message that notifies the client the top card row has been refreshed.
 */
public class TopCardRefreshedMessage implements ServerToClientMessage {
    /** The refreshed top card row. */
    private final List<CardDTO> topCards;

    /**
     * Creates a message carrying the refreshed top card row.
     *
     * @param topCards the new top card row.
     */
    public TopCardRefreshedMessage(List<CardDTO> topCards) {
        this.topCards = topCards;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#topCardRefreshed}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.topCardRefreshed(topCards);
    }
}
