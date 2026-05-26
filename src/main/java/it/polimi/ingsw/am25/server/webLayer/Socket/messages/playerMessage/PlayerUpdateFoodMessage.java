package it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Server-to-client Socket message that notifies the client of an updated food total for a player.
 */
public class PlayerUpdateFoodMessage implements ServerToClientMessage {
    /** The nickname of the player whose food total changed. */
    private final String nickname;
    /** The new food total for the player. */
    private final int food;

    /**
     * Creates a message carrying an updated food value for a player.
     *
     * @param nickname the player's nickname.
     * @param food     the new food total.
     */
    public PlayerUpdateFoodMessage(String nickname, int food) {
        this.nickname = nickname;
        this.food = food;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#playerUpdateFood}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerUpdateFood(nickname, food);
    }
}
