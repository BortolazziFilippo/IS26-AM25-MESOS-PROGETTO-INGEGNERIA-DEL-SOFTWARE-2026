package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class GamePhaseChangedMessage implements ServerToClientMessage {
    private final GAME_PHASE gamePhase;

    /**
     * Creates a message carrying the new game phase.
     * @param gamePhase the phase that just started.
     */
    public GamePhaseChangedMessage(GAME_PHASE gamePhase) {
        this.gamePhase = gamePhase;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#gamePhaseChanged}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.gamePhaseChanged(gamePhase);
    }
}
