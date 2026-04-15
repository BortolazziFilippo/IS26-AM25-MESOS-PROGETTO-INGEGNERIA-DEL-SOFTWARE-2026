package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;

public abstract class BuildingEffect implements BuildingEffectInterface {
    public abstract void applyEffect(Player player);
}
