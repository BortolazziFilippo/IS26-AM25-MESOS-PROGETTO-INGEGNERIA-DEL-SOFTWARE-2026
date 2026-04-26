package it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class PlayerUpdateFoodMessage implements ServerToClientMessage {
    private final String nickname;
    private final int food;

    /**
     * Creates a message carrying an updated food value for a player.
     * @param nickname the player's nickname.
     * @param food the new food total.
     */
    public PlayerUpdateFoodMessage(String nickname, int food) {
        this.nickname = nickname;
        this.food = food;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#playerUpdateFood}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.playerUpdateFood(nickname,food);
    }
}
