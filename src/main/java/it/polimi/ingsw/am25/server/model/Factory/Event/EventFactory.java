package it.polimi.ingsw.am25.server.model.Factory.Event;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Effect.Event.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.EventDTO;

import java.io.InputStream;
import java.io.InputStreamReader;
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
        InputStream inputStream = EventFactory.class.getResourceAsStream("/CardResources/json/event.json");
        if (inputStream == null) {
            throw new RuntimeException(getClass() + ": Errore apertura file event.json");
        }
        EventDTO[] eventDTOS = new Gson().fromJson(new InputStreamReader(inputStream), EventDTO[].class);
        List<EventCard> listToReturn = new ArrayList<>();
        for (EventDTO event : eventDTOS) {
            EventCard card = new EventCard(event.getEra(), CARD_TYPE.EVENT, event.getEventID(), event.getEventType());
            card.setEventEffect(eventBinder(card));
            listToReturn.add(card);
        }
        return listToReturn;
    }

    /**
     * Binds the concrete {@link EventEffect} implementation to the given event card
     * based on its event ID.
     *
     * @param eventCard the event card to bind.
     * @return the matching {@link EventEffect}, or {@code null} for an unrecognised ID.
     */
    private EventEffect eventBinder(EventCard eventCard) {
        return switch (eventCard.getEventID()) {
            case 1  -> new HuntEvent(1, 1);
            case 2  -> new SustenanceEvent(1, -1);
            case 3  -> new ShamanEvent(5, -3);
            case 4  -> new ArtistEvent(1, -2, 1);
            case 5  -> new HuntEvent(1, 2);
            case 6  -> new SustenanceEvent(1, -2);
            case 7  -> new ShamanEvent(10, -5);
            case 8  -> new ArtistEvent(2, -2, 2);
            case 9  -> new HuntEvent(1, 3);
            case 10 -> new ArtistEvent(3, -2, 3);
            case 11 -> new SustenanceEvent(1, -3);
            case 12 -> new ShamanEvent(15, -7);
            default -> {
                logServerError("Unrecognised event ID: " + eventCard.getEventID());
                yield null;
            }
        };
    }

    /**
     * Creates a single EventCard with its effect bound, looked up by event ID.
     *
     * @param id the event ID to look up.
     * @return the matching EventCard with its effect bound.
     */
    public EventCard createEventById(int id) {
        return createEvent().stream()
            .filter(e -> e.getEventID() == id)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown event ID: " + id));
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