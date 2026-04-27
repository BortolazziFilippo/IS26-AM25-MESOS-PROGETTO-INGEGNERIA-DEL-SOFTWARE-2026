package it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

/**
 * Server-to-client Socket message that pushes the initial market state (top/bottom card and building rows).
 */
public class InitializeMarketMessage implements ServerToClientMessage {
    private final List<CardDTO> topCards;
    private final List<CardDTO> bottomCards;
    private final List<BuildingDTO> topBuildings;

    /**
     * Creates a message carrying the initial market state.
     * @param topCards top row of cards.
     * @param bottomCards bottom row of cards.
     * @param topBuildings top row of buildings.
     */
    public InitializeMarketMessage(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings) {
        this.topCards = topCards;
        this.bottomCards = bottomCards;
        this.topBuildings = topBuildings;
    }

    /** Dispatches this message by calling {@link ClientRemoteInterface#initializeMarket}. */
    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.initializeMarket(topCards,bottomCards,topBuildings);
    }
}
