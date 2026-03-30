package it.polimi.ingsw.am25.Model.Utilities.Exception;

public class EmptyMarketException extends RuntimeException {
    public EmptyMarketException(String message) {
        super(message);
    }
    public EmptyMarketException(){};
}
