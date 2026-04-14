package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;
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
    void testHuntersIsCorrect() {
        assertEquals(ERA.ERA_I, hunterWithIcon.getEra());
        assertEquals(CARD_TYPE.HUNTER, hunterWithIcon.getCardType());
        assertTrue(hunterWithIcon.getHasICON());
        assertFalse(hunterWithoutIcon.getHasICON());
    }

    @Test
    void testAddCardToPlayer() {
        hunterWithIcon.addCardToPlayer(player);
        assertEquals(List.of(hunterWithIcon), player.getTribe());
    }

    @Test
    void testHunterNumberIncrements() {
        hunterWithIcon.addCardToPlayer(player);
        hunterWithoutIcon.addCardToPlayer(player);
        assertEquals(2, player.getHunterNumber());
    }

    @Test
    void testAddCardWithIconGivesFood() {
        hunterWithoutIcon.addCardToPlayer(player);
        hunterWithIcon.addCardToPlayer(player);
        assertEquals(1, player.getFood()); // 1 hunter already in the tribe when the card is added
    }

    @Test
    void testAddCardWithoutIconGivesNoFood() {
        hunterWithoutIcon.addCardToPlayer(player);
        assertEquals(0, player.getFood());
    }

    @Test
    void testEquals() {
        HuntersCard card1 = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true);

        // same fields → equal
        HuntersCard card2 = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true);
        assertEquals(card1, card2);

        // different era → not equal
        HuntersCard card3 = new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, true);
        assertNotEquals(card1, card3);

        // different hasICON → not equal
        HuntersCard card4 = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, false);
        assertNotEquals(card1, card4);

        // different type → not equal
        ArtistCard artist = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertNotEquals(card1, artist);

        // null → not equal
        assertNotEquals(null, card1);
    }
}