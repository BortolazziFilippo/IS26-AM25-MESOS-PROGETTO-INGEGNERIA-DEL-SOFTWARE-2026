package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}