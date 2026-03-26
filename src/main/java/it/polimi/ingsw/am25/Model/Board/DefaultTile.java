package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class DefaultTile extends Tile {
    private final int foodPerSlotPosition;

    public DefaultTile( int foodPerSlotPosition ) {
        this.foodPerSlotPosition=foodPerSlotPosition;
        super(null);
    }

    public int getFoodPerSlotPosition() {
        return foodPerSlotPosition;
    }
}
