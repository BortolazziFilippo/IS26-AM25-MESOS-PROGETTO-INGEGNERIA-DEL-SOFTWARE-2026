package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

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
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}
