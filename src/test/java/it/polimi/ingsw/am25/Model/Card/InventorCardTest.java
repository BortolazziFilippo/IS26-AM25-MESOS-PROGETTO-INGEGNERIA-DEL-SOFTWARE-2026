package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.INV_ICON;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}