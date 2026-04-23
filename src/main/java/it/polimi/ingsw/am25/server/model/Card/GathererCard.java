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
    /**
     * Executes add card to player.
     * @param player parameter player.
     */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * Executes equals.
     * @param obj parameter obj.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GathererCard toCompare){
            return toCompare.cardType == this.cardType && toCompare.era == this.era;
        }else {
            return false;
        }
    }
    /**
     * Executes to dto.
     * @return the result of the operation.
     */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }
}
