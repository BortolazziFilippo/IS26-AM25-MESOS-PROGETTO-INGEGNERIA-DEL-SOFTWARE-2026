package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class PlayerToPlayChangedMessage implements ServerToClientMessage {
    private final PlayerDTO playerDTO;

    /**
     * Creates a message indicating which player must act next.
     * @param playerDTO the player whose turn it now is.
     */
    public PlayerToPlayChangedMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#playerToPlayChanged}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerToPlayChanged(playerDTO);
    }
}
