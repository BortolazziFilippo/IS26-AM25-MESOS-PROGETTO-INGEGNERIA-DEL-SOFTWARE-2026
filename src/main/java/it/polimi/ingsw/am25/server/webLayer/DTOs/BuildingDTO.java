package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

public class BuildingDTO {
    private  int buildingID;
    private  int foodCost;
    private  int endGamePP;
    private  EVENT_TYPE applyOn;
    private  ERA era;


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
