package it.polimi.ingsw.am25.server.Card;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.ShamanCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ShamanCardTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("p1", COLOR.RED);
    }

    @Test
    void getEra_shamanCard_returnsCorrectEra() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        assertEquals(ERA.ERA_I, card.getEra());
    }

    @Test
    void getCardType_shamanCard_returnsShamanType() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        assertEquals(CARD_TYPE.SHAMAN, card.getCardType());
    }

    @Test
    void getStarNumber_shamanWithOneStar_returnsOne() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        assertEquals(1, card.getStarNumber());
    }

    @Test
    void getStarNumber_shamanWithTwoStars_returnsTwo() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        assertEquals(2, card.getStarNumber());
    }

    @Test
    void getStarNumber_shamanWithThreeStars_returnsThree() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE);
        assertEquals(3, card.getStarNumber());
    }

    @Test
    void addCard_shamanCard_addsToTribe() {
        ShamanCard card = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        card.addCardToPlayer(player);
        assertEquals(List.of(card), player.getTribe());
    }

    @Test
    void getShamanStarTotal_singleCard_returnsStarValue() {
        new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE).addCardToPlayer(player);
        assertEquals(3, player.getShamanStarTotal());
    }

    @Test
    void getShamanStarTotal_multipleCards_sumsStarValues() {
        new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE).addCardToPlayer(player);
        new ShamanCard(ERA.ERA_II, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO).addCardToPlayer(player);
        new ShamanCard(ERA.ERA_III, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE).addCardToPlayer(player);
        assertEquals(6, player.getShamanStarTotal());
    }

    @Test
    void equals_sameFieldsAreEqual_differentFieldsOrTypeAreNotEqual() {
        ShamanCard card1 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);

        // same fields → equal
        ShamanCard card2 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        assertEquals(card1, card2);

        // different starNumber → not equal
        ShamanCard card3 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE);
        assertNotEquals(card1, card3);

        // different era → not equal
        ShamanCard card4 = new ShamanCard(ERA.ERA_II, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        assertNotEquals(card1, card4);

        // different type → not equal
        ArtistCard artist = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertNotEquals(card1, artist);

        // null → not equal
        assertNotEquals(null, card1);
    }
}