package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Effect.Event.HuntEvent;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.NotSelectableCardException;
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
        // HuntEvent(1,2): 1 cibo a tutti, 2 PP per ogni cacciatore
        // player senza cacciatori -> 1 cibo, 0 PP
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
        assertEquals(0, player.getPrestigePoint()); // ancora 0 cacciatori
    }
}
