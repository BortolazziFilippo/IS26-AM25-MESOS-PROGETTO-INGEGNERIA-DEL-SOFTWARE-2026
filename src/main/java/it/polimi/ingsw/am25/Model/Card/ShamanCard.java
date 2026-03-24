package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.SHAMAN_STAR;

public class ShamanCard extends Card{
    private SHAMAN_STAR starNumber;

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
     *
     * @return return int value of star
     */
    public int getStarNumber() {
        return switch (starNumber) {
            case ONE -> 1;
            case TWO -> 2;
            case THREE -> 3;
            default -> {
                System.err.println("Errore");
                yield -1;
            }
        };
    }
}
