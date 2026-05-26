package it.polimi.ingsw.am25.server.webLayer.Socket.messages;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;
import java.util.Map;

/**
 * Server-to-client Socket message that delivers the global leaderboard to the client at the end of a game.
 */
public class SendRankMessage implements ServerToClientMessage {
    /** Map from player count to the corresponding ordered leaderboard list. */
    private final Map<Integer, List<String>> leaderboards;

    /**
     * Creates a message carrying the leaderboards to send to the client via socket.
     *
     * @param leaderboards a map associating the number of players with the corresponding leaderboard list.
     */
    public SendRankMessage(Map<Integer, List<String>> leaderboards) {
        this.leaderboards = leaderboards;
    }

    /**
     * Delivers this message to the client by invoking
     * {@link ClientRemoteInterface#sendRank} with the leaderboards.
     *
     * @param clientRemoteInterface the remote interface of the client to deliver the message to.
     * @throws Exception if an error occurs during the remote invocation.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws
            Exception {
        clientRemoteInterface.sendRank(leaderboards);
    }

}
