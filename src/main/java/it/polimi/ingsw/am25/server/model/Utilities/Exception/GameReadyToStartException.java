package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class GameReadyToStartException extends RuntimeException {
    /**
     * Creates a new game ready to start exception instance.
     * @param message parameter message.
     */
    public GameReadyToStartException(String message) {
        super(message);
    }
    /**
     * Creates a new game ready to start exception instance.
     */
    public GameReadyToStartException(){

    }
}
