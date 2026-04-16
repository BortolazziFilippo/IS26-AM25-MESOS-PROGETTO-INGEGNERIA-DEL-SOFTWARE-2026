package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

public interface BoardObserver {
    void onBoardChanged(
            List<OfferTile> offerTileList,
            List<DefaultTile> defaultTileList
    );

    /**
     * all the player went back to the default tiles,
     * @param playerOrder order of the player
     */
    void playerToDefaultTile(
      List<Player> playerOrder
    );

    /**
     * the player has been moved to the {@param tilePosition} index
     * @param player player to move
     * @param tilePosition index
     */
    void playerPlacedOnOffertile(
            Player player,
            int tilePosition
    );
}
