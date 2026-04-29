package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Socket message sent by a client to join the current Mesos lobby.
 */
public class AddPlayerMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;

    /**
     * Creates a message requesting that the given player join the current lobby.
     * @param playerDTO the player who wants to join.
     */
    public AddPlayerMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    /** Dispatches this message by calling {@link ServerRemoteInterface#addPlayer}. */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.addPlayer(playerDTO,clientRemoteInterface);
    }
}
