package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Tracks the current phase of a Mesos game session.
 * The game progresses linearly through setup, placing, resolve-action, and (optionally) a last round.
 */
public enum GAME_PHASE {
    /**
     * Initial lobby state — waiting for all players to join.
     */
    SETUP("Preparazione"),
    /**
     * Players are taking turns placing their totems on offer tiles.
     */
    PLACING_PHASE("Fase di posizionamento"),
    /**
     * Players are taking turns resolving their offer-tile actions (drawing cards).
     */
    RESOLVE_ACTION("Risoluzione azione"),
    /**
     * Same as {@link #PLACING_PHASE} but the deck is exhausted — this is the final round.
     */
    LAST_ROUND_PLACING_PHASE("Ultimo round - Posizionamento"),
    /**
     * Same as {@link #RESOLVE_ACTION} but for the final round; ends the game.
     */
    LAST_ROUND_RESOLVE_ACTION("Ultimo round - Risoluzione"),
    /**
     * End-of-round events are being resolved before the next placing phase.
     */
    SOLVING_EVENTS("Risoluzione eventi"),
    /**
     * The game is over and winners are being calculated.
     */
    END_GAME("Fine partita");

    private final String displayName;

    GAME_PHASE(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name of the current game phase.
     *
     * @return the display name of the game phase.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
