package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data-transfer object for a default tile, carrying the food-per-slot-position value
 * used to compute food rewards when players stand on the tile at end of round.
 *
 * @param foodPerSlotPosition the food reward granted for each slot position on this tile.
 */
public record DefaultTileDTO(int foodPerSlotPosition) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a DefaultTileDTO from the given {@link DefaultTile} domain object.
     *
     * @param foodPerSlotPosition the source DefaultTile whose food-per-slot value is copied.
     */
    public DefaultTileDTO(DefaultTile foodPerSlotPosition) {
        this(foodPerSlotPosition.getFoodPerSlotPosition());
    }

    /**
     * Returns the food reward per slot position for this default tile.
     *
     * @return food per slot position.
     */
    @Override
    public int foodPerSlotPosition() {
        return foodPerSlotPosition;
    }
}
