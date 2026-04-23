package it.polimi.ingsw.am25.server.webLayer.DTOs;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class MarketDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<CardDTO> topCards;
    private List<CardDTO> bottomCards;
    private List<BuildingDTO> topBuildings;
    private List<BuildingDTO> bottomBuildings;

    /**
     * Creates a new market dto instance.
     * @param topCards parameter topCards.
     * @param bottomCards parameter bottomCards.
     * @param topBuildings parameter topBuildings.
     * @param bottomBuildings parameter bottomBuildings.
     */
    public MarketDTO(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings, List<BuildingDTO> bottomBuildings) {
        this.topCards = topCards;
        this.bottomCards = bottomCards;
        this.topBuildings = topBuildings;
        this.bottomBuildings = bottomBuildings;
    }

    /**
     * Returns top cards.
     * @return the result of the operation.
     */
    public List<CardDTO> getTopCards() {
        return topCards;
    }

    /**
     * Returns bottom cards.
     * @return the result of the operation.
     */
    public List<CardDTO> getBottomCards() {
        return bottomCards;
    }

    /**
     * Returns top buildings.
     * @return the result of the operation.
     */
    public List<BuildingDTO> getTopBuildings() {
        return topBuildings;
    }

    /**
     * Returns bottom buildings.
     * @return the result of the operation.
     */
    public List<BuildingDTO> getBottomBuildings() {
        return bottomBuildings;
    }

    /**
     * Sets top cards.
     * @param topCards parameter topCards.
     */
    public void setTopCards(List<CardDTO> topCards) {
        this.topCards = topCards;
    }

    /**
     * Sets bottom cards.
     * @param bottomCards parameter bottomCards.
     */
    public void setBottomCards(List<CardDTO> bottomCards) {
        this.bottomCards = bottomCards;
    }

    /**
     * Sets top buildings.
     * @param topBuildings parameter topBuildings.
     */
    public void setTopBuildings(List<BuildingDTO> topBuildings) {
        this.topBuildings = topBuildings;
    }

    /**
     * Sets bottom buildings.
     * @param bottomBuildings parameter bottomBuildings.
     */
    public void setBottomBuildings(List<BuildingDTO> bottomBuildings) {
        this.bottomBuildings = bottomBuildings;
    }
}
