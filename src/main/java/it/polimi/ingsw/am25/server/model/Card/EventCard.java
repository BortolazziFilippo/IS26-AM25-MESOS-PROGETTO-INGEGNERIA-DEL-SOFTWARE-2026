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
public class EventCard extends Card
{
    private static final String LOG_PREFIX = "[SERVER][EVENT]";
    private final int eventID;
    private final EVENT_TYPE eventType;
    private EventEffect eventEffect;

    /**
     * Full constructor of EventCard
     * @param era Card ERA
     * @param cardType Card type
     * @param eventID ID of the event
     * @param eventType type of the event
     * @param eventEffect the event to bind
     */
    public EventCard(ERA era, CARD_TYPE cardType, int eventID, EVENT_TYPE eventType, EventEffect eventEffect) {
        this.cardType=cardType;
        this.era = era;
        this.eventID = eventID;
        this.eventType = eventType;
        this.eventEffect = eventEffect;
    }

    /**
     * Constructor without event effect
     * @param era Card ERA
     * @param cardType Card type
     * @param eventID ID of the event
     * @param eventType type of event
     */
    public EventCard(ERA era, CARD_TYPE cardType,int eventID, EVENT_TYPE eventType) {
        this.cardType=cardType;
        this.era = era;
        this.eventID = eventID;
        this.eventType = eventType;

    }

    /**
     * method for binding the event effect to the event
     * @param eventEffect effect to bind
     */
    public void setEventEffect(EventEffect eventEffect) {
        this.eventEffect = eventEffect;
    }

    /**
     * Returns event id.
     * @return the result of the operation.
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * Returns event type.
     * @return the result of the operation.
     */
    public EVENT_TYPE getEventType() {
        return eventType;
    }

    /**
     * method for applying the event Effect
     * @param PlayersList list of player to apply the effect
     */
    public void applyEventEffect(List<Player> PlayersList)
    {
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
     * Executes add card to player.
     * @param player parameter player.
     */
    @Override
    public void addCardToPlayer(Player player) throws NotSelectableCardException {
        throw new NotSelectableCardException("Events cannot be selected");
    }

    /**
     * Executes equals.
     * @param obj parameter obj.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EventCard toCompare){
            return toCompare.eventID == this.eventID;
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
        return new EventDTO(this);
    }
}
