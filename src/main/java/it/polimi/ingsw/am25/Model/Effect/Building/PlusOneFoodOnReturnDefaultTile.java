package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

public class PlusOneFoodOnReturnDefaultTile extends BuildingEffect{
    public PlusOneFoodOnReturnDefaultTile() {
    }

    @Override
    public void applyEffect(Player player) {

        player.manageFoodAndPP(1);
    }
}
