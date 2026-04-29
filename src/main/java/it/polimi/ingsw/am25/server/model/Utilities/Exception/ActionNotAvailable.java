package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when a player attempts an action that is not allowed in the current game phase
 * or is not their turn to perform (e.g. drawing a card outside the resolve-action phase,
 * placing a totem when it is another player's turn).
 */
public class ActionNotAvailable extends RuntimeException {
    /**
     * Creates a new action not available instance.
     * @param message parameter message.
     */
    public ActionNotAvailable(String message) {
        super(message);
    }
}
