package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

import java.util.List;

/**
 * Memento snapshot of the market state at end of round.
 */
public class MarketMemento {
    private final List<CardDTO> topCards;
    private final List<CardDTO> bottomCards;
    private final List<CardDTO> deck;
    private final List<Integer> topBuildingIDs;
    private final List<Integer> bottomBuildingIDs;
    private final List<Integer> buildingPoolIDs;

    public MarketMemento(List<CardDTO> topCards, List<CardDTO> bottomCards, List<CardDTO> deck,
                         List<Integer> topBuildingIDs, List<Integer> bottomBuildingIDs,
                         List<Integer> buildingPoolIDs) {
        this.topCards = topCards;
        this.bottomCards = bottomCards;
        this.deck = deck;
        this.topBuildingIDs = topBuildingIDs;
        this.bottomBuildingIDs = bottomBuildingIDs;
        this.buildingPoolIDs = buildingPoolIDs;
    }

    public List<CardDTO> getTopCards() { return topCards; }
    public List<CardDTO> getBottomCards() { return bottomCards; }
    public List<CardDTO> getDeck() { return deck; }
    public List<Integer> getTopBuildingIDs() { return topBuildingIDs; }
    public List<Integer> getBottomBuildingIDs() { return bottomBuildingIDs; }
    public List<Integer> getBuildingPoolIDs() { return buildingPoolIDs; }
}
