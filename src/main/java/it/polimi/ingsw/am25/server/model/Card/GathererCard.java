package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents a Gatherer tribe card. Gatherers do not grant food directly but reduce
 * the food penalty during sustenance events (3 food discount per Gatherer).
 */
public class GathererCard extends Card {

    /**
     * @param era      the era this card belongs to.
     * @param cardType the card type (should be {@code CARD_TYPE.GATHERER}).
     */
    public GathererCard(ERA era, CARD_TYPE cardType) {
        this.cardType = cardType;
        this.era = era;
    }

    /**
     * Adds this card to the player's tribe.
     */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * @param obj the object to compare against.
     * @return true if {@code obj} is a GathererCard with the same era and card type.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GathererCard toCompare) {
            return toCompare.cardType == this.cardType && toCompare.era == this.era;
        } else {
            return false;
        }
    }

    /**
     * @return a CardDTO snapshot of this card for network transfer.
     */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}
