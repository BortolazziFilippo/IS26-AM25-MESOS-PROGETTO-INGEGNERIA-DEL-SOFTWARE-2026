package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

/**
 * Building effect that awards 1 extra food when the player returns to a default tile
 * whose food reward is non-negative (i.e. an eligible default tile).
 */
public class PlusOneFoodOnReturnDefaultTile extends BuildingEffect {
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    private BoardView boardView;

    /**
     * Constructs a building effect that awards 1 extra food when the player returns to an eligible default tile.
     */
    public PlusOneFoodOnReturnDefaultTile() {
    }

    /**
     * Sets the board view used to determine whether the player is on an eligible tile.
     *
     * @param boardView the board view
     */
    public void setBoardView(BoardView boardView) {
        this.boardView = boardView;
    }

    /**
     * Awards 1 food if the player is currently on a default tile with a non-negative food reward.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        if (boardView.isPlayerOnAnEligibleDefaultTile(player)) {
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "PlusOneFoodOnReturnDefaultTile: player '" + player.getNickname() +
                            "' is on an eligible default tile, awarding +1 food");
            player.manageFoodAndPP(1);
        } else {
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "PlusOneFoodOnReturnDefaultTile: player '" + player.getNickname() +
                            "' is not on an eligible default tile, no food awarded");
        }

    }
}
