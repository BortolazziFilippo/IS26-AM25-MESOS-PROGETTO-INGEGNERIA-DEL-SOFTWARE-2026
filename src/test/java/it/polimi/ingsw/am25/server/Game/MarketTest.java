package it.polimi.ingsw.am25.server.Game;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Game.Game;
import it.polimi.ingsw.am25.server.model.Game.Market;
import it.polimi.ingsw.am25.server.model.Observers.MarketObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Factory.Building.BuildingFactory;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MarketTest {

    private Market market;
    private Player host;
    private Player player2;
    private Game game;

    @BeforeEach
    void setup() {
        host = new Player("Primo", COLOR.RED);
        player2 = new Player("Secondo", COLOR.BLUE);
        game = new Game(host, 2);
        assertThrows(GameReadyToStartException.class,()-> game.addPlayer(player2));
        market = new Market(game, game.getBoard());
    }




    //checkEventsPresence is marked as deprecated so it is not tested here, which slightly reduces test coverage





    @Test
    void testGetBottomBuildingList() {
        List<BuildingCard> bottomBuildings = market.getBottomBuildingList();

        // the list must not be null
        assertNotNull(bottomBuildings);

        // at the start of the game it should be empty
        assertTrue(bottomBuildings.isEmpty());
    }

    @Test
    void testGetTopBuildingList() {
        List<BuildingCard> topBuildings = market.getTopBuildingList();

        // the list must not be null
        assertNotNull(topBuildings);

        // at the start it should contain buildings (ERA I)
        assertFalse(topBuildings.isEmpty());
    }

    @Test
    void testGetBottomCardList() {
        List<Card> bottomCards = market.getBottomCardList();

        // the list must not be null
        assertNotNull(bottomCards);

        // it should contain the initial cards
        assertFalse(bottomCards.isEmpty());
    }

    @Test
    void testGetTopCardList() {
        List<Card> topCards = market.getTopCardList();

        // the list must not be null
        assertNotNull(topCards);

        // it should contain the initial cards
        assertFalse(topCards.isEmpty());
    }

    @Test
    void testClearBottomCardList() {

        //the bottomCardList == null exception cannot be tested
        // because the constructor always initializes the list

        market.clearBottomCardList();

        assertTrue(market.getBottomCardList().isEmpty());

    }

    @Test
    void testEndOfRoundMarketActions() {
        //TODO: add point 5.a

        //save the state of the top list before calling the method
        List<Card> oldTopCards = new ArrayList<>(market.getTopCardList());

        //also save the bottom list state before the call; it should be cleared afterwards
        List<Card> oldBottomCards = new ArrayList<>(market.getBottomCardList());

        //ensure the initial top list is not empty
        assertFalse(oldTopCards.isEmpty(), "La topCardList iniziale non deve essere vuota");

        //ensure the bottom list exists in its initial state
        assertNotNull(oldBottomCards);

        assertDoesNotThrow(() -> market.endOfRoundMarketActions());

        //"should not throw DeckFinishedException on the first endOfRoundMarketActions call"

        // after endOfRound:
        // 1) the previous bottom list should have been cleared and replaced with the old top list cards
        assertEquals(oldTopCards.size(), market.getBottomCardList().size());
        assertEquals(oldTopCards, market.getBottomCardList());

        // 2) the top list should have been refilled
        assertFalse(market.getTopCardList().isEmpty());
        assertNotEquals(oldTopCards, market.getTopCardList());

        // 3) the new top list and the new bottom list must be different list instances
        assertNotSame(oldTopCards, market.getBottomCardList());
        assertNotSame(oldTopCards, market.getTopCardList());



        // forcing the end of the deck by calling the method repeatedly; there may be a better approach
        // this is meant to trigger the catch branch inside endOfRoundMarketActions()
        boolean deckFinishedTriggered = false;

        for (int i = 0; i < 100; i++) {
            try {
                market.endOfRoundMarketActions();
            } catch (DeckFinishedException e) {
                deckFinishedTriggered = true;
                break;
            }
        }

        assertTrue(deckFinishedTriggered, "DeckFinishedException should be thrown when the deck runs out");
    }

    //this test needs to be reviewed
    @Test
    void testEndOfRoundMarketActionsChangedEra() {
        boolean eraChanged = false;

        for (int i = 0; i < 20; i++) {
            try {
                market.endOfRoundMarketActions();
            } catch (DeckFinishedException e) {
                break; // deck exhausted, stop iterating
            }

            if (game.getCurrentEra() != ERA.ERA_I) {
                eraChanged = true;
                break;
            }
        }

        assertTrue(eraChanged, "The era change should occur after a few rounds");

        // if we reached this point, clearBottomBuildingList was called
        assertNotNull(market.getBottomBuildingList());
    }

    @Test
    void testBuyBuildingTopList() {
        assertFalse(market.getTopBuildingList().isEmpty(), "The top building list must not be empty");

        //failure case: not enough food
        int initialMarketSize = market.getTopBuildingList().size();
        int initialPlayerBuildings = host.getBuildingCards().size();

        BuildingCard building = market.getTopBuildingList().get(0);

        //failure case: not enough food
        assertThrows(NotEnoughFoodException.class, () -> market.buyBuildingTopList(0, host));

        //verify that without enough food, the building was not removed from the market list
        assertEquals(initialMarketSize, market.getTopBuildingList().size());

        //also verify that the building was not added to the player's building list
        assertEquals(initialPlayerBuildings, host.getBuildingCards().size());
        assertFalse(host.getBuildingCards().contains(building));

        //invalid position: IndexOutOfBoundsException expected
        assertThrows(IndexOutOfBoundsException.class, () -> market.buyBuildingTopList(-1, host));
        assertThrows(IndexOutOfBoundsException.class, () -> market.buyBuildingTopList(999, host));


        //success case: player has enough food
        host.manageFoodAndPP(building.getFoodCost());
        assertDoesNotThrow(() -> market.buyBuildingTopList(0, host));

        //verify that the market size decreased by one
        assertEquals(initialMarketSize - 1, market.getTopBuildingList().size());

        //verify that the player received the building
        assertEquals(initialPlayerBuildings + 1, host.getBuildingCards().size());
        assertTrue(host.getBuildingCards().contains(building));
    }

    @Test
    void testBuyBuildingBottomList() {
        if (market.getBottomBuildingList().isEmpty()) {
            List<BuildingCard> buildings = new BuildingFactory()
                    .createBuildingDeck(game.getPlayerNumber(), game.getBoard());
            assertFalse(buildings.isEmpty(), "The building list created by the factory must not be empty");
            market.getBottomBuildingList().add(buildings.get(0));
        }

        assertFalse(market.getBottomBuildingList().isEmpty(), "The bottomBuildingList must not be empty");

        // failure case: not enough food
        int initialMarketSize = market.getBottomBuildingList().size();
        int initialPlayerBuildings = host.getBuildingCards().size();

        BuildingCard building = market.getBottomBuildingList().get(0);

        assertThrows(NotEnoughFoodException.class, () -> market.buyBuildingBottomList(0, host));

        //verify that the market size did not change
        assertEquals(initialMarketSize, market.getBottomBuildingList().size());

        //verify that the player did not receive the building
        assertEquals(initialPlayerBuildings, host.getBuildingCards().size());
        assertFalse(host.getBuildingCards().contains(building));

        //invalid position: IndexOutOfBoundsException expected
        assertThrows(IndexOutOfBoundsException.class, () -> market.buyBuildingBottomList(-1, host));
        assertThrows(IndexOutOfBoundsException.class, () -> market.buyBuildingBottomList(999, host));

        // success case
        host.manageFoodAndPP(building.getFoodCost());

        assertDoesNotThrow(() -> market.buyBuildingBottomList(0, host));

        //verify that the market size decreased by one
        assertEquals(initialMarketSize - 1, market.getBottomBuildingList().size());

        //verify that the player received the building
        assertEquals(initialPlayerBuildings + 1, host.getBuildingCards().size());
        assertTrue(host.getBuildingCards().contains(building));
    }

    @Test
    void testSelectCardFromTopList() {
        assertFalse(market.getTopCardList().isEmpty(), "The topCardList must not be empty");

        int validPosition = -1;
        Card card = null;

        for (int i = 0; i < market.getTopCardList().size(); i++) {
            if (market.getTopCardList().get(i).getCardType() != CARD_TYPE.EVENT) {
                validPosition = i;
                card = market.getTopCardList().get(i);
                break;
            }
        }

        assertTrue(validPosition != -1, "The topCardList must contain at least one non-event card");

        int initialMarketSize = market.getTopCardList().size();
        int initialPlayerCards = host.getTribe().size();

        //normal case
        final int valid=validPosition;
        assertDoesNotThrow(()->market.selectCardFromTopList(valid, host));


        assertEquals(initialMarketSize - 1, market.getTopCardList().size());
        assertEquals(initialPlayerCards + 1, host.getTribe().size());
        assertTrue(host.getTribe().contains(card));

        //invalid position: IndexOutOfBoundsException expected
        assertThrows(IndexOutOfBoundsException.class, () -> market.selectCardFromTopList(-1, host));
        assertThrows(IndexOutOfBoundsException.class, () -> market.selectCardFromTopList(999, host));

        //null player
        assertThrows(IllegalArgumentException.class, () -> market.selectCardFromTopList(0, null));

        //event card selection case
        int eventPosition = -1;
        for (int i = 0; i < market.getTopCardList().size(); i++) {
            if (market.getTopCardList().get(i).getCardType() == CARD_TYPE.EVENT) {
                eventPosition = i;
                break;
            }
        }

        if (eventPosition != -1) {
            int sizeBefore = market.getTopCardList().size();
            int playerCardsBefore = host.getTribe().size();
            final int event=eventPosition;
            assertThrows(NotSelectableCardException.class,()->market.selectCardFromTopList(event, host));

            assertEquals(sizeBefore, market.getTopCardList().size());
            assertEquals(playerCardsBefore, host.getTribe().size());
        }

        // empty list case
        market.getTopCardList().clear();
        assertThrows(EmptyMarketException.class, () -> market.selectCardFromTopList(0, host));
    }

    @Test
    void testSelectCardFromBottomList() {
        assertFalse(market.getBottomCardList().isEmpty(), "The bottomCardList must not be empty");

        int validPosition = -1;
        Card card = null;

        //find a non-event card
        for (int i = 0; i < market.getBottomCardList().size(); i++) {
            if (market.getBottomCardList().get(i).getCardType() != CARD_TYPE.EVENT) {
                validPosition = i;
                card = market.getBottomCardList().get(i);
                break;
            }
        }

        assertTrue(validPosition != -1, "The bottomCardList must contain at least one non-event card");

        int initialMarketSize = market.getBottomCardList().size();
        int initialPlayerCards = host.getTribe().size();
        final int valid=validPosition;
        //normal cvase
        assertDoesNotThrow(()->market.selectCardFromBottomList(valid, host));

        assertEquals(initialMarketSize - 1, market.getBottomCardList().size());
        assertEquals(initialPlayerCards + 1, host.getTribe().size());
        assertTrue(host.getTribe().contains(card));

        //invalid position: IndexOutOfBoundsException expected
        assertThrows(IndexOutOfBoundsException.class, () -> market.selectCardFromBottomList(-1, host));
        assertThrows(IndexOutOfBoundsException.class, () -> market.selectCardFromBottomList(999, host));

        //null player
        assertThrows(IllegalArgumentException.class, () -> market.selectCardFromBottomList(0, null));

        //event card case
        //this time, pick an event card
        int eventPosition = -1;
        for (int i = 0; i < market.getBottomCardList().size(); i++) {
            if (market.getBottomCardList().get(i).getCardType() == CARD_TYPE.EVENT) {
                eventPosition = i;
                break;
            }
        }

        //not using a lambda here because it caused issues
        if (eventPosition != -1) {
            int sizeBefore = market.getBottomCardList().size();
            int playerCardsBefore = host.getTribe().size();

            try {
                market.selectCardFromBottomList(eventPosition, host);
                fail("Expected NotSelectableCardException to be thrown");
            } catch (NotSelectableCardException e) {
                // ok
            } catch (Exception e) {
                fail("Expected NotSelectableCardException to be thrown");
            }

            assertEquals(sizeBefore, market.getBottomCardList().size());
            assertEquals(playerCardsBefore, host.getTribe().size());
        }

        //failure case: empty list
        market.clearBottomCardList();
        assertThrows(EmptyMarketException.class, () -> market.selectCardFromBottomList(0, host));

    }

    @Test
    void testEmptyMarketExceptions() {

        market.getTopCardList().clear();
        assertThrows(EmptyMarketException.class, () -> market.selectCardFromTopList(0, host));

        market.getBottomCardList().clear();
        assertThrows(EmptyMarketException.class, () -> market.selectCardFromBottomList(0, host));

        market.getTopBuildingList().clear();
        assertThrows(EmptyMarketException.class, () -> market.buyBuildingTopList(0, host));

        market.getBottomBuildingList().clear();
        assertThrows(EmptyMarketException.class, () -> market.buyBuildingBottomList(0, host));
    }

    //FIXME:this test needs to be reviewed since the observer has been changed
//    @Test
//    void testRemoveObserver() {
//        MarketObserver observer = new MarketObserver() {
//            @Override
//            public void onMarketChanged(List<Card> topCards, List<Card> bottomCards,
//                                        List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {
//            }
//        };
//
//        assertDoesNotThrow(() -> market.addObserver(observer));
//        assertDoesNotThrow(() -> market.removeObserver(observer));
//    }








    //commento da leggere
    //FIXME:Leggere commento e fare test
    /*QUESTO PER ORA LO LASCIO COSì PERCHè MI SA CHE C'è ERRORE IN SOLVEFINALEVENTS() DI MARKET
    PERCHè FA UN REMOVE MENTRE STA ITERANDO E RESTITUISCE ConcurrentModificationException
    MI SEMBRA CHE CI FOSSE LO STESO PROBLEMA IN UN ALTRO TEST, POI LO SISTEMO
    @Test
    void testSolveFinalEvents() {
        //siccome c'è probelma nel codice testa solo che gli esca ConcurrentModificationException
        //giusto per vedere se esce o no ma poi il test messo così è inutile
        assertThrows(ConcurrentModificationException.class, () -> market.solveFinalEvents());
    }

     */


}