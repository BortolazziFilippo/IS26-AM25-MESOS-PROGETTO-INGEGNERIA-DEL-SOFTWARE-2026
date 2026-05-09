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
     * @param era       the era this card belongs to.
     * @param cardType  the card type (should be {@code CARD_TYPE.BUILDING}).
     * @param buildingID unique identifier for this building.
     * @param foodCost  food cost to purchase this building.
     * @param endgamePP prestige points awarded at end of game.
     * @param applyOn   the event type that triggers this building's effect.
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
     * Binds the effect strategy to this building card.
     * Called by the factory after construction because the effect depends on the building ID.
     *
     * @param buildingEffect the effect to execute when this building is triggered.
     */
    public void setBuildingEffect(BuildingEffect buildingEffect){
        this.buildingEffect=buildingEffect;
    }

    /** @return the unique identifier of this building. */
    public int getBuildingID() {
        return buildingID;
    }

    /** @return the food cost required to purchase this building. */
    public int getFoodCost() {
        return foodCost;
    }

    /** @return the prestige points this building awards at the end of the game. */
    public int getEndgamePP() {
        return endgamePP;
    }

    /** @return the event type that triggers this building's effect. */
    public EVENT_TYPE getApplyOn() {
        return applyOn;
    }

    /**
     * Fires the bound building effect on the given player.
     *
     * @param player the player who owns this building.
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

    /** Adds this building to the player's owned buildings. */
    @Override
    public void addCardToPlayer(Player player) {
        player.addBuilding(this);
    }

    /**
     * @param obj the object to compare against.
     * @return true if {@code obj} is a BuildingCard with the same building ID.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BuildingCard toCompare){
            return toCompare.buildingID == this.buildingID;
        }else{
            return false;
        }
    }

    /** @return a BuildingDTO snapshot of this card for network transfer. */
    @Override
    public CardDTO toDTO() {
        return new BuildingDTO(this);
    }
}
