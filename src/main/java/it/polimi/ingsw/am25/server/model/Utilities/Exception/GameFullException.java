package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when a player tries to join a lobby that has already reached its maximum player count.
 */
public class GameFullException extends RuntimeException {
    /**
     * Creates a new game full exception instance.
     *
     * @param message parameter message.
     */
    public GameFullException(String message) {
        super(message);
    }
}
