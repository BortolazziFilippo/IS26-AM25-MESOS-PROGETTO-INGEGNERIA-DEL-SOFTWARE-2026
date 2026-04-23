package it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

public class AddedCardToTribeMessage implements ServerToClientMessage {
    private final String nickname;
    private final CardDTO cardDTO;

    public AddedCardToTribeMessage(String nickname, CardDTO cardDTO) {
        this.nickname = nickname;
        this.cardDTO = cardDTO;
    }

    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.addedCardToTribe(nickname,cardDTO);
    }
}
