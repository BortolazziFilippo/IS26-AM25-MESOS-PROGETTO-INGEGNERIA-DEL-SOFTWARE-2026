package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class GamePhaseChangedMessage implements ServerToClientMessage {
    private final GAME_PHASE gamePhase;

    public GamePhaseChangedMessage(GAME_PHASE gamePhase) {
        this.gamePhase = gamePhase;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.gamePhaseChanged(gamePhase);
    }
}
