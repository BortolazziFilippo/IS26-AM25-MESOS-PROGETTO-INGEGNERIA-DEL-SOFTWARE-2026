package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

public class TopBuildingRefreshedMessage implements ServerToClientMessage {
    private final List<BuildingDTO> topList;

    /**
     * Creates a message carrying the refreshed top building row.
     * @param topList the new top building row.
     */
    public TopBuildingRefreshedMessage(List<BuildingDTO> topList) {
        this.topList = topList;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#topBuildingRefreshed}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.topBuildingRefreshed(topList);
    }
}
