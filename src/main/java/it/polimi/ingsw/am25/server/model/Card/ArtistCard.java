package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents an Artist tribe member card. Artists contribute to the painting event
 * and may provide food bonuses through building effects.
 */
public class ArtistCard extends Card{

    /**
     * @param era      the era this card belongs to.
     * @param cardType the card type (should be {@code CARD_TYPE.ARTIST}).
     */
    public ArtistCard(ERA era, CARD_TYPE cardType){
        this.era=era;
        this.cardType=cardType;
    }

    /** Adds this card to the player's tribe. */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * @param obj the object to compare against.
     * @return true if {@code obj} is an ArtistCard with the same era and card type.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ArtistCard toCompare){
            return toCompare.cardType == this.cardType && toCompare.era == this.getEra();
        }else{
            return false;
        }
    }

    /** @return a CardDTO snapshot of this card for network transfer. */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}
