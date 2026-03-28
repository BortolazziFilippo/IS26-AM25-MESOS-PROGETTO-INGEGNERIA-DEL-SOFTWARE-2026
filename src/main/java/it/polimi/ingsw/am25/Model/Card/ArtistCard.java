package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;

public class ArtistCard extends Card{
    /**
     * Default ArtistCard contructor
     * @param era Card ERA
     * @param cardType Card type
     */
    public ArtistCard(ERA era, CARD_TYPE cardType){
        this.era=era;
        this.cardType=cardType;
    }

    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ArtistCard toCompare){
            return toCompare.cardType == this.cardType && toCompare.era == this.getEra();
        }else{
            return false;
        }
    }
}