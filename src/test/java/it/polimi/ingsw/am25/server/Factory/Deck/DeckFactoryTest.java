package it.polimi.ingsw.am25.server.Factory.Deck;

import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Factory.Deck.DeckFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckFactoryTest {
    @Test
    void createDeck_variousPlayerCounts_returnsCorrectDistribution() {
        DeckFactory deckFactory = new DeckFactory();
        List<Card> cardList;

        //CASE TWO PLAYER
        cardList = deckFactory.createDeck(2);
        assertEquals(63, cardList.size());
        //GATHERE
        assertEquals(4, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.GATHERER).count());
        //ARTIST
        assertEquals(9, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.ARTIST).count());
        //HUNTER
        assertEquals(9, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.HUNTER).count());
        //SHAMAN
        assertEquals(7, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.SHAMAN).count());
        //INVENTOR
        assertEquals(13, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.INVENTOR).count());
        //BUILDER
        assertEquals(9, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.BUILDER).count());


        //CASE THREE PLAYER
        cardList = deckFactory.createDeck(3);
        assertEquals(74,  cardList.size());
        //GATHERE
        assertEquals(6, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.GATHERER).count());
        //ARTIST
        assertEquals(11, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.ARTIST).count());
        //HUNTER
        assertEquals(12, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.HUNTER).count());
        //SHAMAN
        assertEquals(8, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.SHAMAN).count());
        //INVENTOR
        assertEquals(15, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.INVENTOR).count());
        //BUILDER
        assertEquals(10, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.BUILDER).count());

        //CASE FOUR PLAYER
        cardList = deckFactory.createDeck(4);
        assertEquals(85, cardList.size());
        //GATHERE
        assertEquals(8, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.GATHERER).count());
        //ARTIST
        assertEquals(12, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.ARTIST).count());
        //HUNTER
        assertEquals(13, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.HUNTER).count());
        //SHAMAN
        assertEquals(10, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.SHAMAN).count());
        //INVENTOR
        assertEquals(20, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.INVENTOR).count());
        //BUILDER
        assertEquals(10, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.BUILDER).count());

        //CASE FIVE PLAYER
        cardList = deckFactory.createDeck(5);
        assertEquals(96,  cardList.size());
        //GATHERE
        assertEquals(11, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.GATHERER).count());
        //ARTIST
        assertEquals(13, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.ARTIST).count());
        //HUNTER
        assertEquals(6, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.HUNTER).filter(Card->Card.getEra()==ERA.ERA_II).count());
        //SHAMAN
        assertEquals(13, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.SHAMAN).count());
        //INVENTOR
        assertEquals(20, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.INVENTOR).count());
        //BUILDER
        assertEquals(12, cardList.stream().filter(Card -> Card.getCardType() == CARD_TYPE.BUILDER).count());
    }


}