package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data-transfer object for an event card, carrying the event ID and type
 * so the client can display a human-readable description of the event.
 */
public class EventDTO extends CardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final EVENT_TYPE eventType;

    /**
     * @param eventID   the unique event identifier.
     * @param era       the era this event belongs to.
     * @param eventType the category of event (hunt, sustenance, etc.).
     */
    public EventDTO(int eventID, ERA era, EVENT_TYPE eventType) {
        super(era, eventID ,CARD_TYPE.EVENT);
        this.eventType = eventType;
    }

    /**
     * Builds a DTO snapshot from an EventCard instance.
     *
     * @param eventCard the source EventCard to snapshot.
     */
    public EventDTO(EventCard eventCard) {
        super(eventCard.getEra(), CARD_TYPE.EVENT);
        this.eventID = eventCard.getEventID();
        this.eventType = eventCard.getEventType();
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
     * Returns a human-readable string shown in the TUI describing the event's parameters.
     */
    @Override
    public String toString() {
        switch (eventID) {
            case 1:
                return EVENT_TYPE.HUNT + " Cibo: +1, PP per HUNTER: +1";
            case 2:
                return EVENT_TYPE.SUSTENANCE + " Cibo da pagare: 1, PP persi: -1";
            case 3:
                return EVENT_TYPE.SHAMANIC_RIT + " PP a vincitore: +5, PP a perdente: -3";
            case 4:
                return EVENT_TYPE.PAINTINGS + " Aristi necessari: 1, PP persi: -2, PP per artista: +1";
            case 5:
                return EVENT_TYPE.HUNT + " Cibo: +1, PP per HUNTER: +2";
            case 6:
                return EVENT_TYPE.SUSTENANCE + " Cibo da pagare: 1, PP persi: -2";
            case 7:
                return EVENT_TYPE.SHAMANIC_RIT + " PP a vincitore: +10, PP a perdente: -5";
            case 8:
                return EVENT_TYPE.PAINTINGS + " Aristi necessari: 2, PP persi: -2, PP per artista: +2";
            case 9:
                return EVENT_TYPE.HUNT + " Cibo: +1, PP per HUNTER: +3";
            case 10:
                return EVENT_TYPE.PAINTINGS + " Aristi necessari: 3, PP persi: -2, PP per artista: +3";
            case 11:
                return EVENT_TYPE.SUSTENANCE + " Cibo da pagare: 1, PP persi: -3";
            case 12:
                return EVENT_TYPE.SHAMANIC_RIT + " PP a vincitore: +15, PP a perdente: -7";
            default:
                return "Error id event Card";
        }
    }
}
