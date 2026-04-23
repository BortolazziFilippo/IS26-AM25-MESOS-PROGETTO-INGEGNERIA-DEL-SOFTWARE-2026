package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

public class SelectCardFromBottomListMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;
    private final CARD_TYPE cardType;
    private final int position;

    public SelectCardFromBottomListMessage(PlayerDTO playerDTO, CARD_TYPE cardType, int position) {
        this.playerDTO = playerDTO;
        this.cardType = cardType;
        this.position = position;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.selectCardFromTopList(playerDTO,cardType,position);
    }
}
