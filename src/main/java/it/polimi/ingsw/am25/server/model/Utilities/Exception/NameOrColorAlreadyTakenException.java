package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when a player tries to join with a nickname or totem color that is already in use
 * by another player in the same lobby.
 */
public class NameOrColorAlreadyTakenException extends RuntimeException {
    /**
     * Creates a new name or color already taken exception instance.
     *
     * @param message parameter message.
     */
    public NameOrColorAlreadyTakenException(String message) {
        super(message);
    }
}
