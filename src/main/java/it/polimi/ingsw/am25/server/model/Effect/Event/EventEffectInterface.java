package it.polimi.ingsw.am25.server.model.Effect.Event;

import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

/**
 * Strategy interface for event effects. Each event card binds one implementation
 * that defines how the event affects all players when it is resolved.
 */
public interface EventEffectInterface {
    /**
     * Applies this event's effect to all players.
     *
     * @param playersList the list of all players in the game.
     */
    void solveEvent(List<Player> playersList);
}
