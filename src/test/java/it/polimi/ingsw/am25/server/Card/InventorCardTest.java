package it.polimi.ingsw.am25.server.Card;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.InventorCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InventorCardTest {

    private Player player;
    private InventorCard inventorCard;

    @BeforeEach
    void setUp() {
        player = new Player("p1", COLOR.RED);
        inventorCard = new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.BREAD);
    }

    @Test
    void testInventorIsCorrect() {
        assertEquals(ERA.ERA_I, inventorCard.getEra());
        assertEquals(CARD_TYPE.INVENTOR, inventorCard.getCardType());
        assertEquals(INV_ICON.BREAD, inventorCard.getInvIcon());
    }

    @Test
    void testAddCardToPlayer() {
        inventorCard.addCardToPlayer(player);
        assertEquals(List.of(inventorCard), player.getTribe());
    }

    @Test
    void testMultipleInventorsDifferentIcons() {
        inventorCard.addCardToPlayer(player);
        new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.STONE).addCardToPlayer(player);
        assertEquals(2, player.getTribe().size());
    }

    @Test
    void testMultipleInventorsSameIcon() {
        inventorCard.addCardToPlayer(player);
        new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD).addCardToPlayer(player);
        assertEquals(2, player.getTribe().size());
    }

    @Test
    void testEquals() {
        InventorCard card1 = new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.BREAD);

        // same fields → equal
        InventorCard card2 = new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.BREAD);
        assertEquals(card1, card2);

        // different icon → not equal
        InventorCard card3 = new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.STONE);
        assertNotEquals(card1, card3);

        // different era → not equal
        InventorCard card4 = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD);
        assertNotEquals(card1, card4);

        // different type → not equal
        ArtistCard artist = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertNotEquals(card1, artist);

        // null → not equal
        assertNotEquals(null, card1);
    }
}