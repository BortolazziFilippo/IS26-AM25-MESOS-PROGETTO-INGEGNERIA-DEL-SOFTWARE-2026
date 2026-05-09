package it.polimi.ingsw.am25.server.model.Factory.Event;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Effect.Event.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.EventDTO;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the set of Mesos event cards by loading event definitions from JSON
 * and binding each card to its concrete {@link it.polimi.ingsw.am25.server.model.Effect.Event.EventEffect} implementation.
 */
public class EventFactory {
    private static final String LOG_PREFIX = "[SERVER][EVENT_FACTORY]";

    /**
     * Creates a new event factory instance.
     */
    public EventFactory() {
    }

    /**
     * Loads event cards from JSON and binds each to its effect implementation.
     *
     * @return a list of all event cards ordered by ERA.
     */
    public List<EventCard> createEvent() {
        List<EventCard> templist = new ArrayList<>();
        List<EventCard> listToReturn = new ArrayList<>();
        InputStream inputStream = EventFactory.class.getResourceAsStream("/CardResources/json/event.json");
        if (inputStream == null) {
            throw new RuntimeException(getClass() + ": Errore apertura file building.json");
        }
        Reader reader = new InputStreamReader(inputStream);
        Gson gson = new Gson();
        EventDTO[] eventDTOS = gson.fromJson(reader, EventDTO[].class);
        for (EventDTO event : eventDTOS) {
            templist.add(new EventCard(event.getEra(), CARD_TYPE.EVENT, event.getEventID(), event.getEventType()));
        }
        for (EventCard temp : templist) {
            temp.setEventEffect(eventBinder(temp));
            listToReturn.add(temp);
        }
        return listToReturn;
    }

    /**
     * Executes event binder.
     *
     * @param eventCard parameter eventCard.
     * @return the result of the operation.
     */
    private EventEffect eventBinder(EventCard eventCard) {
        EventEffect eventEffect = null;
        switch (eventCard.getEventID()) {
            case 1:
                eventEffect = new HuntEvent(1, 1);
                break;
            case 2:
                eventEffect = new SustenanceEvent(1, -1);
                break;
            case 3:
                eventEffect = new ShamanEvent(5, -3);
                break;
            case 4:
                eventEffect = new ArtistEvent(1, -2, 1);
                break;
            case 5:
                eventEffect = new HuntEvent(1, 2);
                break;
            case 6:
                eventEffect = new SustenanceEvent(1, -2);
                break;
            case 7:
                eventEffect = new ShamanEvent(10, -5);
                break;
            case 8:
                eventEffect = new ArtistEvent(2, -2, 2);
                break;
            case 9:
                eventEffect = new HuntEvent(1, 3);
                break;
            case 10:
                eventEffect = new ArtistEvent(3, -2, 3);
                break;
            case 11:
                eventEffect = new SustenanceEvent(1, -3);
                break;
            case 12:
                eventEffect = new ShamanEvent(15, -7);
                break;
            default:
                logServerError("Unrecognised event ID: " + eventCard.getEventID());
        }
        return eventEffect;
    }

    /**
     * Executes log server error.
     *
     * @param message parameter message.
     */
    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }
}