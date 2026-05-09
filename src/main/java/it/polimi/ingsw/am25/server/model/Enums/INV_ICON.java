package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Inventory icons that can appear on Inventor cards.
 * Having one of each distinct icon in a player's tribe grants a prestige-point bonus at end of game.
 */
public enum INV_ICON {
    /**
     * Bread icon.
     */
    BREAD("Pane"),
    /**
     * Stone icon.
     */
    STONE("Pietra"),
    /**
     * Necklace icon.
     */
    NECKLACE("Collana"),
    /**
     * Bait icon.
     */
    BAIT("Esca"),
    /**
     * Ghost icon.
     */
    GHOST("Fantasma"),
    /**
     * Arrow icon.
     */
    ARROW("Freccia"),
    /**
     * Leather icon.
     */
    LEATHER("Cuoio"),
    /**
     * Rope icon.
     */
    ROPE("Corda"),
    /**
     * Flute icon.
     */
    FLUTE("Flauto"),
    /**
     * Bowl icon.
     */
    BOWL("Ciotola");

    private final String displayName;

    INV_ICON(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
