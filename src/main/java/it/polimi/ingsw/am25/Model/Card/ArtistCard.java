package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;

public class ArtistCard extends Card{
    public ArtistCard(ERA era, CARD_TYPE cardType){
        this.era=era;
        this.cardType=cardType;
    }
}
