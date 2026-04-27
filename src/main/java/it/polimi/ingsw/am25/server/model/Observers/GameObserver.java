package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

/**
 * Observer interface for game-level state changes in Mesos.
 * Implemented by {@link it.polimi.ingsw.am25.server.webLayer.ServerVirtualView} to forward
 * model events to all connected clients.
 */
public interface GameObserver {
    /**
     * Called when the game is over and the winners have been determined.
     * @param winners the list of winning players.
     */
    void gameWinners(List<Player> winners);

    /**
     * Called once at game start to push the full initial state to the observer.
     * @param currentEra the starting era.
     * @param players the list of all players.
     * @param gamePhase the starting game phase.
     * @param playerToPlace the first player to place their totem.
     * @param playerToPlay the first player to resolve actions.
     */
    void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase,
                       Player playerToPlace, Player playerToPlay);

    /**
     * Called when a new player joins the lobby.
     * @param playerAdded the player who just joined.
     */
    void onPlayerAdded(Player playerAdded);

    /**
     * Called when the game advances to a new era.
     * @param currentEra the era that just started.
     */
    void onEraChanged(ERA currentEra);

    /**
     * Called when the game phase changes (e.g. placing → resolve action).
     * @param gamePhase the new game phase.
     */
    void onGamePhaseChanged(GAME_PHASE gamePhase);

    /**
     * Called when it is a new player's turn to place their totem.
     * @param newPlayerToPlace the player who must place next.
     */
    void onPlayerToPlaceChanged(Player newPlayerToPlace);

    /**
     * Called when it is a new player's turn to resolve their offer-tile actions.
     * @param newPlayerToPlay the player whose turn it now is.
     */
    void onPlayerToPlayChanged(Player newPlayerToPlay);

    /**
     * Called when the draw-action counts for the current player's offer tile change.
     * @param drawTop the remaining top-row draw count.
     * @param drawBottom the remaining bottom-row draw count.
     */
    void actionOfferTileChanged(int drawTop, int drawBottom);
}
