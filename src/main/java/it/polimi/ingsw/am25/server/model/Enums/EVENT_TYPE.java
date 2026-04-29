package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Categories of Mesos event cards. Each category defines which players are affected
 * and what prestige-point or food adjustments are applied when the event fires.
 */
public enum EVENT_TYPE {
    /** Hunt event — rewards players with Hunter cards and grants food. */
    HUNT,
    /** Paintings event — rewards players with Artist cards; penalises others. */
    PAINTINGS,
    /** Shamanic ritual — awards prestige points to the player with the most Shaman cards. */
    SHAMANIC_RIT,
    /** Sustenance event — players must pay food or lose prestige points. */
    SUSTENANCE,
    /** End-of-round event — resolved automatically at the close of each round. */
    END_ROUND,
    /** End-of-game event — resolved when the final round concludes. */
    END_GAME
}
