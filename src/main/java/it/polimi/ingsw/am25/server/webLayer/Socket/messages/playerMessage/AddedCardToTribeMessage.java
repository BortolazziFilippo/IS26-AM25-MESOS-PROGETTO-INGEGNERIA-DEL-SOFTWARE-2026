package it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage;

import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

/**
 * Socket message that notifies the client that a tribe card was added to a player's tribe.
 */
public class AddedCardToTribeMessage implements ServerToClientMessage {
    /** The nickname of the player who received the card. */
    private final String nickname;
    /** The card that was added to the player's tribe. */
    private final CardDTO cardDTO;

    /**
     * Creates a message indicating that a card was added to a player's tribe.
     *
     * @param nickname the player's nickname.
     * @param cardDTO  the card that was added.
     */
    public AddedCardToTribeMessage(String nickname, CardDTO cardDTO) {
        this.nickname = nickname;
        this.cardDTO = cardDTO;
    }

    /**
     * Dispatches this message by calling {@link ClientRemoteInterface#addedCardToTribe}.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.addedCardToTribe(nickname, cardDTO);
    }
}
