package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;

import java.util.List;

public interface BoardObserver {
    void onBoardChanged(
            List<OfferTile> offerTileList,
            List<DefaultTile> defaultTileList

    );
}
