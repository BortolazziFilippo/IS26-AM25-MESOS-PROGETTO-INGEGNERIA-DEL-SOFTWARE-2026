package it.polimi.ingsw.am25.server.Factory.Event;

import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Factory.Event.EventFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventFactoryTest {
    @Test
    void numberOfCardShouldBeRight(){
        EventFactory eventFactory = new EventFactory();
        List<EventCard> eventList= eventFactory.createEvent();
        assertEquals(12,  eventList.size());
        assertEquals(3,eventList.stream().filter(EventCard->EventCard.getEventType()== EVENT_TYPE.HUNT).count());
        assertEquals(3,eventList.stream().filter(EventCard->EventCard.getEventType()== EVENT_TYPE.PAINTINGS).count());
        assertEquals(3,eventList.stream().filter(EventCard->EventCard.getEventType()== EVENT_TYPE.SUSTENANCE).count());
        assertEquals(3,eventList.stream().filter(EventCard->EventCard.getEventType()== EVENT_TYPE.SHAMANIC_RIT).count());

    }

}