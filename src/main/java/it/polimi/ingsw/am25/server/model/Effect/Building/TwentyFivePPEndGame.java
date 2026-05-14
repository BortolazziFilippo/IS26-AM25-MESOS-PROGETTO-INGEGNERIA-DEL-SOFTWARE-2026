package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;


/**
 * Building effect that awards 25 prestige points at the end of the game.
 */
public class TwentyFivePPEndGame extends BuildingEffect {
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";

    /**
     * Constructs a building effect that awards 25 prestige points at the end of the game.
     */
    public TwentyFivePPEndGame() {
    }

    /**
     * Awards the player 25 prestige points.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        UtilitiesFunction.logInfo(LOG_PREFIX,
                "TwentyFivePPEndGame: awarding 25 PP to player '" + player.getNickname() + "' at end of game");
        player.managePP(25);

    }
}
