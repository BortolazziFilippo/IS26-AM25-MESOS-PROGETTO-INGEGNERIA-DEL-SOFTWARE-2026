package it.polimi.ingsw.am25.Model.Utilities.Exception;

public class NoMoreActionToDo extends RuntimeException {
    public NoMoreActionToDo(String message) {
        super(message);
    }
    public NoMoreActionToDo(){};
}
