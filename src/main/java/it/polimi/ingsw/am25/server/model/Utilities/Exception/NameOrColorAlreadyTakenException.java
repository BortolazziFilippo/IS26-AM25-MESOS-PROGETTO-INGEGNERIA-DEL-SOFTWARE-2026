package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class NameOrColorAlreadyTakenException extends RuntimeException {
    public NameOrColorAlreadyTakenException(String message) {
        super(message);
    }
}
