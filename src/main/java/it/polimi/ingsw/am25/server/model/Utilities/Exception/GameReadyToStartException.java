package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when the last required player joins a lobby, signalling that the game
 * is full and {@code setupAndStartGame} should be called immediately.
 */
public class GameReadyToStartException extends RuntimeException {
    /**
     * Creates a new game ready to start exception instance.
     *
     * @param message parameter message.
     */
    public GameReadyToStartException(String message) {
        super(message);
    }

    /**
     * Creates a new game ready to start exception instance.
     */
    public GameReadyToStartException() {

    }
}
