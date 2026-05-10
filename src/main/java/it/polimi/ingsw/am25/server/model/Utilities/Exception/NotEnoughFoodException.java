package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when a player attempts to purchase a card but does not have enough food to pay its cost.
 */
public class NotEnoughFoodException extends RuntimeException {
    /**
     * Creates a new not enough food exception instance.
     *
     * @param message parameter message.
     */
    public NotEnoughFoodException(String message) {
        super(message);
    }

    /**
     * Creates a new not enough food exception instance.
     */
    public NotEnoughFoodException() {

    }
}
