package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;

/**
 * Strategy interface for building effects. Each building card binds one implementation
 * that defines what happens when the building is triggered.
 */
public interface BuildingEffectInterface {
    /**
     * Applies this building's effect to the given player.
     *
     * @param player the player who owns the building and receives the effect.
     */
    void applyEffect(Player player);
}
