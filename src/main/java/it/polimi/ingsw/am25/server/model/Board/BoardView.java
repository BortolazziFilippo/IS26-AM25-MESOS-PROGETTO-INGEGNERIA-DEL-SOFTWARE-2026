package it.polimi.ingsw.am25.server.model.Board;

import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

/**
 * Read-only view of the Mesos board, exposing the current player positions
 * without allowing direct state modification.
 */
public interface BoardView {
    /**
     * Returns the players currently on offer tiles, in tile order.
     *
     * @return ordered list of players on offer tiles.
     */
    List<Player> getOrderedPlayerOnOfferTile();

    /**
     * Returns the players currently on default tiles, in tile order.
     *
     * @return ordered list of players on default tiles.
     */
    List<Player> getOrderedPlayerOnDefaultTile();

    /**
     * Returns a defensive copy of the offer tile the given player is currently on.
     *
     * @param player the player whose tile should be returned.
     * @return a copy of the {@link OfferTile} the player occupies.
     */
    OfferTile getCopyTilePlayerIsOn(Player player);

    /**
     * Returns {@code true} if the given player is on a default tile that grants a food bonus.
     *
     * @param player the player to check.
     * @return whether the player is on an eligible default tile.
     */
    boolean isPlayerOnAnEligibleDefaultTile(Player player);
}
