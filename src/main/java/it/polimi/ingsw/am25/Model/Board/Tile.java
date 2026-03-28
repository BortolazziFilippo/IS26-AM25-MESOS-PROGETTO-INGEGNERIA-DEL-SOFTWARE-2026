package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Player.Player;

public abstract class Tile {
    private Player playerOn;

    public Tile(Player playerOn) {
        this.playerOn = playerOn;
    }

    public Player getPlayerOn() {
        return playerOn;
    }

    public  boolean isOccupied(){
        return playerOn != null;
    }
    public void placePlayer(Player player){
        this.playerOn=player;
    }
    public void removePlayer(){
        this.playerOn=null;
    }

}
