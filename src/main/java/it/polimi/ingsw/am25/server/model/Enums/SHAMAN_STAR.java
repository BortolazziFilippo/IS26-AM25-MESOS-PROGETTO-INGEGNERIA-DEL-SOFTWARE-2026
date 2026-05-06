package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Represents the star value on a Shaman card.
 * The total star count across a player's Shaman cards determines the winner of shamanic-ritual events.
 */
public enum SHAMAN_STAR {
    /** Single star — contributes 1 to the shaman total. */
    ONE("Una stella"),
    /** Double star — contributes 2 to the shaman total. */
    TWO("Due stelle"),
    /** Triple star — contributes 3 to the shaman total. */
    THREE("Tre stelle");

    private final String displayName;

    SHAMAN_STAR(String displayName) { this.displayName = displayName; }

    @Override
    public String toString() { return displayName; }
}
