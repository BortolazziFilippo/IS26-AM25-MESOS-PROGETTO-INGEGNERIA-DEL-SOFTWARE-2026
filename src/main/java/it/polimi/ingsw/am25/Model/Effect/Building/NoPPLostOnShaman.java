package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

public class NoPPLostOnShaman extends BuildingEffect{
    private int PPToRestore;

    public NoPPLostOnShaman(int PPToRestore) {
        this.PPToRestore = PPToRestore;
    }

    @Override
    public void applyEffect(Player player) {
        player.managePP(PPToRestore);
    }
}
