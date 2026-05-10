package it.polimi.ingsw.am25.server.model.Utilities.Exception;

/**
 * Thrown when a player has exhausted all their available draw actions for the current turn,
 * signalling that the turn should automatically advance to the next player.
 */
public class NoMoreActionToDo extends RuntimeException {
    /**
     * Creates a new no more action to do instance.
     *
     * @param message parameter message.
     */
    public NoMoreActionToDo(String message) {
        super(message);
    }

    /**
     * Creates a new no-more-action-to-do exception with no message.
     */
    public NoMoreActionToDo() {
    }
}
