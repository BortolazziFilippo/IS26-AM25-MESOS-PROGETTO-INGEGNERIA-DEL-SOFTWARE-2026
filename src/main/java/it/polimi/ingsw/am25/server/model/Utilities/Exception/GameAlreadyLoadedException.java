package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class GameAlreadyLoadedException extends RuntimeException {
    public GameAlreadyLoadedException(String message) {
        super(message);
    }
}
