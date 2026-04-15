package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Player.Player;

/**
 * Building effect that awards 1 extra food when the player returns to a default tile
 * whose food reward is non-negative (i.e. an eligible default tile).
 */
public class PlusOneFoodOnReturnDefaultTile extends BuildingEffect{
    private BoardView boardView;

    /**
     * Default constructor for PlusOneFoodOnReturnDefaultTile.
     * {@link #setBoardView(BoardView)} must be called before the effect can be applied.
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
        if(boardView.isPlayerOnAnEligibleDefaultTile(player)){
            player.manageFoodAndPP(1);
        }

    }
}
