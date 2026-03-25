package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;

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
        player.addCardToTribe(this);
    }
}
