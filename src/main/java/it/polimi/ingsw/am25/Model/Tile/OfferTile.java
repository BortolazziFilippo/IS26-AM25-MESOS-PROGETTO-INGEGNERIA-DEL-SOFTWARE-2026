package it.polimi.ingsw.am25.Model.Tile;

import it.polimi.ingsw.am25.Model.Player.Player;

public class OfferTile extends Tile{
    private Action ActionAvailable;
    private int position;
    private Player palyerOn;

    public OfferTile(int drawTop, int drawBot, int position){
        this.ActionAvailable = new Action(drawTop, drawBot);
        this.position = position;
        this.palyerOn = null;

    }

    public Action getActionAvailable() {
        return ActionAvailable;
    }

    public int getPosition() {
        return position;
    }

    public Player getPalyerOn() {
        return palyerOn;
    }

    public boolean isOccupied(){
       if(palyerOn != null){
           return true;
       }else{
           return false;
       }
    }

    public void setPalyerOn(Player palyerOn) {
        this.palyerOn = palyerOn;
    }
}
