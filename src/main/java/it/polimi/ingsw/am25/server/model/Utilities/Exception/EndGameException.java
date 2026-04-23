package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class EndGameException extends RuntimeException {
    /**
     * Creates a new end game exception instance.
     * @param message parameter message.
     */
    public EndGameException(String message) {
        super(message);
    }
}
