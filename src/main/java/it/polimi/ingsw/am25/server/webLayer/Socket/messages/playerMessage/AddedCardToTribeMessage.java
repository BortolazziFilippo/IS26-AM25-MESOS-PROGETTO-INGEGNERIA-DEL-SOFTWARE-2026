package it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class AddedCardToTribeMessage implements ServerToClientMessage {
    private final String nickname;
    private final CardDTO cardDTO;

    /**
     * Creates a message indicating that a card was added to a player's tribe.
     * @param nickname the player's nickname.
     * @param cardDTO the card that was added.
     */
    public AddedCardToTribeMessage(String nickname, CardDTO cardDTO) {
        this.nickname = nickname;
        this.cardDTO = cardDTO;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#addedCardToTribe}. */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.addedCardToTribe(nickname,cardDTO);
    }
}
