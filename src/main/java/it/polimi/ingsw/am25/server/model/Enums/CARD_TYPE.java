package it.polimi.ingsw.am25.server.model.Enums;

/**
 * All card types available in Mesos: tribe member roles, buildings, and events.
 */
public enum CARD_TYPE {
    /**
     * A building card that can be purchased with food and triggers special effects.
     */
    BUILDING("Edificio"),
    /**
     * An event card that resolves at the end of a round, affecting all players.
     */
    EVENT("Evento"),
    /**
     * An Inventor tribe member, contributing to invention set bonuses.
     */
    INVENTOR("Inventore"),
    /**
     * An Artist tribe member, contributing to the painting event.
     */
    ARTIST("Artista"),
    /**
     * A Gatherer tribe member, providing food income.
     */
    GATHERER("Raccoglitore"),
    /**
     * A Shaman tribe member, scoring prestige points via the shaman majority.
     */
    SHAMAN("Sciamano"),
    /**
     * A Builder tribe member, reducing building costs and scoring end-game PP.
     */
    BUILDER("Costruttore"),
    /**
     * A Hunter tribe member, triggering hunt-event bonuses.
     */
    HUNTER("Cacciatore");

    private final String displayName;

    CARD_TYPE(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name of this card type.
     *
     * @return the display name of the card type.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
