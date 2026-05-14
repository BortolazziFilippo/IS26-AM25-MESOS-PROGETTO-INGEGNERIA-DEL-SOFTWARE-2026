package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class GameAlreadyLoadedException extends RuntimeException {
    /**
     * Constructs the exception indicating that a game has already been loaded.
     *
     * @param message the descriptive error message.
     */
    public GameAlreadyLoadedException(String message) {
        super(message);
    }
}
