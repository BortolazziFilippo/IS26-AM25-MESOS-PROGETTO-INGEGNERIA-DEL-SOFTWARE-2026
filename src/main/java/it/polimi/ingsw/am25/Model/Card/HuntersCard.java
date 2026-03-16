package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;

public class HuntersCard extends Card{

    private boolean hasICON;

    public HuntersCard(ERA era, CARD_TYPE cardType, boolean hasICON){
        this.era = era;
        this.cardType=cardType;
        this.hasICON = hasICON;
    }

    public boolean getHasICON() {

        return hasICON;
    }
}
