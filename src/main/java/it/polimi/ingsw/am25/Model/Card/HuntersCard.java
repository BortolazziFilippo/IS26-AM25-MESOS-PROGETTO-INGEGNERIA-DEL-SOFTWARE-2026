package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.ERA;

public class HuntersCard extends Card{

    private boolean hasICON;

    public HuntersCard(ERA era, boolean hasICON){
        this.era = era;
        this.hasICON = hasICON;
    }

    public boolean getHasICON() {

        return hasICON;
    }
}
