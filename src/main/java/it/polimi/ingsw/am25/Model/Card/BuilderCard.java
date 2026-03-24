package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;

public class BuilderCard extends Card{
    private int foodDiscount;
    private int finalPrestigePoint;

    /**
     * Default constructor BuilerCard
     * @param era Card ERA
     *@param cardType Card type
     * @param foodDiscount discount when buying building
     * @param finalPrestigePoint PP given at end game
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
}
