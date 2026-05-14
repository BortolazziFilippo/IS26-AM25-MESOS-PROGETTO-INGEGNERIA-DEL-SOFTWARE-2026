package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

public class askForRankMessage implements ClientToServerMessage {
    private final String playerNumber;

    /**
     * Creates the socket message to request the global leaderboard.
     *
     * @param playerNumber the number of players in the just-finished game,
     *                     as a string, used to filter the correct leaderboard.
     */
    public askForRankMessage(String playerNumber) {
        this.playerNumber = playerNumber;
    }

    /**
     * Executes the message by invoking {@link ServerRemoteInterface#askForRank} on the server,
     * passing the player count and the client reference.
     *
     * @param serverRemoteInterface the remote server interface on which to execute the call.
     * @param clientRemoteInterface the client reference to pass to the server.
     * @throws Exception if a communication error occurs.
     */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.askForRank(playerNumber, clientRemoteInterface);
    }
}
