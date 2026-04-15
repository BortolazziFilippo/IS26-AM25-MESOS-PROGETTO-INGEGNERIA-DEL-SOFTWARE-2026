package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class GameReadyToStartException extends RuntimeException {
    public GameReadyToStartException(String message) {
        super(message);
    }
    public GameReadyToStartException(){

    }
}
