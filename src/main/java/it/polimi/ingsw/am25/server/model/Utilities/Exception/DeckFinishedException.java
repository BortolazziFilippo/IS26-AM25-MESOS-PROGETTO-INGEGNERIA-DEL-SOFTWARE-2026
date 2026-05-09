package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when the card deck runs out of cards during market refresh,
 * signalling that the game should transition to the final round.
 */
public class DeckFinishedException extends RuntimeException {
    /**
     * Creates a new deck finished exception instance.
     *
     * @param message parameter message.
     */
    public DeckFinishedException(String message) {
        super(message);
    }

    /**
     * Creates a new deck finished exception instance.
     */
    public DeckFinishedException() {

    }
}
