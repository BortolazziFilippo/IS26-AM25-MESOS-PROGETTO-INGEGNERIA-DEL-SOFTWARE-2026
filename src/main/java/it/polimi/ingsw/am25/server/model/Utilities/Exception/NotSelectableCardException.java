package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class NotSelectableCardException extends RuntimeException {
    /**
     * Creates a new not selectable card exception instance.
     * @param message parameter message.
     */
    public NotSelectableCardException(String message) {
        super(message);
    }
    public NotSelectableCardException(){};
}
