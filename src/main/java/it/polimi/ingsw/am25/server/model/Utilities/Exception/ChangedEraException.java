package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown internally by the market when the last card drawn from the deck belongs to a new era,
 * signalling that the game should advance to the next era and refresh the building rows.
 */
public class ChangedEraException extends RuntimeException {
    /**
     * Creates a new changed era exception instance.
     * @param message parameter message.
     */
    public ChangedEraException(String message) {
        super(message);
    }
    /**
     * Creates a new changed era exception instance.
     */
    public ChangedEraException(){

    }
}
