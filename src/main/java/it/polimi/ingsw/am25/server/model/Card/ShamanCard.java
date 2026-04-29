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
     * default constructor of shamanCard
     * @param era Card ERA
     * @param cardType Card type
     * @param starNumber ENUM of star a shaman has
     */
    public ShamanCard(ERA era, CARD_TYPE cardType, SHAMAN_STAR starNumber){
        this.era = era;
        this.cardType=cardType;
        this.starNumber = starNumber;
    }

    /**
     * Returns the {@link SHAMAN_STAR} enum value of this card.
     * @return the star value enum.
     */
    public SHAMAN_STAR getShamanStar() {
        return starNumber;
    }
    /**
     * Returns the numeric value of this card's star rating (1, 2, or 3).
     * @return the integer star count.
     */
    public int getStarNumber() {
        return switch (starNumber) {
            case ONE -> 1;
            case TWO -> 2;
            case THREE -> 3;
        };
    }
    /**
     * Executes add card to player.
     * @param player parameter player.
     */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * Executes equals.
     * @param o parameter o.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShamanCard that)) return false;
        return starNumber == that.starNumber && this.cardType==that.cardType && this.era==that.era;
    }
    /**
     * Executes to dto.
     * @return the result of the operation.
     */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }

}
