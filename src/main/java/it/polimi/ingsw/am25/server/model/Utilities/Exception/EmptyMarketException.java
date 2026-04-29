package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when a player attempts to draw from a market row that contains no selectable cards
 * (the row is either empty or contains only event cards).
 */
public class EmptyMarketException extends RuntimeException {
    /**
     * Creates a new empty market exception instance.
     * @param message parameter message.
     */
    public EmptyMarketException(String message) {
        super(message);
    }
    /** Creates a new empty market exception with no message. */
    public EmptyMarketException(){}
}
