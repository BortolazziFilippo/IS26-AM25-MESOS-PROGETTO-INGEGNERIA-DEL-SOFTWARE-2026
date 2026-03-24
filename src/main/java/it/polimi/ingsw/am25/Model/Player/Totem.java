package it.polimi.ingsw.am25.Model.Player;

import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Tile.OfferTile;
import it.polimi.ingsw.am25.Model.Tile.Tile;

public class Totem {
    private final COLOR color;
    private OfferTile tile;

    public Totem(COLOR color){
        this.color = color;
    }

    public Tile getTile(){
        return tile;
    }
}
