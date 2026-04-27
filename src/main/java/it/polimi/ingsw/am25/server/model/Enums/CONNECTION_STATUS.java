package it.polimi.ingsw.am25.server.model.Enums;

/**
 * Tracks whether a player's network connection is currently active.
 */
public enum CONNECTION_STATUS {
    /** The player is reachable. */
    CONNECTED,
    /** The player's connection has been lost. */
    DISCONNECTED
}
