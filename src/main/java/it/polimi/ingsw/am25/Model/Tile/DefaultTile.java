package it.polimi.ingsw.am25.Model.Tile;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class DefaultTile extends Tile {
    private final List<Integer> foodPerSlotPosition;
    private final List<Player> playerPosition;
    public DefaultTile( int numberSlot ) {
        this.foodPerSlotPosition = new ArrayList<>();
        this.playerPosition = new ArrayList<>();
        switch (numberSlot) {
            case 2:
                foodPerSlotPosition.add(0, 1);
                foodPerSlotPosition.add(1, -1);
                break;
            case 3:
                foodPerSlotPosition.add(0, 2);
                foodPerSlotPosition.add(1, 0);
                foodPerSlotPosition.add(2, -1);
                break;
            case 4:
                foodPerSlotPosition.add(0, 2);
                foodPerSlotPosition.add(1, 1);
                foodPerSlotPosition.add(2, 0);
                foodPerSlotPosition.add(3, -1);
                break;
            case 5:
                foodPerSlotPosition.add(0, 3);
                foodPerSlotPosition.add(1, 1);
                foodPerSlotPosition.add(2, 0);
                foodPerSlotPosition.add(3, 0);
                foodPerSlotPosition.add(4, -1);
                break;
        }

    }

    public void insertPlayer(Player player, int Position) {
        if(this.playerPosition.get(Position)==null) {
            this.playerPosition.add(Position, player);
        }
        else {
            System.err.println("Posizione già occupata!");
        }
    }

    public void removePlayer(int Position){
        if(this.playerPosition.get(Position)!=null) {
            this.playerPosition.set(Position, null);
        }
    }

    public int getFoodPerSlot(int Position) {
        return this.foodPerSlotPosition.get(Position);
    }

    public List<Player> getPlayerPosition() {
        return playerPosition;
    }
}
