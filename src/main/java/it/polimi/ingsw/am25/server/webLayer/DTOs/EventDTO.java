package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

import java.io.Serial;
import java.io.Serializable;

public class EventDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private  int eventID;
    private  ERA era;
    private  EVENT_TYPE eventType;

    public EventDTO(int eventID, ERA era, EVENT_TYPE eventType) {
        this.eventID = eventID;
        this.era = era;
        this.eventType = eventType;
    }

    public int getEventID() {
        return eventID;
    }

    public ERA getEra() {
        return era;
    }

    public EVENT_TYPE getEventType() {
        return eventType;
    }
}
