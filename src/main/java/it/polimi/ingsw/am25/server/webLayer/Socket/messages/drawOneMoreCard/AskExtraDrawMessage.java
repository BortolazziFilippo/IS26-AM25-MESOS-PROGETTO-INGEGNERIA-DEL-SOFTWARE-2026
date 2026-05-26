package it.polimi.ingsw.am25.server.webLayer.Socket.messages.drawOneMoreCard;

import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

/**
 * Socket message that asks the client to pick an extra card from the end-of-round market snapshot,
 * triggered by the draw-one-more building effect.
 */
public class AskExtraDrawMessage implements ServerToClientMessage {
    /** The end-of-round top card row snapshot available for the extra draw. */
    private final List<CardDTO> snapshotCards;
    /** The end-of-round top building row snapshot available for the extra draw. */
    private final List<BuildingDTO> snapshotBuildings;

    /**
     * Creates an ask-extra-draw notification carrying the end-of-round market snapshot.
     *
     * @param snapshotCards     the top card row available for the extra draw.
     * @param snapshotBuildings the top building row available for the extra draw.
     */
    public AskExtraDrawMessage(List<CardDTO> snapshotCards, List<BuildingDTO> snapshotBuildings) {
        this.snapshotCards = snapshotCards;
        this.snapshotBuildings = snapshotBuildings;
    }

    /**
     * Delivers this message to the client by invoking
     * {@link ClientRemoteInterface#askExtraDraw} with the end-of-turn market snapshot.
     *
     * @param clientRemoteInterface the remote interface of the client to deliver the message to.
     * @throws Exception if an error occurs during the remote invocation.
     */
    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.askExtraDraw(snapshotCards, snapshotBuildings);
    }
}
