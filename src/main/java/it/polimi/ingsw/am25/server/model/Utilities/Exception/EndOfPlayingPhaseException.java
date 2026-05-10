package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when all players have resolved their offer-tile actions, signalling
 * that the playing phase is complete and the game should transition to the next round.
 */
public class EndOfPlayingPhaseException extends RuntimeException {
    /**
     * Creates a new end of playing phase exception instance.
     *
     * @param message parameter message.
     */
    public EndOfPlayingPhaseException(String message) {
        super(message);
    }
}
