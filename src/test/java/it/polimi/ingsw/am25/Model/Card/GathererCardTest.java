package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GathererCardTest {

    private Player player;
    private GathererCard gathererCard;

    @BeforeEach
    void setUp() {
        player = new Player("p1", COLOR.RED);
        gathererCard = new GathererCard(ERA.ERA_I, CARD_TYPE.GATHERER);
    }

    @Test
    void testEraIsCorrect() {
        assertEquals(ERA.ERA_I, gathererCard.getEra());
        assertEquals(CARD_TYPE.GATHERER, gathererCard.getCardType());
    }

    @Test
    void testAddCardToPlayer() {
        gathererCard.addCardToPlayer(player);
        assertEquals(List.of(gathererCard), player.getTribe());
        assertEquals(3, player.getGatherDiscount());
    }

    @Test
    void testMultipleGatherersDiscountSummed() {
        gathererCard.addCardToPlayer(player);
        new GathererCard(ERA.ERA_II, CARD_TYPE.GATHERER).addCardToPlayer(player);
        assertEquals(6, player.getGatherDiscount());
    }

    @Test
    void testEquals() {
        GathererCard card1 = new GathererCard(ERA.ERA_I, CARD_TYPE.GATHERER);

        // same era and type → equal
        GathererCard card2 = new GathererCard(ERA.ERA_I, CARD_TYPE.GATHERER);
        assertEquals(card1, card2);

        // different era → not equal
        GathererCard card3 = new GathererCard(ERA.ERA_II, CARD_TYPE.GATHERER);
        assertNotEquals(card1, card3);

        // different type → not equal
        ArtistCard artist = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertNotEquals(card1, artist);

        // null → not equal
        assertNotEquals(null, card1);
    }

}