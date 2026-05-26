package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

import java.util.List;

/**
 * Memento snapshot of the market state at end of round.
 *
 * @param topCards         the cards in the top draw row.
 * @param bottomCards      the cards in the bottom draw row.
 * @param deck             the remaining draw pile.
 * @param topBuildingIDs   the IDs of buildings displayed in the top building row.
 * @param bottomBuildingIDs the IDs of buildings displayed in the bottom building row.
 * @param buildingPoolIDs  the IDs of all remaining buildings in the pool.
 */
public record MarketMemento(List<CardDTO> topCards, List<CardDTO> bottomCards, List<CardDTO> deck,
                            List<Integer> topBuildingIDs, List<Integer> bottomBuildingIDs,
                            List<Integer> buildingPoolIDs) {
}
