package it.polimi.ingsw.am25.Model.Observers;

import it.polimi.ingsw.am25.Model.Board.OfferTile;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public interface GameObserver {
    void gameWinners(
            List<Player> winners
    );
    void onGameChanged(
            ERA currentEra,
            List<Player> players,
            GAME_PHASE gamePhase,
            Player playerToPlace,
            Player playerToPlay,
            OfferTile offerTilePlayerIsOn
    );
}
