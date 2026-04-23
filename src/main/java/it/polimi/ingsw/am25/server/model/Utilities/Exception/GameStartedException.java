package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class GameStartedException extends RuntimeException {
    /**
     * Creates a new game started exception instance.
     * @param message parameter message.
     */
    public GameStartedException(String message) {
        super(message);
    }
}
