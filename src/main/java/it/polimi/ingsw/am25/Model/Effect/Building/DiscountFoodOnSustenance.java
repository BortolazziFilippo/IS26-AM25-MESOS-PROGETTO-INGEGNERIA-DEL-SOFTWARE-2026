package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

public class DiscountFoodOnSustenance extends BuildingEffect {
    private final CARD_TYPE cardType;

    public DiscountFoodOnSustenance(CARD_TYPE cardType) {
        this.cardType = cardType;
    }

    @Override
    public void applyEffect(Player player) {

    }
}
