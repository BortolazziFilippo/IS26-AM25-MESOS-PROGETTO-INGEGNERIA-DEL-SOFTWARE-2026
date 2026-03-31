package it.polimi.ingsw.am25.Model.Player;

import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Board.OfferTile;
import it.polimi.ingsw.am25.Model.Board.Tile;

import java.util.Objects;

public class Totem {
    private final COLOR color;

    public Totem(COLOR color){
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Totem totem)) return false;
        return color == totem.color;
    }

}
