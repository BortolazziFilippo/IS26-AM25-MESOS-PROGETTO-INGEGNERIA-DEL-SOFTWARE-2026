package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.ERA;

public class BuilderCard extends Card{
    private int foodDiscount;
    private int finalPrestigePoint;

    public BuilderCard(ERA era, int foodDiscount, int finalPrestigePoint){
        this.era = era;
        this.foodDiscount = foodDiscount;
        this.finalPrestigePoint = finalPrestigePoint;
    }

    public int getFoodDiscount() {
        return foodDiscount;
    }

    public int getFinalPrestigePoint() {
        return finalPrestigePoint;
    }
}
