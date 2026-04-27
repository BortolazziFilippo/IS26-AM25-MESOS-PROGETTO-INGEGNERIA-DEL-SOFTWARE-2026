package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Represents the star value on a Shaman card.
 * The total star count across a player's Shaman cards determines the winner of shamanic-ritual events.
 */
public enum SHAMAN_STAR {
    /** Single star — contributes 1 to the shaman total. */
    ONE,
    /** Double star — contributes 2 to the shaman total. */
    TWO,
    /** Triple star — contributes 3 to the shaman total. */
    THREE
}
