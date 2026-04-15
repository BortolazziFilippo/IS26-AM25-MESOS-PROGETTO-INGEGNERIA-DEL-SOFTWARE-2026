package it.polimi.ingsw.am25.server.model.Board;

import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

public interface BoardView {
    List<Player> getOrderedPlayerOnOfferTile();
    List<Player> getOrderedPlayerOnDefaultTile();
    OfferTile getCopyTilePlayerIsOn(Player player);
    boolean isPlayerOnAnEligibleDefaultTile(Player player);
}
