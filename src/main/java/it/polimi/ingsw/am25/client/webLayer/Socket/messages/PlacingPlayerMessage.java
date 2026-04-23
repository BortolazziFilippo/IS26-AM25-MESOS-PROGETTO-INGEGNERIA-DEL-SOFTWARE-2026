package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

public class PlacingPlayerMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;
    private final int position;
    public PlacingPlayerMessage(PlayerDTO playerDTO, int position) {
        this.playerDTO = playerDTO;
        this.position=position;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.placingPlayer(playerDTO,position);
    }
}
