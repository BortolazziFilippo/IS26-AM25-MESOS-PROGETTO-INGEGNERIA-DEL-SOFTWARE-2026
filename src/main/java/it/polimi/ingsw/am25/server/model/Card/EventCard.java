package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Effect.Event.EventEffect;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotSelectableCardException;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.EventDTO;

import java.util.List;

/**
 * Represents an event card in the Mesos deck. Unlike tribe-member cards, event cards
 * cannot be selected by players — they fire automatically when they come off the market
 * and apply their {@link EventEffect} to all players.
 */
public class EventCard extends Card {
    private static final String LOG_PREFIX = "[SERVER][EVENT]";
    private final int eventID;
    private final EVENT_TYPE eventType;
    private EventEffect eventEffect;

    /**
     * @param era         the era this card belongs to.
     * @param cardType    the card type (should be {@code CARD_TYPE.EVENT}).
     * @param eventID     the unique event identifier.
     * @param eventType   the category of event (hunt, sustenance, etc.).
     * @param eventEffect the effect to execute when this event fires.
     */
    public EventCard(ERA era, CARD_TYPE cardType, int eventID, EVENT_TYPE eventType, EventEffect eventEffect) {
        this.cardType = cardType;
        this.era = era;
        this.eventID = eventID;
        this.eventType = eventType;
        this.eventEffect = eventEffect;
    }

    /**
     * Constructor without a bound effect; call {@link #setEventEffect} before triggering.
     *
     * @param era       the era this card belongs to.
     * @param cardType  the card type (should be {@code CARD_TYPE.EVENT}).
     * @param eventID   the unique event identifier.
     * @param eventType the category of event (hunt, sustenance, etc.).
     */
    public EventCard(ERA era, CARD_TYPE cardType, int eventID, EVENT_TYPE eventType) {
        this.cardType = cardType;
        this.era = era;
        this.eventID = eventID;
        this.eventType = eventType;
    }

    /**
     * Binds the effect strategy to this event card.
     *
     * @param eventEffect the effect to execute when this event fires.
     */
    public void setEventEffect(EventEffect eventEffect) {
        this.eventEffect = eventEffect;
    }

    /**
     * @return the unique event identifier.
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * @return the category of event (hunt, sustenance, shamanic ritual, paintings).
     */
    public EVENT_TYPE getEventType() {
        return eventType;
    }

    /**
     * Fires the bound event effect on all players.
     *
     * @param PlayersList the list of players participating in the event.
     */
    public void applyEventEffect(List<Player> PlayersList) {
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "Executing event #" + eventID + " (" + eventType + ") for " + PlayersList.size() + " players"
        );
        this.eventEffect.solveEvent(PlayersList);
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "Completed event #" + eventID + " (" + eventType + ")"
        );
    }

    /**
     * @throws NotSelectableCardException always — event cards cannot be chosen by players.
     */
    @Override
    public void addCardToPlayer(Player player) throws NotSelectableCardException {
        throw new NotSelectableCardException("Events cannot be selected");
    }

    /**
     * @param obj the object to compare against.
     * @return true if {@code obj} is an EventCard with the same event ID.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventCard toCompare) {
            return toCompare.eventID == this.eventID;
        } else {
            return false;
        }
    }

    /**
     * @return an EventDTO snapshot of this card for network transfer.
     */
    @Override
    public CardDTO toDTO() {
        return new EventDTO(this);
    }

    @Override
    public boolean matchesDTO(CardDTO dto) {
        return dto.getCardType() == CARD_TYPE.EVENT && dto.getEventID() == this.eventID;
    }
}
