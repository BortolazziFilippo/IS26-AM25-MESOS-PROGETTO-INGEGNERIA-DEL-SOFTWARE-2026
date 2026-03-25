package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Player.Player;

public class OfferTile extends Tile{
    private final Action ActionAvailable;
    private final char offerTileID;


    public OfferTile(int drawTop, int drawBot, char offerTileID){
        this.ActionAvailable = new Action(drawTop, drawBot);
        this.offerTileID = offerTileID;
        super(null);
    }

    public Action getActionAvailable() {
        return ActionAvailable;
    }


}
