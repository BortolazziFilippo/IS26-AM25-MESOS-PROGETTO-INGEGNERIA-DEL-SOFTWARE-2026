package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

/**
 * Server-to-client Socket message that delivers the final list of game winners.
 */
public class GameWinnersMessage implements ServerToClientMessage {
    /** The list of players who won the game. */
    private final List<PlayerDTO> playerDTOList;

    /**
     * Creates a message carrying the list of game winners.
     *
     * @param playerDTOList the winning players.
     */
    public GameWinnersMessage(List<PlayerDTO> playerDTOList) {
        this.playerDTOList = playerDTOList;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#gameWinners}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.gameWinners(playerDTOList);
    }
}
