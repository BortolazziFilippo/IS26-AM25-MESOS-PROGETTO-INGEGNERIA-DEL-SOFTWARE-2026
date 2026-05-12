package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Socket message sent by a reconnecting player to join a game being loaded.
 */
public class JoinGameLoadedMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;

    public JoinGameLoadedMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.joinGameLoaded(playerDTO, clientRemoteInterface);
    }
}
