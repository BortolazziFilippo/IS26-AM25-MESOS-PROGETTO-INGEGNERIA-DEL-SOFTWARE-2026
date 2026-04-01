package it.polimi.ingsw.am25.Model.Observers;

import it.polimi.ingsw.am25.Model.Board.DefaultTile;
import it.polimi.ingsw.am25.Model.Board.OfferTile;

import java.util.List;

public interface BoardObserver {
    void onBoardChanged(
            List<OfferTile> offerTileList,
            List<DefaultTile> defaultTileList

    );
}
