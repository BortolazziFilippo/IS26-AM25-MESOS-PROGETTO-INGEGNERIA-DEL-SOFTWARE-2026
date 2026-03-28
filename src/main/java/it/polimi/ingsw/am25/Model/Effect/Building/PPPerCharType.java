package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

public class PPPerCharType extends BuildingEffect{
    private final int PrestigePoint;
    private final CARD_TYPE cardType;

    public PPPerCharType(int prestigePoint, CARD_TYPE cardType) {
        PrestigePoint = prestigePoint;
        this.cardType = cardType;
    }

    @Override
    public void applyEffect(Player player) {
        int num_of_occurence = (int) player.getTribe().stream().filter(card -> card.getCardType() == cardType).count();
        player.managePP(num_of_occurence*PrestigePoint);
    }
}
