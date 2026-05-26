package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that delivers an error description to the client
 * (e.g. invalid action, wrong turn, lobby full).
 */
public class ErrorMessage implements ServerToClientMessage {
    /** The human-readable error description to display to the player. */
    private final String errorMessage;

    /**
     * Creates a message carrying an error description to show to the client.
     *
     * @param errorMessage the error description.
     */
    public ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#showErrorMessage}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.showErrorMessage(errorMessage);
    }
}
