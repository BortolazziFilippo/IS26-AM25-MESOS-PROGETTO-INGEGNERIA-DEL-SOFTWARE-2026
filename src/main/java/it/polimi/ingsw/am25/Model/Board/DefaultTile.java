package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class DefaultTile extends Tile {
    private int foodPerSlotPosition;
    private  Player playerOn;

    public DefaultTile( int foodPerSlotPosition ) {
        this.foodPerSlotPosition=foodPerSlotPosition;
    }

    public void insertPlayer(Player player, int Position) {

    }

    public void removePlayer(int Position){

    }

    public int getFoodPerSlot(int Position) {

    }

    public List<Player> getPlayerPosition() {

    }
}
