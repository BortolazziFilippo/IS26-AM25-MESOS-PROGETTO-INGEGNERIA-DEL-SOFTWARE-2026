package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;

public class GathererCard extends Card{
    /**
     * Default GathererCard constructor.
     *
     * @param era      Card ERA
     * @param cardType Card type
     */
    public GathererCard(ERA era, CARD_TYPE cardType) {
        this.cardType=cardType;
        this.era=era;
    }
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GathererCard toCompare){
            return toCompare.cardType == this.cardType && toCompare.era == this.era;
        }else {
            return false;
        }
    }
}
