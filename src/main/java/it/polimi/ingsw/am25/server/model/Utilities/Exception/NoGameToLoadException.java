package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when the server attempts to resume a saved game but no save file is found.
 */
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
