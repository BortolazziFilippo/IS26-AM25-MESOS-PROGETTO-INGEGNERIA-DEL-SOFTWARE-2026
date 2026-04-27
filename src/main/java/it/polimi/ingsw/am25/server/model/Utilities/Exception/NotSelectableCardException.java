package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when a player attempts to select a card type that cannot be chosen directly
 * (e.g. event cards, which fire automatically and are never added to a player's tribe).
 */
public class NotSelectableCardException extends RuntimeException {
    /**
     * Creates a new not selectable card exception instance.
     * @param message parameter message.
     */
    public NotSelectableCardException(String message) {
        super(message);
    }
    /** Creates a new not-selectable-card exception with no message. */
    public NotSelectableCardException(){}
}
