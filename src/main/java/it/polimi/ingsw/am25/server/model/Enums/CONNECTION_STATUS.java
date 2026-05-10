package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Tracks whether a player's network connection is currently active.
 */
public enum CONNECTION_STATUS {
    /**
     * The player is reachable.
     */
    CONNECTED("Connesso"),
    /**
     * The player's connection has been lost.
     */
    DISCONNECTED("Disconnesso");

    private final String displayName;

    CONNECTION_STATUS(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
