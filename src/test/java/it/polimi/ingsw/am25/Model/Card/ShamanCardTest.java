package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShamanCardTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("p1", COLOR.RED);
    }

    @Test
    void testEraIsCorrect() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        assertEquals(ERA.ERA_I, card.getEra());
    }

    @Test
    void testCardTypeIsCorrect() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        assertEquals(CARD_TYPE.SHAMAN, card.getCardType());
    }

    @Test
    void testStarNumberOne() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        assertEquals(1, card.getStarNumber());
    }

    @Test
    void testStarNumberTwo() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        assertEquals(2, card.getStarNumber());
    }

    @Test
    void testStarNumberThree() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE);
        assertEquals(3, card.getStarNumber());
    }

    @Test
    void testAddCardToPlayer() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        card.addCardToPlayer(player);
        assertEquals(List.of(card), player.getTribe());
    }

    @Test
    void testShamanStarTotalSingleCard() {
        new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE).addCardToPlayer(player);
        assertEquals(3, player.getShamanStarTotal());
    }

    @Test
    void testShamanStarTotalMultipleCards() {
        new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE).addCardToPlayer(player);
        new ShamanCard(ERA.ERA_II, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO).addCardToPlayer(player);
        new ShamanCard(ERA.ERA_III, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE).addCardToPlayer(player);
        assertEquals(6, player.getShamanStarTotal());
    }
}