package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that pushes the initial game state to the client at game start.
 */
public class InitializeGameMessage implements ServerToClientMessage {
    private final ERA currentEra;
    private final GAME_PHASE gamePhase;
    private final String playerToPlace;
    private final String playerToPlay;

    /**
     * Creates a message carrying the initial game state.
     * @param currentEra the starting era.
     * @param gamePhase the starting game phase.
     * @param playerToPlace the first player to place.
     * @param playerToPlay the first player to play.
     */
    public InitializeGameMessage(ERA currentEra, GAME_PHASE gamePhase, String playerToPlace, String playerToPlay) {
        this.currentEra = currentEra;
        this.gamePhase = gamePhase;
        this.playerToPlace = playerToPlace;
        this.playerToPlay = playerToPlay;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#initializeGame}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.initializeGame(currentEra,gamePhase,playerToPlace,playerToPlay);
    }
}
