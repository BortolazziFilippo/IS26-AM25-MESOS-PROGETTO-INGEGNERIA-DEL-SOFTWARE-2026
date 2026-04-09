package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;

public class BuilderCard extends Card{
    private final int foodDiscount;
    private int finalPrestigePoint;

    /**
     * Default constructor for BuilderCard.
     *
     * @param era                Card ERA
     * @param cardType           Card type
     * @param foodDiscount       food discount applied when buying a building
     * @param finalPrestigePoint prestige points awarded at end of game
     */
    public BuilderCard(ERA era, CARD_TYPE cardType, int foodDiscount, int finalPrestigePoint){
        this.era = era;
        this.cardType=cardType;
        this.foodDiscount = foodDiscount;
        this.finalPrestigePoint = finalPrestigePoint;
    }

    public int getFoodDiscount() {
        return foodDiscount;
    }

    public void setFinalPrestigePoint(int finalPrestigePoint) {
        this.finalPrestigePoint = finalPrestigePoint;
    }

    public int getFinalPrestigePoint() {
        return finalPrestigePoint;
    }
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BuilderCard toCompare){
            return toCompare.cardType == this.cardType && toCompare.era == this.era && toCompare.foodDiscount == this.foodDiscount
                    && toCompare.finalPrestigePoint == this.finalPrestigePoint;
        }else {
            return false;
        }
    }
}
