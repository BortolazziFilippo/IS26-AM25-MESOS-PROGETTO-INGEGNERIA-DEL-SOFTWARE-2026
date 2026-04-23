package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class InitializeGameMessage implements ServerToClientMessage {
    private final ERA currentEra;
    private final GAME_PHASE gamePhase;
    private final String playerToPlace;
    private final String playerToPlay;

    public InitializeGameMessage(ERA currentEra, GAME_PHASE gamePhase, String playerToPlace, String playerToPlay) {
        this.currentEra = currentEra;
        this.gamePhase = gamePhase;
        this.playerToPlace = playerToPlace;
        this.playerToPlay = playerToPlay;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.initializeGame(currentEra,gamePhase,playerToPlace,playerToPlay);
    }
}
