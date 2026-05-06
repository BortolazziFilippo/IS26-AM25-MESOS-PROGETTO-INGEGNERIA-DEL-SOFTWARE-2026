package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Totem colors available to Mesos players. Each player chooses a unique color when joining the lobby.
 */
public enum COLOR {
    /** Red totem. */
    RED("Rosso"),
    /** Blue totem. */
    BLUE("Blu"),
    /** Yellow totem. */
    YELLOW("Giallo"),
    /** Green totem. */
    GREEN("Verde"),
    /** Purple totem. */
    PURPLE("Viola");

    private final String displayName;

    COLOR(String displayName) { this.displayName = displayName; }

    @Override
    public String toString() { return displayName; }
}
