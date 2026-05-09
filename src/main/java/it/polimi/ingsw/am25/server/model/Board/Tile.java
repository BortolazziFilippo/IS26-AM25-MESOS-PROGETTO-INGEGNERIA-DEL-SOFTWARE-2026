package it.polimi.ingsw.am25.server.model.Board;

import it.polimi.ingsw.am25.server.model.Player.Player;

/**
 * Abstract base class representing a board tile that can hold a single player.
 */
public abstract class Tile {
    private Player playerOn;

    /**
     * Creates a tile with the given initial occupant (may be {@code null}).
     *
     * @param playerOn the player initially on this tile, or {@code null} if empty
     */
    public Tile(Player playerOn) {
        this.playerOn = playerOn;
    }

    /**
     * Returns the player currently on this tile.
     *
     * @return the occupying player, or {@code null} if the tile is empty
     */
    public Player getPlayerOn() {
        return playerOn;
    }

    /**
     * Returns whether the tile is currently occupied.
     *
     * @return {@code true} if a player is on the tile
     */
    public boolean isOccupied() {
        return playerOn != null;
    }

    /**
     * Places a player on this tile.
     *
     * @param player the player to place
     */
    public void placePlayer(Player player) {
        this.playerOn = player;
    }

    /**
     * Removes the current occupant from this tile.
     */
    public void removePlayer() {
        this.playerOn = null;
    }

}
