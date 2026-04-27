package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when a client tries to create or join a lobby after the game has already started.
 */
public class GameStartedException extends RuntimeException {
    /**
     * Creates a new game started exception instance.
     * @param message parameter message.
     */
    public GameStartedException(String message) {
        super(message);
    }
}
