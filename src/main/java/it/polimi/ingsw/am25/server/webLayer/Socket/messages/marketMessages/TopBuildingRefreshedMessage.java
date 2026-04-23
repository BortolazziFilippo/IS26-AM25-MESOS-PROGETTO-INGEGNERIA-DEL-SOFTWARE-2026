package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

public class TopBuildingRefreshedMessage implements ServerToClientMessage {
    private final List<BuildingDTO> topList;

    public TopBuildingRefreshedMessage(List<BuildingDTO> topList) {
        this.topList = topList;
    }

    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.topBuildingRefreshed(topList);
    }
}
