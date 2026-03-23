package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.BuilderCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

public class BuilderDoublePP extends BuildingEffect{
    public BuilderDoublePP() {
    }

    @Override
    public void applyEffect(Player player) {
        player.getTribe().stream().filter(card -> card.getCardType()== CARD_TYPE.BUILDER).map(card -> (BuilderCard)card).forEach(card -> card.setFinalPrestigePoint(card.getFinalPrestigePoint()*2) );
    }
}
