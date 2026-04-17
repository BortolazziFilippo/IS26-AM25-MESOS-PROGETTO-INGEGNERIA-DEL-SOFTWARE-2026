package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Player.Player;

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
            Player playerToPlay
    );

    void onPlayerAdded(
       Player playerAdded
    );

    void onEraChanged(
            ERA currentEra
    );

    void onGamePhaseChanged(
            GAME_PHASE gamePhase
    );

    void onPlayerToPlaceChanged(
            Player newPlayerToPlace
    );
    void onPlayerToPlayChanged(
            Player newPlayerToPlay
    );
}
