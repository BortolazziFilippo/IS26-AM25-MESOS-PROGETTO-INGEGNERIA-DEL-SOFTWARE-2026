package it.polimi.ingsw.am25.server.model.Player;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;

public class Totem {
    private final COLOR color;

    /**
     * Creates a new totem instance.
     * @param color parameter color.
     */
    public Totem(COLOR color){
        this.color = color;
    }

    /**
     * Returns color.
     * @return the result of the operation.
     */
    public COLOR getColor() {
        return color;
    }

    /**
     * Executes equals.
     * @param o parameter o.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Totem totem)) return false;
        return color == totem.color;
    }

}
