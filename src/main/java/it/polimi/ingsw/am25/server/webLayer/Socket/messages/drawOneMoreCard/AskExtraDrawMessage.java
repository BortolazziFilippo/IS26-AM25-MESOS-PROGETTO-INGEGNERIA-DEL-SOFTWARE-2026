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
    private final List<CardDTO> snapshotCards;
    private final List<BuildingDTO> snapshotBuildings;

    /** Creates an ask-extra-draw notification carrying the end-of-round market snapshot. */
    public AskExtraDrawMessage(List<CardDTO> snapshotCards, List<BuildingDTO> snapshotBuildings) {
        this.snapshotCards = snapshotCards;
        this.snapshotBuildings = snapshotBuildings;
    }

    @Override
    public void execute(ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.askExtraDraw(snapshotCards, snapshotBuildings);
    }
}
