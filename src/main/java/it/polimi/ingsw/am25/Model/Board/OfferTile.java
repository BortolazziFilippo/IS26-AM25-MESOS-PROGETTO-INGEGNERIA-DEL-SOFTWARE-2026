package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Player.Player;

public class OfferTile extends Tile{
    private final Action actionAvailable;
    private final char offerTileID;


    public OfferTile(int drawTop, int drawBot, char offerTileID){
        this.actionAvailable = new Action(drawTop, drawBot);
        this.offerTileID = offerTileID;
        super(null);
    }

    public OfferTile(OfferTile offerTile){
        this.actionAvailable = new Action(offerTile.getActionAvailable());
        this.offerTileID = offerTile.offerTileID;
        super(null);
    }

    public char getOfferTileID() {
        return offerTileID;
    }

    public Action getActionAvailable() {
        return actionAvailable;
    }


}
