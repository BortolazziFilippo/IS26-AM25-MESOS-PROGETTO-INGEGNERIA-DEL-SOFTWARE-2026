package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Effect.Event.EventEffect;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotSelectableCardException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.EventDTO;

import java.util.List;

public class EventCard extends Card
{
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

    public int getEventID() {
        return eventID;
    }

    public EVENT_TYPE getEventType() {
        return eventType;
    }

    /**
     * method for applying the event Effect
     * @param PlayersList list of player to apply the effect
     */
    public void applyEventEffect(List<Player> PlayersList)
    {
        this.eventEffect.solveEvent(PlayersList);
    }
    @Override
    public void addCardToPlayer(Player player) throws NotSelectableCardException {
        throw new NotSelectableCardException("Events cannot be selected");
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EventCard toCompare){
            return toCompare.eventID == this.eventID;
        }else {
            return false;
        }
    }
    @Override
    public CardDTO toDTO() {
        return new EventDTO(this);
    }
}
