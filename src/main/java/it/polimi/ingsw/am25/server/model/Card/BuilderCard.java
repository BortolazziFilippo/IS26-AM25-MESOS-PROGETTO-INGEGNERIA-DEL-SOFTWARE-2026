package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents a Builder tribe member card. Builders reduce the food cost of purchasing buildings
 * and award prestige points at the end of the game.
 */
public class BuilderCard extends Card {
    private final int foodDiscount;
    private int finalPrestigePoint;
    private final int builderID;

    /**
     * @param era                the era this card belongs to.
     * @param cardType           the card type (should be {@code CARD_TYPE.BUILDER}).
     * @param foodDiscount       food discount applied when buying a building.
     * @param finalPrestigePoint prestige points awarded at end of game.
     * @param builderID          unique identifier shared by cards with the same effect.
     */
    public BuilderCard(ERA era, CARD_TYPE cardType, int foodDiscount, int finalPrestigePoint, int builderID) {
        this.era = era;
        this.cardType = cardType;
        this.foodDiscount = foodDiscount;
        this.finalPrestigePoint = finalPrestigePoint;
        this.builderID = builderID;
    }

    /**
     * @return the food discount this builder provides when buying a building.
     */
    public int getFoodDiscount() {
        return foodDiscount;
    }

    public int getBuilderID() {
        return builderID;
    }

    /**
     * @param finalPrestigePoint the new end-game prestige-point value (used by doubling effects).
     */
    public void setFinalPrestigePoint(int finalPrestigePoint) {
        this.finalPrestigePoint = finalPrestigePoint;
    }

    /**
     * @return the prestige points this builder awards at end of game.
     */
    public int getFinalPrestigePoint() {
        return finalPrestigePoint;
    }

    /**
     * Adds this card to the player's tribe.
     */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * @param obj the object to compare against.
     * @return true if {@code obj} is a BuilderCard with the same era, type, discount, and prestige points.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BuilderCard toCompare) {
            return toCompare.cardType == this.cardType && toCompare.era == this.era && toCompare.foodDiscount == this.foodDiscount
                    && toCompare.finalPrestigePoint == this.finalPrestigePoint;
        } else {
            return false;
        }
    }

    /**
     * @return a CardDTO snapshot of this card for network transfer.
     */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }

    @Override
    public boolean matchesDTO(CardDTO dto) {
        return dto.getCardType() == CARD_TYPE.BUILDER
            && dto.getEra() == this.era
            && dto.getBuilderID() == this.builderID;
    }
}
