package it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that notifies the client of an updated prestige-point total for a player.
 */
public class PlayerUpdatePPMessage implements ServerToClientMessage {
    /** The nickname of the player whose prestige points changed. */
    private final String nickname;
    /** The new prestige-point total for the player. */
    private final int PP;

    /**
     * Creates a message carrying an updated prestige-point value for a player.
     *
     * @param nickname the player's nickname.
     * @param PP       the new prestige-point total.
     */
    public PlayerUpdatePPMessage(String nickname, int PP) {
        this.nickname = nickname;
        this.PP = PP;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#playerUpdatePP}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerUpdatePP(nickname, PP);
    }
}
