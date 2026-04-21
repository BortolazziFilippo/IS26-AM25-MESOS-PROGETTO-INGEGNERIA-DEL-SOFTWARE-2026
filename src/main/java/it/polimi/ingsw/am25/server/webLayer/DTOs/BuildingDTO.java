package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

import java.io.Serial;
import java.io.Serializable;

public class BuildingDTO extends CardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    private  int buildingID;
    private  int foodCost;
    private  int endGamePP;
    private  EVENT_TYPE applyOn;

    public BuildingDTO(BuildingCard buildingCard) {
        super(buildingCard.getEra());
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

    public int getBuildingID() {
        return buildingID;
    }
}
