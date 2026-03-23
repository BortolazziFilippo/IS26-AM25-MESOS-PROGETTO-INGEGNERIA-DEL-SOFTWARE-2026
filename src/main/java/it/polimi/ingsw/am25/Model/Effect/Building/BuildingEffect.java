package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

public abstract class BuildingEffect implements BuildingEffectInterface {
    public abstract void applyEffect(Player player);
}
