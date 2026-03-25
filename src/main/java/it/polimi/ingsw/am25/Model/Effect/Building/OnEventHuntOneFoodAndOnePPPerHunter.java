package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

public class OnEventHuntOneFoodAndOnePPPerHunter extends BuildingEffect{
    public OnEventHuntOneFoodAndOnePPPerHunter() {
    }

    @Override
    public void applyEffect(Player player) {
        player.managePP((int)player.getTribe().stream().filter(card -> card.getCardType()== CARD_TYPE.HUNTER).count());
        player.manageFoodAndPP(1);
    }
}
