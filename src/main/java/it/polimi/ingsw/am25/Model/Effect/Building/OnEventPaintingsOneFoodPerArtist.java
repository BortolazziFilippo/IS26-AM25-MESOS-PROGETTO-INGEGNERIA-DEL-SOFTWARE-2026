package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

public class OnEventPaintingsOneFoodPerArtist extends BuildingEffect{
    public OnEventPaintingsOneFoodPerArtist() {
    }

    @Override
    public void applyEffect(Player player) {
        player.manageFoodAndPP( (int) player.getTribe().stream().filter(card -> card.getCardType()== CARD_TYPE.ARTIST).count() );
    }
}
