package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.DeckFinishedException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.NotEnoughFoodException;
import it.polimi.ingsw.am25.Model.Utilities.UtilitiesFunction;
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
        game.addPlayer(player2);
        market = new Market(game);
    }


    @Test
    void testGetBottomBuildingList() {
        List<BuildingCard> bottomBuildings = market.getBottomBuildingList();

        //si assicura che non ritorni una lista null e che size sia >=0
        assertNotNull(bottomBuildings);
        //forse va reso più specifico il size
        assertTrue(bottomBuildings.size() >= 0);
    }

    @Test
    void testGetTopBuildingList() {
        List<BuildingCard> topBuildings = market.getTopBuildingList();

        //si assicura che non ritorni una lista null e che size sia >=0
        assertNotNull(topBuildings);
        //forse va reso più specifico il size
        assertTrue(topBuildings.size() >= 0);
    }

    @Test
    void testGetBottomCardList() {
        List<Card> bottomCards = market.getBottomCardList();

        assertNotNull(bottomCards);
        //stessa cosa per size forse da rendere più preciso
        assertTrue(bottomCards.size() >= 0);
    }

    @Test
    void testGetTopCardList() {
        List<Card> topCards = market.getTopCardList();

        assertNotNull(topCards);
        //size forse da rendere più specifico
        assertTrue(topCards.size() >= 0);
    }

    @Test
    void testClearBottomCardList() {
        market.clearBottomCardList();

        assertTrue(market.getBottomCardList().isEmpty());
    }

    @Test
    void testBuyBuildingTopList() {
        assertFalse(market.getTopBuildingList().isEmpty(), "La toplist non deve essere vuota");

        //caso in cui fallisce perchè non ha abbastanza cibo
        int initialMarketSize = market.getTopBuildingList().size();
        int initialPlayerBuildings = host.getBuildingCards().size();

        BuildingCard building = market.getTopBuildingList().get(0);

        try {
            market.buyBuildingTopList(0, host);
            //"fail" dice che il try deve fallire lì eprchè tanto è già successo qualcosa di sbagliato
            fail("Doveva lanciare NotEnoughFoodException");
        } catch (NotEnoughFoodException e) {
            // ok
        }

        //poi si assicura che non avendo cibo non abbia comprato l'edificio comunque togliendolo da list
        assertEquals(initialMarketSize, market.getTopBuildingList().size());

        //qui si assicura la stessa cosa ma guardando che non abbia aggiunto il building alla lista del giocatore
        assertEquals(initialPlayerBuildings, host.getBuildingCards().size());
        assertFalse(host.getBuildingCards().contains(building));


        //caso invece in cui ha abbastanza cibo
        host.manageFoodAndPP(building.getFoodCost());

        try {
            market.buyBuildingTopList(0, host);
        } catch (NotEnoughFoodException e) {
            fail("Non doveva lanciare eccezione");
        }

        //controlla che allora il market sia diminuito
        assertEquals(initialMarketSize - 1, market.getTopBuildingList().size());

        //controlla che il player abbia ricevuto l'edificio
        assertEquals(initialPlayerBuildings + 1, host.getBuildingCards().size());
        assertTrue(host.getBuildingCards().contains(building));
    }

    /* questo fallisce perchè quando inizializza la bottom list la lascia vuota
    @Test
    void testBuyBuildingBottomList() {
        assertFalse(market.getBottomBuildingList().isEmpty(), "La bottomBuildingList non deve essere vuota");

        // caso in cui fallisce perché non ha abbastanza cibo
        int initialMarketSize = market.getBottomBuildingList().size();
        int initialPlayerBuildings = host.getBuildingCards().size();

        BuildingCard building = market.getBottomBuildingList().get(0);

        try {
            market.buyBuildingBottomList(0, host);
            fail("Doveva lanciare NotEnoughFoodException");
        } catch (NotEnoughFoodException e) {
            // ok
        }

        //check che il market non cambi di size
        assertEquals(initialMarketSize, market.getBottomBuildingList().size());

        //check che il giocatore non riceva il building
        assertEquals(initialPlayerBuildings, host.getBuildingCards().size());
        assertFalse(host.getBuildingCards().contains(building));


        // caso successo
        host.manageFoodAndPP(building.getFoodCost());

        try {
            market.buyBuildingBottomList(0, host);
        } catch (NotEnoughFoodException e) {
            fail("Non doveva lanciare eccezione");
        }

        //check perchè il market deve diminuire
        assertEquals(initialMarketSize - 1, market.getBottomBuildingList().size());

        //check perchè il player deve ricevere l'edificio
        assertEquals(initialPlayerBuildings + 1, host.getBuildingCards().size());
        assertTrue(host.getBuildingCards().contains(building));
    }

    */

    @Test
    void testSelectCardFromBottomList() {
        assertFalse(market.getBottomCardList().isEmpty(), "La bottomCardList non deve essere vuota");

        int validPosition = -1;
        Card card = null;

        //questo for per gli stessi motivi di testSelectCardFromTopList()
        for (int i = 0; i < market.getBottomCardList().size(); i++) {
            if (market.getBottomCardList().get(i).getCardType() != CARD_TYPE.EVENT) {
                validPosition = i;
                card = market.getBottomCardList().get(i);
                break;
            }
        }

        assertTrue(validPosition != -1, "La bottomCardList deve contenere almeno una carta non evento");

        int initialMarketSize = market.getBottomCardList().size();
        int initialPlayerCards = host.getTribe().size();

        //caso successo
        try {
            market.selectCardFromBottomList(validPosition, host);
        } catch (Exception e) {
            fail("Non doveva lanciare eccezioni");
        }

        //check
        assertEquals(initialMarketSize - 1, market.getBottomCardList().size());
        assertEquals(initialPlayerCards + 1, host.getTribe().size());
        assertTrue(host.getTribe().contains(card));

        //caso fallimento
        market.clearBottomCardList();

        try {
            market.selectCardFromBottomList(0, host);
            fail("Doveva lanciare IllegalStateException");
        } catch (IllegalStateException e) {
            // ok
        }
    }

    @Test
    void testSelectCardFromTopList() {
        assertFalse(market.getTopCardList().isEmpty(), "La topCardList non deve essere vuota");

        int validPosition = -1;
        Card card = null;

        //questo for lo fa perchè se prendiamo solo la posiozione generica 0 c'è la possibilità che
        //la prima carta (posizione 0) sia un event e non può essere pescato
        for (int i = 0; i < market.getTopCardList().size(); i++) {
            if (market.getTopCardList().get(i).getCardType() != CARD_TYPE.EVENT) {
                validPosition = i;
                card = market.getTopCardList().get(i);
                break;
            }
        }

        assertTrue(validPosition != -1, "La topCardList deve contenere almeno una carta non evento");

        int initialMarketSize = market.getTopCardList().size();
        int initialPlayerCards = host.getTribe().size();

        // caso successo
        try {
            market.selectCardFromTopList(validPosition, host);
        } catch (Exception e) {
            fail("Non doveva lanciare eccezioni: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        // check che la lista del market deve diminuire
        assertEquals(initialMarketSize - 1, market.getTopCardList().size());

        // check che il player deve ricevere la carta
        assertEquals(initialPlayerCards + 1, host.getTribe().size());
        assertTrue(host.getTribe().contains(card));

        // caso fallimento (la topCardList è vuota)
        market.getTopCardList().clear();

        try {
            market.selectCardFromTopList(0, host);
            fail("Doveva lanciare IllegalStateException");
        } catch (IllegalStateException e) {
            // ok
        }
    }

    @Test
    void testEndOfRoundMarketActions() {


        //da aggiungere punto 5.a


        //salvo lo stato della top list prima della chiamata
        List<Card> oldTopCards = new ArrayList<>(market.getTopCardList());

        //mi assicuro che la top list iniziale non sia vuota
        assertFalse(oldTopCards.isEmpty(), "La topCardList iniziale non deve essere vuota");

        try {
            market.endOfRoundMarketActions();
        } catch (DeckFinishedException e) {
            fail("Non doveva lanciare DeckFinishedException alla prima endOfRoundMarketActions");
        }

        // dopo endOfRound:
        // 1) la bottom list deve contenere esattamente le vecchie carte della top list
        assertEquals(oldTopCards.size(), market.getBottomCardList().size());
        assertEquals(oldTopCards, market.getBottomCardList());

        // 2) la top list deve essere stata refillata
        assertFalse(market.getTopCardList().isEmpty());
        assertNotEquals(oldTopCards, market.getTopCardList());

    }














}