package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents a Hunter tribe card. Hunters grant food and prestige points during hunt events.
 * Cards with an icon grant an immediate food bonus equal to the player's current hunter count
 * when added to the tribe.
 */
public class HuntersCard extends Card{

    private final boolean hasICON;

    /**
     * @param era      the era this card belongs to.
     * @param cardType the card type (should be {@code CARD_TYPE.HUNTER}).
     * @param hasICON  whether this card carries an icon granting an immediate food bonus.
     */
    public HuntersCard(ERA era, CARD_TYPE cardType, boolean hasICON){
        this.era = era;
        this.cardType=cardType;
        this.hasICON = hasICON;
    }

    /** @return whether this card carries an icon granting an immediate food bonus. */
    public boolean getHasICON() {
        return hasICON;
    }

    /**
     * Adds this card to the player's tribe.
     * If the card has an icon, immediately grants food equal to the player's current hunter count.
     */
    @Override
    public void addCardToPlayer(Player player) {
        if(this.hasICON){
            player.manageFoodAndPP( player.getHunterNumber() );
        }
        player.addCardToTribe(this);
    }

    /**
     * @param obj the object to compare against.
     * @return true if {@code obj} is a HuntersCard with the same era, card type, and icon flag.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HuntersCard toCompare) {
            return toCompare.era == this.era && toCompare.cardType == this.cardType && toCompare.hasICON == this.hasICON;
        }else {
            return false;
        }
    }

    /** @return a CardDTO snapshot of this card for network transfer. */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}
