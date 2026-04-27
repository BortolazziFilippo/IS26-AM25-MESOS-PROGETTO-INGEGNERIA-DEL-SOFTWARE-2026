package it.polimi.ingsw.am25.server.model.Enums;

/**
 * All card types available in Mesos: tribe member roles, buildings, and events.
 */
public enum CARD_TYPE {
    /** A building card that can be purchased with food and triggers special effects. */
    BUILDING,
    /** An event card that resolves at the end of a round, affecting all players. */
    EVENT,
    /** An Inventor tribe member, contributing to invention set bonuses. */
    INVENTOR,
    /** An Artist tribe member, contributing to the painting event. */
    ARTIST,
    /** A Gatherer tribe member, providing food income. */
    GATHERER,
    /** A Shaman tribe member, scoring prestige points via the shaman majority. */
    SHAMAN,
    /** A Builder tribe member, reducing building costs and scoring end-game PP. */
    BUILDER,
    /** A Hunter tribe member, triggering hunt-event bonuses. */
    HUNTER
}
