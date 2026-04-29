package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Effect.Building.BuildingEffect;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents a building card in Mesos. Buildings are purchased with food during the
 * resolve-action phase and may trigger special effects during the game or award
 * prestige points at the end.
 */
public class BuildingCard extends Card {
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
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

    /**
     * Returns the unique identifier of this building.
     *
     * @return building ID
     */
    public int getBuildingID() {
        return buildingID;
    }

    /**
     * Returns the food cost required to purchase this building.
     *
     * @return food cost
     */
    public int getFoodCost() {
        return foodCost;
    }

    /**
     * Returns the prestige points this building awards at the end of the game.
     *
     * @return end-game prestige points
     */
    public int getEndgamePP() {
        return endgamePP;
    }

    /**
     * Returns the event type that triggers this building's effect.
     *
     * @return the {@link EVENT_TYPE} this building reacts to
     */
    public EVENT_TYPE getApplyOn() {
        return applyOn;
    }

    /**
     * Method for applying the building effect
     * @param player the player to apply the effect to
     */
    public void applyBuildingEffect(Player player) {
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "Applying building #" + buildingID + " (trigger: " + applyOn + ") to player '" + player.getNickname() + "'"
        );
        this.buildingEffect.applyEffect(player);
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "Completed building #" + buildingID + " for player '" + player.getNickname() + "'"
        );
    }
    /**
     * Executes add card to player.
     * @param player parameter player.
     */
    @Override
    public void addCardToPlayer(Player player) {
        player.addBuilding(this);
    }

    /**
     * Executes equals.
     * @param obj parameter obj.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BuildingCard toCompare){
            return toCompare.buildingID == this.buildingID;
        }else{
            return false;
        }
    }

    /**
     * Executes to dto.
     * @return the result of the operation.
     */
    @Override
    public CardDTO toDTO() {
        return new BuildingDTO(this);
    }
}
