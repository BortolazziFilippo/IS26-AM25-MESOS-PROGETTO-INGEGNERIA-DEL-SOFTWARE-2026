package it.polimi.ingsw.am25.Model.Player;

import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Board.OfferTile;
import it.polimi.ingsw.am25.Model.Board.Tile;

import java.util.Objects;

public class Totem {
    private final COLOR color;
    private OfferTile tile;

    public Totem(COLOR color){
        this.color = color;
    }

    public Tile getTile(){
        return tile;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Totem totem)) return false;
        return color == totem.color;
    }

}
