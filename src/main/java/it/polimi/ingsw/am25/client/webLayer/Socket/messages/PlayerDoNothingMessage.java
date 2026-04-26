package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

public class PlayerDoNothingMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;

    /**
     * Creates a message indicating the player skips their turn.
     * @param playerDTO the player who does nothing.
     */
    public PlayerDoNothingMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    /** Dispatches this message by calling {@link ServerRemoteInterface#playerDoNothing}. */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.playerDoNothing(playerDTO);
    }
}
