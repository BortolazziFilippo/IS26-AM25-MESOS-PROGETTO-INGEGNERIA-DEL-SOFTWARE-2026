package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;

public abstract class BuildingEffect implements BuildingEffectInterface {
    /**
     * Applies this building's effect to the given player.
     * @param player the player who owns the building.
     */
    public abstract void applyEffect(Player player);
}
