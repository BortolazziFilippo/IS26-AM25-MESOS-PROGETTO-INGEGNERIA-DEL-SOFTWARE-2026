package it.polimi.ingsw.am25.server.model.Factory.DTO;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

public class EventDTO {
    private  int eventID;
    private  ERA era;
    private  EVENT_TYPE eventType;

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
