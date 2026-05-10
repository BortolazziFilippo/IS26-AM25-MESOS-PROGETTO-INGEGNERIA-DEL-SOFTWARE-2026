package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

public class askForRankMessage implements ClientToServerMessage {
    private final String playerNumber;

    public askForRankMessage(String playerNumber) {
        this.playerNumber = playerNumber;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.askForRank(playerNumber, clientRemoteInterface);
    }
}
