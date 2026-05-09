package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents a Shaman tribe card. Shamans contribute star points that determine the winner
 * of shamanic-ritual events, granting prestige points to the player with the highest total.
 */
public class ShamanCard extends Card{
    private final SHAMAN_STAR starNumber;

    /**
     * @param era        the era this card belongs to.
     * @param cardType   the card type (should be {@code CARD_TYPE.SHAMAN}).
     * @param starNumber the star rating of this shaman.
     */
    public ShamanCard(ERA era, CARD_TYPE cardType, SHAMAN_STAR starNumber){
        this.era = era;
        this.cardType=cardType;
        this.starNumber = starNumber;
    }

    /** @return the {@link SHAMAN_STAR} enum value of this card. */
    public SHAMAN_STAR getShamanStar() {
        return starNumber;
    }

    /** @return the numeric star count of this card (1, 2, or 3). */
    public int getStarNumber() {
        return switch (starNumber) {
            case ONE -> 1;
            case TWO -> 2;
            case THREE -> 3;
        };
    }

    /** Adds this card to the player's tribe. */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * @param o the object to compare against.
     * @return true if {@code o} is a ShamanCard with the same era, card type, and star rating.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShamanCard that)) return false;
        return starNumber == that.starNumber && this.cardType==that.cardType && this.era==that.era;
    }

    /** @return a CardDTO snapshot of this card for network transfer. */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}
