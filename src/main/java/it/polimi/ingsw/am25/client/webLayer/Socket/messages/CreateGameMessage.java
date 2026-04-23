package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

public class CreateGameMessage implements ClientToServerMessage {
    private final PlayerDTO playerHostL;
    private final int playerNumber;
    public CreateGameMessage(PlayerDTO playerHostL, int playerNumber) {
        this.playerHostL = playerHostL;
        this.playerNumber=playerNumber;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.createGame(playerHostL,playerNumber,clientRemoteInterface);
    }
}
