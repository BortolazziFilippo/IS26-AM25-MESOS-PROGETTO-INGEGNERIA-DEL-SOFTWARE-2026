package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Categories of Mesos event cards. Each category defines which players are affected
 * and what prestige-point or food adjustments are applied when the event fires.
 */
public enum EVENT_TYPE {
    /**
     * Hunt event — rewards players with Hunter cards and grants food.
     */
    HUNT("Caccia"),
    /**
     * Paintings event — rewards players with Artist cards; penalises others.
     */
    PAINTINGS("Pitture"),
    /**
     * Shamanic ritual — awards prestige points to the player with the most Shaman cards.
     */
    SHAMANIC_RIT("Rituale sciamanico"),
    /**
     * Sustenance event — players must pay food or lose prestige points.
     */
    SUSTENANCE("Sostentamento"),
    /**
     * End-of-round event — resolved automatically at the close of each round.
     */
    END_ROUND("Fine round"),
    /**
     * End-of-game event — resolved when the final round concludes.
     */
    END_GAME("Fine partita");

    private final String displayName;

    EVENT_TYPE(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
