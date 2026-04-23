package it.polimi.ingsw.am25.server.model.Utilities.Exception;

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
