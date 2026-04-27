package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when all players have placed their totems on offer tiles, signalling
 * that the placing phase is complete and the game should transition to the resolve-action phase.
 */
public class EndOfPlacingPhaseException extends RuntimeException {
    /**
     * Creates a new end of placing phase exception instance.
     * @param message parameter message.
     */
    public EndOfPlacingPhaseException(String message) {
        super(message);
    }
}
