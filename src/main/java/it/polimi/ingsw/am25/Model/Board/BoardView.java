package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public interface BoardView {
    List<Player> getOrderedPlayerOnOfferTile();
    List<Player> getOrderedPlayerOnDefaultTile();
}
