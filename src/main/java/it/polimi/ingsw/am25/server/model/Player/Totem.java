package it.polimi.ingsw.am25.server.model.Player;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;

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
