package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Tracks the current phase of a Mesos game session.
 * The game progresses linearly through setup, placing, resolve-action, and (optionally) a last round.
 */
public enum GAME_PHASE {
    /** Initial lobby state — waiting for all players to join. */
    SETUP,
    /** Players are taking turns placing their totems on offer tiles. */
    PLACING_PHASE,
    /** Players are taking turns resolving their offer-tile actions (drawing cards). */
    RESOLVE_ACTION,
    /** Same as {@link #PLACING_PHASE} but the deck is exhausted — this is the final round. */
    LAST_ROUND_PLACING_PHASE,
    /** Same as {@link #RESOLVE_ACTION} but for the final round; ends the game. */
    LAST_ROUND_RESOLVE_ACTION,
    /** End-of-round events are being resolved before the next placing phase. */
    SOLVING_EVENTS,
    /** The game is over and winners are being calculated. */
    END_GAME
}
