package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;


/**
 * Building effect that awards 25 prestige points at the end of the game.
 */
public class TwentyFivePPEndGame extends BuildingEffect{

    /**
     * Default constructor for TwentyFivePPEndGame.
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
        player.managePP(25);

    }
}
