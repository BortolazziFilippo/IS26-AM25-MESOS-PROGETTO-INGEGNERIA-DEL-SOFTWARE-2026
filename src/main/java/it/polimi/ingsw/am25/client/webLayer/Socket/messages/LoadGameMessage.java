package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Socket message sent by the first reconnecting player to load a saved game.
 */
public class LoadGameMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;

    public LoadGameMessage(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.loadGame(playerDTO, clientRemoteInterface);
    }
}
