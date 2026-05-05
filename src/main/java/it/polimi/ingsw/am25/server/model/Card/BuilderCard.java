package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents a Builder tribe member card. Builders reduce the food cost of purchasing buildings
 * and award prestige points at the end of the game.
 */
public class BuilderCard extends Card{
    private final int foodDiscount;
    private int finalPrestigePoint;
    private final int builderID;

    /**
     * Default constructor for BuilderCard.
     *
     * @param era                Card ERA
     * @param cardType           Card type
     * @param foodDiscount       food discount applied when buying a building
     * @param finalPrestigePoint prestige points awarded at end of game
     * @param builderID          unique identifier shared by cards with the same effect
     */
    public BuilderCard(ERA era, CARD_TYPE cardType, int foodDiscount, int finalPrestigePoint, int builderID){
        this.era = era;
        this.cardType=cardType;
        this.foodDiscount = foodDiscount;
        this.finalPrestigePoint = finalPrestigePoint;
        this.builderID = builderID;
    }

    /**
     * Returns food discount.
     * @return the result of the operation.
     */
    public int getFoodDiscount() {
        return foodDiscount;
    }

    public int getBuilderID() {
        return builderID;
    }

    /**
     * Sets final prestige point.
     * @param finalPrestigePoint parameter finalPrestigePoint.
     */
    public void setFinalPrestigePoint(int finalPrestigePoint) {
        this.finalPrestigePoint = finalPrestigePoint;
    }

    /**
     * Returns final prestige point.
     * @return the result of the operation.
     */
    public int getFinalPrestigePoint() {
        return finalPrestigePoint;
    }
    /**
     * Executes add card to player.
     * @param player parameter player.
     */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * Executes equals.
     * @param obj parameter obj.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BuilderCard toCompare){
            return toCompare.cardType == this.cardType && toCompare.era == this.era && toCompare.foodDiscount == this.foodDiscount
                    && toCompare.finalPrestigePoint == this.finalPrestigePoint;
        }else {
            return false;
        }
    }
    /**
     * Executes to dto.
     * @return the result of the operation.
     */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}
