package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class EndOfPlacingPhaseException extends RuntimeException {
    /**
     * Creates a new end of placing phase exception instance.
     * @param message parameter message.
     */
    public EndOfPlacingPhaseException(String message) {
        super(message);
    }
}
