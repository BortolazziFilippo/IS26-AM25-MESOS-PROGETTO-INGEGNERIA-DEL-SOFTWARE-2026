package it.polimi.ingsw.am25.server.Card;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Card.GathererCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtistCardTest {

    private Player player;
    private ArtistCard artistCard;

    @BeforeEach
    void setup(){
        player = new Player("Mario", COLOR.RED);
        artistCard = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
    }

    @Test
    void addCard_singleArtist_incrementsArtistCount() {

        artistCard.addCardToPlayer(player);

        assertEquals(1, player.getArtistNumber());
        assertEquals(ERA.ERA_I, artistCard.getEra());
        assertEquals(CARD_TYPE.ARTIST, artistCard.getCardType());
    }

    @Test
    void addCard_multipleArtists_countIncrementsCorrectly(){

        artistCard.addCardToPlayer(player);
        new ArtistCard(ERA.ERA_II, CARD_TYPE.ARTIST).addCardToPlayer(player);

        assertEquals(2, player.getArtistNumber());
    }

    @Test
    void addCard_artistCard_appearsInTribe(){
        artistCard.addCardToPlayer(player);
        List<Card> expectedTribe = List.of(artistCard);

        assertEquals(expectedTribe, player.getTribe());
    }
    @Test
    void equals_sameFieldsAreEqual_differentFieldsOrTypeAreNotEqual(){
        // same era and type → equal
        ArtistCard card1 = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        ArtistCard card2 = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertEquals(card1, card2);

        // different era → not equal
        ArtistCard card3 = new ArtistCard(ERA.ERA_II, CARD_TYPE.ARTIST);
        assertNotEquals(card1, card3);

        // different type (GathererCard) → not equal
        GathererCard gatherer = new GathererCard(ERA.ERA_I, CARD_TYPE.GATHERER);
        assertNotEquals(card1, gatherer);

        // null → not equal
        assertNotEquals(null, card1);
    }
}