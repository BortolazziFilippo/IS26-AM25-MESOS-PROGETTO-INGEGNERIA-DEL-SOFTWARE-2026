package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class ActionNotAvailable extends RuntimeException {
    /**
     * Creates a new action not available instance.
     * @param message parameter message.
     */
    public ActionNotAvailable(String message) {
        super(message);
    }
}
