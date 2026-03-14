package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.SHAMAN_STAR;

public class ShamanCard extends Card{
    private SHAMAN_STAR starNumber;

    public ShamanCard(ERA era, SHAMAN_STAR starNumber){
        this.era = era;
        this.starNumber = starNumber;
    }

    public SHAMAN_STAR getStarNumber() {
        return starNumber;
    }
}
