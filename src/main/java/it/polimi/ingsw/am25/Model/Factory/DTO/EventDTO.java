package it.polimi.ingsw.am25.Model.Factory.DTO;

import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;

public class EventDTO {
    private int eventID;
    private ERA era;
    private EVENT_TYPE eventType;

    public int getEventID() {
        return eventID;
    }

    public ERA getEra() {
        return era;
    }

    public EVENT_TYPE getEventType() {
        return eventType;
    }

    public EventDTO(int eventID, ERA era, EVENT_TYPE eventType) {
        this.eventID = eventID;
        this.era = era;
        this.eventType = eventType;
    }
}
