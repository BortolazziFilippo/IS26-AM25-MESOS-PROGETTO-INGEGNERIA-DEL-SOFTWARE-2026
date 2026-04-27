package it.polimi.ingsw.am25.client.webLayer.Socket.messages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

/**
 * Client-to-server Socket message requesting that a card be drawn from the bottom market row.
 */
public class SelectCardFromBottomListMessage implements ClientToServerMessage {
    private final PlayerDTO playerDTO;
    private final CARD_TYPE cardType;
    private final int position;

    /**
     * Creates a message requesting a card draw from the bottom market row.
     * @param playerDTO the acting player.
     * @param cardType the type of card to draw.
     * @param position the position in the bottom row.
     */
    public SelectCardFromBottomListMessage(PlayerDTO playerDTO, CARD_TYPE cardType, int position) {
        this.playerDTO = playerDTO;
        this.cardType = cardType;
        this.position = position;
    }

    /** Dispatches this message by calling {@link ServerRemoteInterface#selectCardFromBottomList}. */
    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        serverRemoteInterface.selectCardFromBottomList(playerDTO,cardType,position);
    }
}
