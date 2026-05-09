package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

/**
 * Building effect triggered during a hunt event: awards 1 food plus 1 prestige point
 * per Hunter card in the player's tribe.
 */
public class OnEventHuntOneFoodAndOnePPPerHunter extends BuildingEffect{
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";

    public OnEventHuntOneFoodAndOnePPPerHunter() {
    }
    /**
     * Awards the player 1 food and 1 PP per Hunter card in their tribe.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        int hunterCount = (int) player.getTribe().stream().filter(card -> card.getCardType() == CARD_TYPE.HUNTER).count();
        UtilitiesFunction.logInfo(LOG_PREFIX,
                "OnEventHuntOneFoodAndOnePPPerHunter: player '" + player.getNickname() +
                        "' has " + hunterCount + " Hunter(s), awarding 1 food and " + hunterCount + " PP (1 PP per Hunter)");
        player.managePP(hunterCount);
        player.manageFoodAndPP(1);
    }
}
