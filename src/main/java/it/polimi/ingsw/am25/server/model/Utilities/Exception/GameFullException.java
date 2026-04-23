package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class GameFullException extends RuntimeException {
    /**
     * Creates a new game full exception instance.
     * @param message parameter message.
     */
    public GameFullException(String message) {
        super(message);
    }
}
