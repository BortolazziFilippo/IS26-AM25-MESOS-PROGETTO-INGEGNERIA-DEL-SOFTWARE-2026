package it.polimi.ingsw.am25.server.model.Board;

/**
 * A default (home) tile where players rest between rounds.
 * Each tile has a food reward (positive) or penalty (negative) that is applied when a player
 * returns to it at the end of the playing phase.
 */
public class DefaultTile extends Tile {
    private final int foodPerSlotPosition;
    /**
     * Creates a default tile with the specified food reward/penalty.
     *
     * @param foodPerSlotPosition food added (positive) or subtracted (negative) when a player occupies this tile
     */
    public DefaultTile( int foodPerSlotPosition ) {
        this.foodPerSlotPosition=foodPerSlotPosition;
        super(null);
    }

    /**
     * Returns the food reward or penalty associated with this tile slot.
     *
     * @return food value (positive = reward, negative = penalty)
     */
    public int getFoodPerSlotPosition() {
        return foodPerSlotPosition;
    }
}
