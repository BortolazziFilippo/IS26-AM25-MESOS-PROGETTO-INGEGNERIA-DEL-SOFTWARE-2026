package it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class PlayerPlacedOnOffertileMessage implements ServerToClientMessage {
    private final String nickname;
    private final int position;

    public PlayerPlacedOnOffertileMessage(String nickname, int position) {
        this.nickname = nickname;
        this.position = position;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerPlacedOnOffertile(nickname,position);
    }
}
