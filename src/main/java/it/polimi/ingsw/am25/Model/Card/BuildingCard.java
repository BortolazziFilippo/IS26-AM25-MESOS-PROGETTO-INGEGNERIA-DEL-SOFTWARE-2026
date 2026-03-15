package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Effect.Building.BuildingEffect;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

public class BuildingCard extends Card {
    private final int buildingID;
    private  BuildingEffect buildingEffect;
    private final int foodCost;
    private final int endgamePP;
    private final EVENT_TYPE applyOn;

    public BuildingCard(ERA era,int buildingID, int foodCost, int endgamePP, EVENT_TYPE applyOn) {
        this.era=era;
        this.buildingID = buildingID;

        this.foodCost = foodCost;
        this.endgamePP = endgamePP;
        this.applyOn = applyOn;
    }
    public void setBuildingEffect(BuildingEffect buildingEffect){
        this.buildingEffect=buildingEffect;
    }
    public int getBuildingID() {
        return buildingID;
    }

    public BuildingEffect getBuildingEffect() {
        return buildingEffect;
    }

    public int getFoodCost() {
        return foodCost;
    }

    public int getEndgamePP() {
        return endgamePP;
    }

    public EVENT_TYPE getApplyOn() {
        return applyOn;
    }

    public void applyEventEffect(Player player) {
        this.buildingEffect.applyEffect(player);
    }
}
