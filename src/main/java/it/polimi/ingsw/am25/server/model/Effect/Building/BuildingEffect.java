package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;

/**
 * Abstract base class for all building effects, providing a typed entry point
 * that all concrete effects must implement.
 */
public abstract class BuildingEffect implements BuildingEffectInterface {
    public BuildingEffect() {}

    /**
     * Applies this building's effect to the given player.
     * @param player the player who owns the building.
     */
    public abstract void applyEffect(Player player);
}
