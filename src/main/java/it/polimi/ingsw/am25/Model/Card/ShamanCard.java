package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.SHAMAN_STAR;

public class ShamanCard extends Card{
    private SHAMAN_STAR starNumber;

    public ShamanCard(ERA era, SHAMAN_STAR starNumber){
        this.era = era;
        this.starNumber = starNumber;
    }

    public int getStarNumber() {
        switch (starNumber){
            case ONE:
                return 1;
            case TWO:
                return 2;
            case THREE:
                return 3;
            default:
                System.err.println("Errore");
                return -1;
        }
    }
}
