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
    /** Cards displayed in the top draw row of the market. */
    private List<CardDTO> topCards;
    /** Cards displayed in the bottom draw row of the market. */
    private List<CardDTO> bottomCards;
    /** Buildings displayed in the top building row of the market. */
    private List<BuildingDTO> topBuildings;
    /** Buildings displayed in the bottom building row of the market. */
    private List<BuildingDTO> bottomBuildings;

    /**
     * Creates a MarketDTO with the given row contents.
     *
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
     * Returns the cards in the top row of the market.
     *
     * @return the top card row.
     */
    public List<CardDTO> getTopCards() {
        return topCards;
    }

    /**
     * Returns the cards in the bottom row of the market.
     *
     * @return the bottom card row.
     */
    public List<CardDTO> getBottomCards() {
        return bottomCards;
    }

    /**
     * Returns the buildings in the top row of the market.
     *
     * @return the top building row.
     */
    public List<BuildingDTO> getTopBuildings() {
        return topBuildings;
    }

    /**
     * Returns the buildings in the bottom row of the market.
     *
     * @return the bottom building row.
     */
    public List<BuildingDTO> getBottomBuildings() {
        return bottomBuildings;
    }

    /**
     * Replaces the top-row card list.
     *
     * @param topCards the new top-row card list.
     */
    public void setTopCards(List<CardDTO> topCards) {
        this.topCards = topCards;
    }

    /**
     * Replaces the bottom-row card list.
     *
     * @param bottomCards the new bottom-row card list.
     */
    public void setBottomCards(List<CardDTO> bottomCards) {
        this.bottomCards = bottomCards;
    }

    /**
     * Replaces the top-row building list.
     *
     * @param topBuildings the new top-row building list.
     */
    public void setTopBuildings(List<BuildingDTO> topBuildings) {
        this.topBuildings = topBuildings;
    }

    /**
     * Replaces the bottom-row building list.
     *
     * @param bottomBuildings the new bottom-row building list.
     */
    public void setBottomBuildings(List<BuildingDTO> bottomBuildings) {
        this.bottomBuildings = bottomBuildings;
    }
}
