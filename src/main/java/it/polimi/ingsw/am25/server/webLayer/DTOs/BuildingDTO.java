package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

import java.io.Serial;
import java.io.Serializable;

public class BuildingDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private  int buildingID;
    private  int foodCost;
    private  int endGamePP;
    private  EVENT_TYPE applyOn;
    private  ERA era;

    public BuildingDTO(BuildingCard buildingCard) {
        this.buildingID = buildingCard.getBuildingID();
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
