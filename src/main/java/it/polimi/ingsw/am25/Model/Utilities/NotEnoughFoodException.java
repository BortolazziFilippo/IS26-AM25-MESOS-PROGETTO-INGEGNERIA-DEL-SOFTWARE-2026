package it.polimi.ingsw.am25.Model.Utilities;

public class NotEnoughFoodException extends RuntimeException{
    public NotEnoughFoodException(String message) {
        super(message);
    }
    public NotEnoughFoodException(){

    }
}
