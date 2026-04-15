package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class EmptyMarketException extends RuntimeException {
    public EmptyMarketException(String message) {
        super(message);
    }
    public EmptyMarketException(){};
}
