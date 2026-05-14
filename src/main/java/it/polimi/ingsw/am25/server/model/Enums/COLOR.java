package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Totem colors available to Mesos players. Each player chooses a unique color when joining the lobby.
 */
public enum COLOR {
    /**
     * Red totem.
     */
    RED("Rosso"),
    /**
     * Blue totem.
     */
    BLUE("Blu"),
    /**
     * Yellow totem.
     */
    YELLOW("Giallo"),
    /**
     * White totem.
     */
    WHITE("Bianco"),
    /**
     * Purple totem.
     */
    PURPLE("Viola");

    private final String displayName;

    COLOR(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name of this totem color.
     *
     * @return the display name of the color.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
