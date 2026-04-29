package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when the last round's resolve-action phase is complete, signalling that
 * the game is over and winners should be calculated.
 */
public class EndGameException extends RuntimeException {
    /**
     * Creates a new end game exception instance.
     * @param message parameter message.
     */
    public EndGameException(String message) {
        super(message);
    }
}
