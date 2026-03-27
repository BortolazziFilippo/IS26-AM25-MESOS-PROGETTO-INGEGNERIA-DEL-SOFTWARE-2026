package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;
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
    void testAddCardToPlayer() {

        artistCard.addCardToPlayer(player);

        assertEquals(1, player.getArtistNumber());
        assertEquals(ERA.ERA_I, artistCard.getEra());
        assertEquals(CARD_TYPE.ARTIST, artistCard.getCardType());
    }

    @Test
    void testAddMultipleArtists(){

        artistCard.addCardToPlayer(player);
        new ArtistCard(ERA.ERA_II, CARD_TYPE.ARTIST).addCardToPlayer(player);

        assertEquals(2, player.getArtistNumber());
    }

    @Test
    void testRibeContainsArtist(){
        artistCard.addCardToPlayer(player);
        List<Card> expectedTribe = List.of(artistCard);

        assertEquals(expectedTribe, player.getTribe());
    }
}