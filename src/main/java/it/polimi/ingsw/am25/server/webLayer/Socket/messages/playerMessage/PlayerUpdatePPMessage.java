package it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class PlayerUpdatePPMessage implements ServerToClientMessage {
    private final String nickname;
    private final int PP;

    public PlayerUpdatePPMessage(String nickname, int PP) {
        this.nickname = nickname;
        this.PP = PP;
    }

    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerUpdateFood(nickname,PP);
    }
}
