package it.polimi.ingsw.am25.server.model.Game;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

/**
 * Read-only view of the {@link Game} model exposed to observers and the board.
 * Provides access to the player list, current era, and era-progression logic.
 */
public interface GameView {

    /**
     * Returns the total number of players in this game session.
     *
     * @return the player count (2–5).
     */
    int getPlayerNumber();

    /**
     * Returns the ordered list of all players participating in the game.
     *
     * @return an unmodifiable snapshot of the player list.
     */
    List<Player> getPlayerList();

    /**
     * Returns the era the game is currently in.
     *
     * @return the current {@link ERA}.
     */
    ERA getCurrentEra();

    /**
     * Advances the game to the next era. Called when the current era's deck section is exhausted.
     */
    void nextEra();
}
