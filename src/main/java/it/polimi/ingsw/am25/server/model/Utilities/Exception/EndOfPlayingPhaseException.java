package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class EndOfPlayingPhaseException extends RuntimeException {
    /**
     * Creates a new end of playing phase exception instance.
     * @param message parameter message.
     */
    public EndOfPlayingPhaseException(String message) {
        super(message);
    }
}
