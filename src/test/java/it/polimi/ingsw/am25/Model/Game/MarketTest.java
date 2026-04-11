package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Observers.MarketObserver;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.DeckFinishedException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EmptyMarketException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.GameReadyToStartException;
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
        assertThrows(GameReadyToStartException.class,()-> game.addPlayer(player2));
        market = new Market(game, game.getBoard());
    }

    @Test
    void testGetBottomBuildingList() {
        List<BuildingCard> bottomBuildings = market.getBottomBuildingList();

        // la lista deve esistere
        assertNotNull(bottomBuildings);

        // all'inizio della partita deve essere vuota
        assertTrue(bottomBuildings.isEmpty());
    }

    @Test
    void testGetTopBuildingList() {
        List<BuildingCard> topBuildings = market.getTopBuildingList();

        // la lista deve esistere
        assertNotNull(topBuildings);

        // all'inizio deve contenere edifici (ERA I)
        assertFalse(topBuildings.isEmpty());
    }

    @Test
    void testGetBottomCardList() {
        List<Card> bottomCards = market.getBottomCardList();

        // la lista deve esistere
        assertNotNull(bottomCards);

        // deve contenere carte iniziali
        assertFalse(bottomCards.isEmpty());
    }

    @Test
    void testGetTopCardList() {
        List<Card> topCards = market.getTopCardList();

        // la lista deve esistere
        assertNotNull(topCards);

        // deve contenere carte iniziali
        assertFalse(topCards.isEmpty());
    }

    @Test
    void testClearBottomCardList() {
        market.clearBottomCardList();

        assertTrue(market.getBottomCardList().isEmpty());
    }

    @Test
    void testEndOfRoundMarketActions() {
        //da aggiungere punto 5.a

        //salvo lo stato della top list prima della chiamata
        List<Card> oldTopCards = new ArrayList<>(market.getTopCardList());

        //salvo anche la bottom list prima della chiamata, che deve essere svuotata
        List<Card> oldBottomCards = new ArrayList<>(market.getBottomCardList());

        //mi assicuro che la top list iniziale non sia vuota
        assertFalse(oldTopCards.isEmpty(), "La topCardList iniziale non deve essere vuota");

        //mi assicuro che anche la bottom list esista nello stato iniziale
        assertNotNull(oldBottomCards);

        assertDoesNotThrow(() -> market.endOfRoundMarketActions());

        //"Non doveva lanciare DeckFinishedException alla prima endOfRoundMarketActions"

        // dopo endOfRound:
        // 1) la bottom list precedente deve essere stata svuotata e rimpiazzata con le vecchie carte della top list
        assertEquals(oldTopCards.size(), market.getBottomCardList().size());
        assertEquals(oldTopCards, market.getBottomCardList());

        // 2) la top list deve essere stata refillata
        assertFalse(market.getTopCardList().isEmpty());
        assertNotEquals(oldTopCards, market.getTopCardList());

        // 3) la nuova top list e la nuova bottom list devono essere liste diverse
        assertNotSame(oldTopCards, market.getBottomCardList());
        assertNotSame(oldTopCards, market.getTopCardList());
    }

    //questo devo ancora rivederlo
    @Test
    void testEndOfRoundMarketActionsChangedEra() {
        boolean eraChanged = false;

        for (int i = 0; i < 20; i++) {
            try {
                market.endOfRoundMarketActions();
            } catch (DeckFinishedException e) {
                break; // fine mazzo, esci
            }

            if (game.getCurrentEra() != it.polimi.ingsw.am25.Model.Enums.ERA.ERA_I) {
                eraChanged = true;
                break;
            }
        }

        assertTrue(eraChanged, "Il cambio era dovrebbe avvenire dopo alcuni round");

        // se siamo arrivati qui, clearBottomBuildingList è stato chiamato
        assertNotNull(market.getBottomBuildingList());
    }

    @Test
    void testBuyBuildingTopList() {
        assertFalse(market.getTopBuildingList().isEmpty(), "La toplist non deve essere vuota");

        //caso in cui fallisce perchè non ha abbastanza cibo
        int initialMarketSize = market.getTopBuildingList().size();
        int initialPlayerBuildings = host.getBuildingCards().size();

        BuildingCard building = market.getTopBuildingList().get(0);

        assertThrows(NotEnoughFoodException.class, () -> market.buyBuildingTopList(0, host));

        //poi si assicura che non avendo cibo non abbia comprato l'edificio comunque togliendolo da list
        assertEquals(initialMarketSize, market.getTopBuildingList().size());

        //qui si assicura la stessa cosa ma guardando che non abbia aggiunto il building alla lista del giocatore
        assertEquals(initialPlayerBuildings, host.getBuildingCards().size());
        assertFalse(host.getBuildingCards().contains(building));


        //caso invece in cui ha abbastanza cibo
        host.manageFoodAndPP(building.getFoodCost());
        assertDoesNotThrow(() -> market.buyBuildingTopList(0, host));

        //controlla che allora il market sia diminuito
        assertEquals(initialMarketSize - 1, market.getTopBuildingList().size());

        //controlla che il player abbia ricevuto l'edificio
        assertEquals(initialPlayerBuildings + 1, host.getBuildingCards().size());
        assertTrue(host.getBuildingCards().contains(building));
    }

    @Test
    void testBuyBuildingBottomList() {
        if (market.getBottomBuildingList().isEmpty()) {
            List<BuildingCard> buildings = new it.polimi.ingsw.am25.Model.Factory.Building.BuildingFactory()
                    .createBuildingDeck(game.getPlayerNumber(), game.getBoard());
            assertFalse(buildings.isEmpty(), "La lista di building creata dalla factory non deve essere vuota");
            market.getBottomBuildingList().add(buildings.get(0));
        }

        assertFalse(market.getBottomBuildingList().isEmpty(), "La bottomBuildingList non deve essere vuota");

        // caso in cui fallisce perché non ha abbastanza cibo
        int initialMarketSize = market.getBottomBuildingList().size();
        int initialPlayerBuildings = host.getBuildingCards().size();

        BuildingCard building = market.getBottomBuildingList().get(0);

        assertThrows(NotEnoughFoodException.class, () -> market.buyBuildingBottomList(0, host));

        //check che il market non cambi di size
        assertEquals(initialMarketSize, market.getBottomBuildingList().size());

        //check che il giocatore non riceva il building
        assertEquals(initialPlayerBuildings, host.getBuildingCards().size());
        assertFalse(host.getBuildingCards().contains(building));


        // caso successo
        host.manageFoodAndPP(building.getFoodCost());

        assertDoesNotThrow(() -> market.buyBuildingBottomList(0, host));

        //check perchè il market deve diminuire
        assertEquals(initialMarketSize - 1, market.getBottomBuildingList().size());

        //check perchè il player deve ricevere l'edificio
        assertEquals(initialPlayerBuildings + 1, host.getBuildingCards().size());
        assertTrue(host.getBuildingCards().contains(building));
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
        int finalPos = validPosition;
        assertDoesNotThrow(() -> market.selectCardFromTopList(finalPos, host));

        // check che la lista del market deve diminuire
        assertEquals(initialMarketSize - 1, market.getTopCardList().size());

        // check che il player deve ricevere la carta
        assertEquals(initialPlayerCards + 1, host.getTribe().size());
        assertTrue(host.getTribe().contains(card));

        // caso fallimento (la topCardList è vuota)
        market.getTopCardList().clear();
        assertThrows(EmptyMarketException.class, () -> market.selectCardFromTopList(0, host));
    }

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
        int finalPos = validPosition;
        assertDoesNotThrow(() -> market.selectCardFromBottomList(finalPos, host));
        //check
        assertEquals(initialMarketSize - 1, market.getBottomCardList().size());
        assertEquals(initialPlayerCards + 1, host.getTribe().size());
        assertTrue(host.getTribe().contains(card));

        //caso fallimento
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

    //non sicuro, lo devo ricontrollare
    @Test
    void testRemoveObserver() {
        MarketObserver observer = new MarketObserver() {
            @Override
            public void onMarketChanged(List<Card> topCards, List<Card> bottomCards,
                                        List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {
            }
        };

        assertDoesNotThrow(() -> market.addObserver(observer));
        assertDoesNotThrow(() -> market.removeObserver(observer));
    }


}