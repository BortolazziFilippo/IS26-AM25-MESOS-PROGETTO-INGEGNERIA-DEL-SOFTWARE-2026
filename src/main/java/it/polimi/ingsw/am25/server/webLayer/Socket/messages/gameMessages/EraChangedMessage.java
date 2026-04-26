package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class EraChangedMessage implements ServerToClientMessage {
    private final ERA newEra;

    /**
     * Creates a message carrying the new era.
     * @param newEra the era that just started.
     */
    public EraChangedMessage(ERA newEra) {
        this.newEra = newEra;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#eraChanged}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.eraChanged(newEra);
    }
}
