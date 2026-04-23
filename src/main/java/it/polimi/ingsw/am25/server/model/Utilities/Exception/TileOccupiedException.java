package it.polimi.ingsw.am25.server.model.Utilities.Exception;

public class TileOccupiedException extends RuntimeException {
    /**
     * Creates a new tile occupied exception instance.
     * @param message parameter message.
     */
    public TileOccupiedException(String message) {
        super(message);
    }
    /**
     * Creates a new tile occupied exception instance.
     */
    public TileOccupiedException(){

    }
}
