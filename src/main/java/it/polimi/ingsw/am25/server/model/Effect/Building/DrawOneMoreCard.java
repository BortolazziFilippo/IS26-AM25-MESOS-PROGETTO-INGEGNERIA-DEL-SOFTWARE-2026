package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
/**
 * Building effect that entitles the player to draw one extra card per round from the top list.
 * The actual card selection is delegated to the controller layer, which must invoke the
 * appropriate market method once this effect is triggered.
 */
public class DrawOneMoreCard extends BuildingEffect {
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    private final boolean turnFinished = false; /* flag to signal the end of a round; the corresponding game-layer method still needs to be reviewed */

    public DrawOneMoreCard() {
    }

    /**
     * Applies the "draw one more card" effect.
     * The actual card selection requires controller-layer intervention
     * (the player must choose a position in the top card list).
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        UtilitiesFunction.logInfo(LOG_PREFIX,
                "DrawOneMoreCard: requesting extra card draw for player '" + player.getNickname() + "'");
        player.requestExtraDraw();
    }
}
