package it.polimi.ingsw.am25.server.webLayer.DTOs;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Data-transfer object for the market state, carrying the top and bottom card rows
 * and the top and bottom building rows.
 */
public class MarketDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<CardDTO> topCards;
    private List<CardDTO> bottomCards;
    private List<BuildingDTO> topBuildings;
    private List<BuildingDTO> bottomBuildings;

    /**
     * @param topCards        cards in the top row of the market.
     * @param bottomCards     cards in the bottom row of the market.
     * @param topBuildings    buildings in the top row of the market.
     * @param bottomBuildings buildings in the bottom row of the market.
     */
    public MarketDTO(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings, List<BuildingDTO> bottomBuildings) {
        this.topCards = topCards;
        this.bottomCards = bottomCards;
        this.topBuildings = topBuildings;
        this.bottomBuildings = bottomBuildings;
    }

    /**
     * @return cards in the top row of the market.
     */
    public List<CardDTO> getTopCards() {
        return topCards;
    }

    /**
     * @return cards in the bottom row of the market.
     */
    public List<CardDTO> getBottomCards() {
        return bottomCards;
    }

    /**
     * @return buildings in the top row of the market.
     */
    public List<BuildingDTO> getTopBuildings() {
        return topBuildings;
    }

    /**
     * @return buildings in the bottom row of the market.
     */
    public List<BuildingDTO> getBottomBuildings() {
        return bottomBuildings;
    }

    /**
     * @param topCards the new top-row card list.
     */
    public void setTopCards(List<CardDTO> topCards) {
        this.topCards = topCards;
    }

    /**
     * @param bottomCards the new bottom-row card list.
     */
    public void setBottomCards(List<CardDTO> bottomCards) {
        this.bottomCards = bottomCards;
    }

    /**
     * @param topBuildings the new top-row building list.
     */
    public void setTopBuildings(List<BuildingDTO> topBuildings) {
        this.topBuildings = topBuildings;
    }

    /**
     * @param bottomBuildings the new bottom-row building list.
     */
    public void setBottomBuildings(List<BuildingDTO> bottomBuildings) {
        this.bottomBuildings = bottomBuildings;
    }
}
