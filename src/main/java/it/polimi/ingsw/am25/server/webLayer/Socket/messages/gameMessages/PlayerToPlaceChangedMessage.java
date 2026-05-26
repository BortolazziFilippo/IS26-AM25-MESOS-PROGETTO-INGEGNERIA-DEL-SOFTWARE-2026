package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that notifies the client which player must place their totem next.
 */
public class PlayerToPlaceChangedMessage implements ServerToClientMessage {
    /** The player who must place their totem next. */
    private final PlayerDTO playerDTO;

    /**
     * Creates a message indicating which player must place next.
     *
     * @param playerDTO the player who must place.
     */
    public PlayerToPlaceChangedMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#playerToPlaceChanged}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerToPlaceChanged(playerDTO);
    }
}
