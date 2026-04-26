package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotSelectableCardException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

public abstract class Card {
    protected ERA era;
    protected CARD_TYPE cardType;

    /**
     * Returns era.
     * @return the result of the operation.
     */
    public ERA getEra() {
        return era;
    }
    /**
     * Returns card type.
     * @return the result of the operation.
     */
    public CARD_TYPE getCardType(){
        return cardType;
    }
    /**
     * Adds this card to the given player, applying any immediate effects.
     * @param player the player who receives the card.
     * @throws NotSelectableCardException if this card type cannot be selected by a player.
     */
    public abstract void addCardToPlayer(Player player) throws NotSelectableCardException;

    /** @return a DTO representation of this card for network transfer. */
    public abstract CardDTO toDTO();
}
