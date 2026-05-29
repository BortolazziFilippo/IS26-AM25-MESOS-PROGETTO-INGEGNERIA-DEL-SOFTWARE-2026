package it.polimi.ingsw.am25.server.Card;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.HuntersCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HuntersCardTest {

    private Player player;
    private HuntersCard hunterWithIcon;
    private HuntersCard hunterWithoutIcon;

    @BeforeEach
    void setUp() {
        player = new Player("p1", COLOR.RED);
        hunterWithIcon = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true);
        hunterWithoutIcon = new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false);
    }

    @Test
    void hunterCard_getters_returnConstructorValues() {
        assertEquals(ERA.ERA_I, hunterWithIcon.getEra());
        assertEquals(CARD_TYPE.HUNTER, hunterWithIcon.getCardType());
        assertTrue(hunterWithIcon.getHasICON());
        assertFalse(hunterWithoutIcon.getHasICON());
    }

    @Test
    void addCard_hunterCard_addsToTribe() {
        hunterWithIcon.addCardToPlayer(player);
        assertEquals(List.of(hunterWithIcon), player.getTribe());
    }

    @Test
    void addCard_multipleHunters_incrementsCount() {
        hunterWithIcon.addCardToPlayer(player);
        hunterWithoutIcon.addCardToPlayer(player);
        assertEquals(2, player.getHunterNumber());
    }

    @Test
    void addCard_hunterWithIcon_awardsFood() {
        hunterWithoutIcon.addCardToPlayer(player);
        hunterWithIcon.addCardToPlayer(player);
        assertEquals(1, player.getFood()); // 1 hunter already in the tribe when the card is added
    }

    @Test
    void addCard_hunterWithoutIcon_awardsNoFood() {
        hunterWithoutIcon.addCardToPlayer(player);
        assertEquals(0, player.getFood());
    }

    @Test
    void equals_sameFieldsAreEqual_differentFieldsOrTypeAreNotEqual() {
        HuntersCard card1 = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true);

        // same fields -> equal
        HuntersCard card2 = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true);
        assertEquals(card1, card2);

        // different era -> not equal
        HuntersCard card3 = new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, true);
        assertNotEquals(card1, card3);

        // different hasICON -> not equal
        HuntersCard card4 = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, false);
        assertNotEquals(card1, card4);

        // different type -> not equal
        ArtistCard artist = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertNotEquals(card1, artist);

        // null -> not equal
        assertNotEquals(null, card1);
    }
}