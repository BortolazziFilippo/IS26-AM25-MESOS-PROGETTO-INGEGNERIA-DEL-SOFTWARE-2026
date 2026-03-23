package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

public class ThreeMoreShamanStar extends BuildingEffect{
    public ThreeMoreShamanStar() {
    }

    @Override
    public void applyEffect(Player player) {
        player.addTemporaryShamanBonus(3);
    }
}
