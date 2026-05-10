package it.polimi.ingsw.am25.server.model.Effect.Event;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.util.List;

/**
 * Event effect for a sustenance event: each tribe member costs food,
 * Gatherers discount the cost, and players who cannot pay lose prestige points instead.
 */
public class SustenanceEvent extends EventEffect {
    private static final String LOG_PREFIX = "[SERVER][EVENT]";
    private final int foodPerCharacter;
    private final int PPLost;

    /**
     * Constructor for SustenanceEvent.
     *
     * @param foodPerCharacter food cost per tribe member
     * @param PPLost           prestige points lost per food unit the player cannot pay (positive value)
     */
    public SustenanceEvent(int foodPerCharacter, int PPLost) {
        this.foodPerCharacter = foodPerCharacter;
        this.PPLost = -PPLost;
    }

    /**
     * Solves the sustenance event for all players:
     * each tribe member costs {@code foodPerCharacter} food; gatherers discount 3 food each.
     * Building effects tagged SUSTENANCE are applied first (they may add food back).
     * If a player cannot pay the full cost, their food is set to 0 and they lose
     * {@code |shortfall| * |PPLost|} prestige points instead.
     * The net result of this event can never increase a player's food above the value
     * they had at the start of the event.
     *
     * @param playersList list of players who participate in the event
     */
    @Override
    public void solveEvent(List<Player> playersList) {
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "SUSTENANCE event started: food cost per villager=" + foodPerCharacter +
                        ", PP loss per missing food=" + PPLost
        );
        int numberOfVillager;
        int foodToSubtract;
        int foodDiscount;
        int startingFood;
        for (Player player : playersList) {
            startingFood = player.getFood();
            numberOfVillager = -(int) player.getTribe().size() * foodPerCharacter;
            foodDiscount = (int) player.getTribe().stream().filter(card -> card.getCardType() == CARD_TYPE.GATHERER).count() * 3;
            foodToSubtract = numberOfVillager + foodDiscount;
            UtilitiesFunction.logInfo(
                    LOG_PREFIX,
                    "SUSTENANCE event on player '" + player.getNickname() + "': tribeSize=" + player.getTribe().size() +
                            ", gathererDiscount=" + foodDiscount + ", provisional food delta=" + foodToSubtract
            );

            // gatherers cannot produce a net food gain during sustenance
            if (foodToSubtract > 0) {
                foodToSubtract = 0;
            }
            // apply SUSTENANCE building effects (e.g. DiscountFoodOnSustenance) before subtracting
            List<BuildingCard> triggeredBuildings = player.getBuildingCards().stream()
                    .filter(buildingCard -> buildingCard.getApplyOn() == EVENT_TYPE.SUSTENANCE)
                    .toList();
            triggeredBuildings.forEach(buildingCard -> buildingCard.applyBuildingEffect(player));
            UtilitiesFunction.logInfo(
                    LOG_PREFIX,
                    "SUSTENANCE event triggered " + triggeredBuildings.size() + " building effect(s) for player '" +
                            player.getNickname() + "'"
            );
            // check if the player can afford the food cost
            // foodToSubtract is <= 0, so player.getFood() + foodToSubtract gives the remaining food
            if (player.getFood() + foodToSubtract < 0) {
                // player cannot pay: drain food to 0 and convert shortfall to PP loss
                int shortfall = -(player.getFood() + foodToSubtract); // positive
                UtilitiesFunction.logInfo(
                        LOG_PREFIX,
                        "Player '" + player.getNickname() + "' cannot cover sustenance cost: shortfall=" + shortfall +
                                ", food set to 0, PP delta=" + (-shortfall * PPLost)
                );
                player.manageFoodAndPP(-player.getFood()); // set food to 0
                player.managePP(-shortfall * PPLost); // PPLost is positive; negate to lose PP
            } else {
                UtilitiesFunction.logInfo(
                        LOG_PREFIX,
                        "Player '" + player.getNickname() + "' pays sustenance cost with food delta=" + foodToSubtract
                );
                player.manageFoodAndPP(foodToSubtract);
            }
            // sustenance must never be a net food gain for the player
            if (player.getFood() > startingFood) {
                UtilitiesFunction.logInfo(
                        LOG_PREFIX,
                        "Applying anti-gain clamp for player '" + player.getNickname() + "': food " +
                                player.getFood() + " -> " + startingFood
                );
                player.manageFoodAndPP(-(player.getFood() - startingFood));
            }
        }
        UtilitiesFunction.logInfo(LOG_PREFIX, "SUSTENANCE event completed");
    }
}
