package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data-transfer object for a default tile, carrying the food-per-slot-position value
 * used to compute food rewards when players stand on the tile at end of round.
 */
public class DefaultTileDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int foodPerSlotPosition;

    /**
     * Creates a new default tile dto instance.
     * @param defaultTile parameter defaultTile.
     */
    public DefaultTileDTO(DefaultTile defaultTile) {
        this.foodPerSlotPosition = defaultTile.getFoodPerSlotPosition();
    }

    /**
     * Returns the food reward per slot position for this default tile.
     * @return food per slot position.
     */
    public int getFoodPerSlotPosition() {
        return foodPerSlotPosition;
    }
}
