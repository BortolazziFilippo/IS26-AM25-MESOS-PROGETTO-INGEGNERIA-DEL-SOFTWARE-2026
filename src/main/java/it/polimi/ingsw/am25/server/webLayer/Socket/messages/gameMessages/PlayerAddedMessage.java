package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class PlayerAddedMessage implements ServerToClientMessage {
    private final PlayerDTO playerAdded;

    /**
     * Creates a message indicating that a player joined the lobby.
     * @param playerAdded the player who joined.
     */
    public PlayerAddedMessage(PlayerDTO playerAdded) {
        this.playerAdded = playerAdded;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#playerAdded}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerAdded(playerAdded);
    }
}
