package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;
/**
 * Building effect that reduces the food cost of the sustenance event by awarding
 * one food per tribe member of the configured card type.
 */
public class DiscountFoodOnSustenance extends BuildingEffect {
    private final CARD_TYPE cardType;

    /**
     * Creates a DiscountFoodOnSustenance effect for the given card type.
     *
     * @param cardType the type of card that generates a food discount during sustenance
     */
    public DiscountFoodOnSustenance(CARD_TYPE cardType) {
        this.cardType = cardType;
    }

    /**
     * Awards one food per tribe member of the configured type.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        int cardOfTypeAmount=(int)player.getTribe().stream().filter(card -> card.getCardType()==this.cardType).count();
        player.manageFoodAndPP(+cardOfTypeAmount);
    }
}
