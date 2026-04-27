package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotSelectableCardException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Abstract base class for all Mesos cards (tribe members, buildings, events).
 */
public abstract class Card {
    /** Creates a new card instance. */
    public Card() {}

    /** The era in which this card was introduced. */
    protected ERA era;
    /** The type of this card (tribe member role, building, or event). */
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

    /**
     * Returns a DTO representation of this card for network transfer.
     *
     * @return a {@link CardDTO} carrying the data needed by the client.
     */
    public abstract CardDTO toDTO();
}
