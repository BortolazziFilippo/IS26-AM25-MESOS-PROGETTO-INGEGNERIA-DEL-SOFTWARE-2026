package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

public class CreateGameMessage implements ClientToServerMessage {
    private final PlayerDTO playerHostL;
    private final int playerNumber;
    /**
     * Creates a message requesting that a new game be created.
     * @param playerHostL the hosting player.
     * @param playerNumber the required number of players.
     */
    public CreateGameMessage(PlayerDTO playerHostL, int playerNumber) {
        this.playerHostL = playerHostL;
        this.playerNumber=playerNumber;
    }

    /** Dispatches this message by calling {@link ServerRemoteInterface#createGame}. */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.createGame(playerHostL,playerNumber,clientRemoteInterface);
    }
}
