package it.polimi.ingsw.am25.Model.Factory.DTO;

import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;

public class BuildingDTO {
    private int buildingID;
    private int foodCost;
    private int endGamePP;
    private EVENT_TYPE applyOn;
    private ERA era;

    /**
     * default BuildingDTO constructor
     * @param buildingID building id
     * @param foodCost cost of the building
     * @param endGamePP PP given at end game
     * @param applyOn when building is triggered
     * @param era ERA of the building
     */
    public BuildingDTO(int buildingID, int foodCost, int endGamePP, EVENT_TYPE applyOn, ERA era) {
        this.buildingID = buildingID;
        this.foodCost = foodCost;
        this.endGamePP = endGamePP;
        this.applyOn = applyOn;
        this.era = era;
    }

    public int getFoodCost() {
        return foodCost;
    }

    public int getEndGamePP() {
        return endGamePP;
    }

    public EVENT_TYPE getApplyOn() {
        return applyOn;
    }

    public ERA getEra() {
        return era;
    }

    public int getBuildingID() {
        return buildingID;
    }
}
