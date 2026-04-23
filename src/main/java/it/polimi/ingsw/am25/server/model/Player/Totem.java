package it.polimi.ingsw.am25.server.model.Player;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;

public record Totem(COLOR color) {
    /**
     * Creates a new totem instance.
     *
     * @param color parameter color.
     */
    public Totem {
    }

    /**
     * Returns color.
     *
     * @return the result of the operation.
     */
    @Override
    public COLOR color() {
        return color;
    }

    /**
     * Executes equals.
     *
     * @param o parameter o.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Totem(COLOR color1))) return false;
        return color == color1;
    }

}
