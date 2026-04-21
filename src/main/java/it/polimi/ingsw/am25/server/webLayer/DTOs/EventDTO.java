package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Card.HuntersCard;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

import java.io.Serial;
import java.io.Serializable;

public class EventDTO extends CardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private  int eventID;
    private  EVENT_TYPE eventType;

    public EventDTO(int eventID, ERA era, EVENT_TYPE eventType) {
        super(era);
        this.eventID = eventID;
        this.eventType = eventType;
    }
    public EventDTO(EventCard eventCard) {
        super(eventCard.getEra());
        this.eventID = eventCard.getEventID();
        this.eventType = eventCard.getEventType();
    }

    public int getEventID() {
        return eventID;
    }

    public EVENT_TYPE getEventType() {
        return eventType;
    }
}
