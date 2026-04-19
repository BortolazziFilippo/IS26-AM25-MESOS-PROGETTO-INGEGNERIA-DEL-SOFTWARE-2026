package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class GameFullException extends RuntimeException {
    public GameFullException(String message) {
        super(message);
    }
}
