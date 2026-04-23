package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

public class InitializeMarketMessage implements ServerToClientMessage {
    private final List<CardDTO> topCards;
    private final List<CardDTO> bottomCards;
    private final List<BuildingDTO> topBuildings;

    public InitializeMarketMessage(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings) {
        this.topCards = topCards;
        this.bottomCards = bottomCards;
        this.topBuildings = topBuildings;
    }

    @Override
    public void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.initializeMarket(topCards,bottomCards,topBuildings);
    }
}
