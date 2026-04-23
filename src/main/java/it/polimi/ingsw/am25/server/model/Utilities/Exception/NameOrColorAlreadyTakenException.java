package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class NameOrColorAlreadyTakenException extends RuntimeException {
    /**
     * Creates a new name or color already taken exception instance.
     * @param message parameter message.
     */
    public NameOrColorAlreadyTakenException(String message) {
        super(message);
    }
}
