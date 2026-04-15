package it.polimi.ingsw.am25.server.Card;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Effect.Event.HuntEvent;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotSelectableCardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventCardTest {
    private Player player;
    private EventCard eventCard;

    @BeforeEach
    void setUp() {
        player = new Player("p1", COLOR.RED);
        eventCard = new EventCard(ERA.ERA_I, CARD_TYPE.EVENT, 1, EVENT_TYPE.HUNT, new HuntEvent(1, 2));
    }

    @Test
    void testEraIsCorrect() {
        assertEquals(ERA.ERA_I, eventCard.getEra());
    }

    @Test
    void testCardTypeIsCorrect() {
        assertEquals(CARD_TYPE.EVENT, eventCard.getCardType());
    }

    @Test
    void testEventIDIsCorrect() {
        assertEquals(1, eventCard.getEventID());
    }

    @Test
    void testEventTypeIsCorrect() {
        assertEquals(EVENT_TYPE.HUNT, eventCard.getEventType());
    }

    @Test
    void testApplyEventEffect() {
        // HuntEvent(1,2): 1 food for all players, 2 PP per hunter
        // player with no hunters → 1 food, 0 PP
        eventCard.applyEventEffect(List.of(player));
        assertEquals(0, player.getPrestigePoint());
        assertEquals(1, player.getFood());
    }

    @Test
    void testAddCardToPlayerThrowsException() {
        assertThrows(NotSelectableCardException.class, () -> eventCard.addCardToPlayer(player));
    }

    @Test
    void testSetEventEffect() {
        eventCard.setEventEffect(new HuntEvent(1, 3));
        eventCard.applyEventEffect(List.of(player));
        assertEquals(0, player.getPrestigePoint()); // still 0 hunters
    }

    @Test
    void testEquals() {
        // same eventID → equal (era and type differences don't matter)
        EventCard card2 = new EventCard(ERA.ERA_II, CARD_TYPE.EVENT, 1, EVENT_TYPE.SUSTENANCE);
        assertEquals(eventCard, card2);

        // eventID diverso → not equal
        EventCard card3 = new EventCard(ERA.ERA_I, CARD_TYPE.EVENT, 2, EVENT_TYPE.HUNT);
        assertNotEquals(eventCard, card3);

        // different type → not equal
        ArtistCard artist = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertNotEquals(eventCard, artist);

        // null → not equal
        assertNotEquals(null, eventCard);
    }
}
