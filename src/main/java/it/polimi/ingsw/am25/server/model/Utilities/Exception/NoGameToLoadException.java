package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class NoGameToLoadException extends RuntimeException {
    /**
     * Constructs the exception indicating that no saved game exists to load.
     *
     * @param message the descriptive error message.
     */
    public NoGameToLoadException(String message) {
        super(message);
    }
}
