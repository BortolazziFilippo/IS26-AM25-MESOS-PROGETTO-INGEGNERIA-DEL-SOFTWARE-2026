package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;

import java.util.List;

public class MarketDTO {
    private List<CardDTO> topCards;
    private List<CardDTO> bottomCards;
    private List<BuildingDTO> topBuildings;
    private List<BuildingDTO> bottomBuildings;

    public MarketDTO(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings, List<BuildingDTO> bottomBuildings) {
        this.topCards = topCards;
        this.bottomCards = bottomCards;
        this.topBuildings = topBuildings;
        this.bottomBuildings = bottomBuildings;
    }

    public List<CardDTO> getTopCards() {
        return topCards;
    }

    public List<CardDTO> getBottomCards() {
        return bottomCards;
    }

    public List<BuildingDTO> getTopBuildings() {
        return topBuildings;
    }

    public List<BuildingDTO> getBottomBuildings() {
        return bottomBuildings;
    }

    public void setTopCards(List<CardDTO> topCards) {
        this.topCards = topCards;
    }

    public void setBottomCards(List<CardDTO> bottomCards) {
        this.bottomCards = bottomCards;
    }

    public void setTopBuildings(List<BuildingDTO> topBuildings) {
        this.topBuildings = topBuildings;
    }

    public void setBottomBuildings(List<BuildingDTO> bottomBuildings) {
        this.bottomBuildings = bottomBuildings;
    }
}
