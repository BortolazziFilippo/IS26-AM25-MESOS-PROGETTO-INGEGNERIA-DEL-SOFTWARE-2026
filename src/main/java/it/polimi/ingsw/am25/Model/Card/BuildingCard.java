package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Effect.Building.BuildingEffect;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

public class BuildingCard extends Card {
    private final int buildingID;
    private  BuildingEffect buildingEffect;
    private final int foodCost;
    private final int endgamePP;
    private final EVENT_TYPE applyOn;

    /**
     * Default BuildingCard Constructor
     * @param era Card ERA
     * @param cardType Card type
     * @param buildingID ID of the building
     * @param foodCost cost of the building
     * @param endgamePP pp givent at end game
     * @param applyOn when the buildingeffect is triggered
     */
    public BuildingCard(ERA era, CARD_TYPE cardType, int buildingID, int foodCost, int endgamePP, EVENT_TYPE applyOn) {
        this.era=era;
        this.cardType=cardType;
        this.buildingID = buildingID;

        this.foodCost = foodCost;
        this.endgamePP = endgamePP;
        this.applyOn = applyOn;
    }

    /**
     * used to set the building effect afterward
     * @param buildingEffect  building effect to bind
     */
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

    /**
     * Method for applying the building effect
     * @param player the player to apply the effect to
     */
    public void applyBuildingEffect(Player player) {
        this.buildingEffect.applyEffect(player);
    }
    @Override
    public void addCardToPlayer(Player player) {
        player.addBuilding(this);
    }
}
