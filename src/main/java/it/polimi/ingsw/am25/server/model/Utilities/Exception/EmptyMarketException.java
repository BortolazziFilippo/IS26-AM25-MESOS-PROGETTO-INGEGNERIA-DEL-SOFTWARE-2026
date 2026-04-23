package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class EmptyMarketException extends RuntimeException {
    /**
     * Creates a new empty market exception instance.
     * @param message parameter message.
     */
    public EmptyMarketException(String message) {
        super(message);
    }
    public EmptyMarketException(){};
}
