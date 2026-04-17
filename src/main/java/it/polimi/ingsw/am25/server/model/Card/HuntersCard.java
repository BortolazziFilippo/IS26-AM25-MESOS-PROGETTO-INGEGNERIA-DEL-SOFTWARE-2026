package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

public class HuntersCard extends Card{

    private final boolean hasICON;

    /**
     * Default HunterCard Constructor
     * @param era Card ERA
     * @param cardType Card type
     * @param hasICON boolean parameter for signaling the card has the icon
     */
    public HuntersCard(ERA era, CARD_TYPE cardType, boolean hasICON){
        this.era = era;
        this.cardType=cardType;
        this.hasICON = hasICON;
    }


    public boolean getHasICON() {

        return hasICON;
    }
    @Override
    public void addCardToPlayer(Player player) {
        if(this.hasICON){
            player.manageFoodAndPP( player.getHunterNumber() );
        }
        player.addCardToTribe(this);

    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HuntersCard toCompare) {
            return toCompare.era == this.era && toCompare.cardType == this.cardType && toCompare.hasICON == this.hasICON;
        }else {
            return false;
        }
    }
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}
