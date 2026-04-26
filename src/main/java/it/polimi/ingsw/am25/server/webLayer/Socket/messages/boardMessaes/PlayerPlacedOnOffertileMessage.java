package it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class PlayerPlacedOnOffertileMessage implements ServerToClientMessage {
    private final String nickname;
    private final int position;

    /**
     * Creates a message indicating that a player was placed on an offer tile.
     * @param nickname the player's nickname.
     * @param position the tile position chosen.
     */
    public PlayerPlacedOnOffertileMessage(String nickname, int position) {
        this.nickname = nickname;
        this.position = position;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#playerPlacedOnOffertile}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerPlacedOnOffertile(nickname,position);
    }
}
