package it.polimi.ingsw.am25.server.webLayer.Socket.messages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;
import java.util.Map;

public class SendRankMessage implements ServerToClientMessage {
    private final Map<Integer, List<String>> leaderboards;

    public SendRankMessage(Map<Integer, List<String>> leaderboards) {
        this.leaderboards = leaderboards;
    }

    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws
            Exception {
        clientRemoteInterface.sendRank(leaderboards);
    }

}
