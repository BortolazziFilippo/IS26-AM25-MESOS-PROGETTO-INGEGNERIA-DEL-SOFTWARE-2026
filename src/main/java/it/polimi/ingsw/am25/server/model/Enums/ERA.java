package it.polimi.ingsw.am25.server.model.Enums;

/**
 * The three eras through which a Mesos game progresses.
 * The deck and event cards are divided by era; as each era ends the market is refreshed.
 */
public enum ERA {
    /**
     * First era — early-game cards and events.
     */
    ERA_I("Era I"),
    /**
     * Second era — mid-game cards and events.
     */
    ERA_II("Era II"),
    /**
     * Third and final era — late-game cards and events.
     */
    ERA_III("Era III");

    private final String displayName;

    ERA(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
