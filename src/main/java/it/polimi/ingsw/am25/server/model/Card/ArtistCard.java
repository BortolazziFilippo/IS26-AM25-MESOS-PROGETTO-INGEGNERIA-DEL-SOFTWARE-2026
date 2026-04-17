package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

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

    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}