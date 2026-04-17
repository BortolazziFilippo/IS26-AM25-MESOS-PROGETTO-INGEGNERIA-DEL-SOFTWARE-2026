package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

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
     * @return the SHAMAN_STAR enum value of this card
     */
    public SHAMAN_STAR getShamanStar() {
        return starNumber;
    }
    /**
     *
     * @return return int value of star
     */
    public int getStarNumber() {
        return switch (starNumber) {
            case ONE -> 1;
            case TWO -> 2;
            case THREE -> 3;
        };
    }
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShamanCard that)) return false;
        return starNumber == that.starNumber && this.cardType==that.cardType && this.era==that.era;
    }
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }

}
