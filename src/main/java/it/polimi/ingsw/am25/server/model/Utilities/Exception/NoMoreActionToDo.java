package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class NoMoreActionToDo extends RuntimeException {
    /**
     * Creates a new no more action to do instance.
     * @param message parameter message.
     */
    public NoMoreActionToDo(String message) {
        super(message);
    }
    public NoMoreActionToDo(){};
}
