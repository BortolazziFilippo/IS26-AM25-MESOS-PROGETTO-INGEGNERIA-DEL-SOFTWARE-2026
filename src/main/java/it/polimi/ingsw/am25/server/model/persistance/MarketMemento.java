package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

import java.util.List;

/**
 * Memento snapshot of the market state at end of round.
 */
public record MarketMemento(List<CardDTO> topCards, List<CardDTO> bottomCards, List<CardDTO> deck,
                            List<Integer> topBuildingIDs, List<Integer> bottomBuildingIDs,
                            List<Integer> buildingPoolIDs) {
}
