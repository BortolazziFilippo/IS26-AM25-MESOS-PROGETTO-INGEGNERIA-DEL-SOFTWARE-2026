package it.polimi.ingsw.am25.server.model.Player;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;

/**
 * Immutable value object representing a player's totem, identified by its {@link COLOR}.
 *
 * @param color the color identifying this totem.
 */
public record Totem(COLOR color) {
    /**
     * Creates a new totem instance with the specified color.
     *
     * @param color the color identifying this totem.
     */
    public Totem {
    }

    /**
     * Returns the color of this totem.
     *
     * @return the color identifying this totem.
     */
    @Override
    public COLOR color() {
        return color;
    }

    /**
     * Checks whether this totem is equal to another object.
     * Two totems are equal if they share the same color.
     *
     * @param o the object to compare against.
     * @return {@code true} if {@code o} is a {@link Totem} with the same color.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Totem(COLOR color1))) return false;
        return color == color1;
    }

}
