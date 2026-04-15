package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class NoMoreActionToDo extends RuntimeException {
    public NoMoreActionToDo(String message) {
        super(message);
    }
    public NoMoreActionToDo(){};
}
