package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;

/**
 * Building effect triggered during a hunt event: awards 1 food plus 1 prestige point
 * per Hunter card in the player's tribe.
 */
public class OnEventHuntOneFoodAndOnePPPerHunter extends BuildingEffect{

    /**
     * Default constructor for OnEventHuntOneFoodAndOnePPPerHunter.
     */
    public OnEventHuntOneFoodAndOnePPPerHunter() {
    }
    /**
     * Awards the player 1 food and 1 PP per Hunter card in their tribe.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        player.managePP((int)player.getTribe().stream().filter(card -> card.getCardType()== CARD_TYPE.HUNTER).count());
        player.manageFoodAndPP(1);
    }
}
