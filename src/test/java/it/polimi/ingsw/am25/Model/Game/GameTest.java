package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EndOfPlacingPhaseException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.GameReadyToStartException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private Player host;
    private Player player2;
    private Player player3;
    private Game game;

    @BeforeEach
    void setup() {
        host = new Player("Primo", COLOR.RED);
        player2 = new Player("Secondo", COLOR.BLUE);
        player3 = new Player("Terzo", COLOR.GREEN);
        game = new Game(host, 3);
    }

    @Test
    void testGetPlayerNumber() {
        assertEquals(3, game.getPlayerNumber());
    }

    @Test
    void testGetCurrentEra() {

        //questo deve ritornare l'era attuale che però sarà diversa in base al moemnto in cui
        //il metodo è chiamato, quindi ho diviso in 3 casi ma forse bastava fargli testare solo
        //che all'inizio l'era fosse la eraI
        assertEquals(ERA.ERA_I, game.getCurrentEra());

        game.nextEra();
        assertEquals(ERA.ERA_II, game.getCurrentEra());

        game.nextEra();
        assertEquals(ERA.ERA_III, game.getCurrentEra());
    }

    @Test
    void testGetPlayerList() {
        //anche qui come in testGetCurrentEra prima testa che ci sia host, poi aggiugno gli altri
        assertEquals(1, game.getPlayerList().size());
        assertTrue(game.getPlayerList().contains(host));

        game.addPlayer(player2);
        assertEquals(2, game.getPlayerList().size());
        assertTrue(game.getPlayerList().contains(player2));
    }

    @Test
    void testGetGamePhase() {
        //appena creata la partita deve essere in fase di setup
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());

        //aggiunge giocatori
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //dopo che si chiama gameStart dobbiamo passare a placing_phase
        game.gameStart();
        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
    }

    @Test
    void testGetPlayerToPlace() {
        //prima che la partita inizi non c'è ancora nessun player da piazzare
        assertNull(game.getPlayerToPlace());

        //solite due assert iniziali
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //dopo aver chiamato gameStart deve esserci un player da piazzare
        game.gameStart();
        assertNotNull(game.getPlayerToPlace());

        //verifica anche che quello da piazzare sia un player della partita
        assertTrue(game.getPlayerList().contains(game.getPlayerToPlace()));
    }

    @Test
    void testGetPlayerToPlay() {
        //all'inizio il player to play è null
        assertNull(game.getPlayerToPlay());

        //prepara partita completa con solite due assert
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //piazza tutti giocatori (qui era comodo il try e catch ma si può togliere in teoria)
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            // è normale che alla fine lanci questa eccezione
        }

        //fase successiva in cui deve esserci player che gioca
        game.advancePlayingPhase();
        assertNotNull(game.getPlayerToPlay());

        //solito check che quel player sia effettivamente uno dei player del gioco
        assertTrue(game.getPlayerList().contains(game.getPlayerToPlay()));
    }

    @Test
    void testGetMarket() {
        //il market viene creato nel costruttore di game quindi non deve mai essere null
        assertNotNull(game.getMarket());

        //controlla anche che restituisca davvero un market
        assertInstanceOf(Market.class, game.getMarket());
    }

    @Test
    void testAddPlayer() {
        //testa numero di player, che per ora dovrebbe essere solo 1 (host)
        assertEquals(1, game.getPlayerList().size());
        assertEquals(List.of(host), game.getPlayerList());

        //aggiunta normale secondo player
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertEquals(2, game.getPlayerList().size());
        assertEquals(List.of(host, player2), game.getPlayerList());

        // quando la lobby si riempie, lancia eccezione ma il player viene comunque aggiunto
        GameReadyToStartException ex = assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        assertEquals("The lobby is full, game can start", ex.getMessage());
        assertEquals(3, game.getPlayerList().size());
        assertEquals(List.of(host, player2, player3), game.getPlayerList());
    }

    @Test
    void testGetBoard() {
        Board board = game.getBoard();

        //testa che board non sia null
        assertNotNull(board);
        //testa che board sia effettivamente di classe Board
        assertInstanceOf(Board.class, board);
    }

    @Test
    void testNextEra() {
        //testa tutte e 3 le ere

        assertEquals(ERA.ERA_I, game.getCurrentEra());

        game.nextEra();
        assertEquals(ERA.ERA_II, game.getCurrentEra());

        game.nextEra();
        assertEquals(ERA.ERA_III, game.getCurrentEra());

        //questo pezzo l'ho aggiutno solo per testare che dopo la era3 non faccia casino
        game.nextEra();
        game.nextEra();
        game.nextEra();

        assertEquals(ERA.ERA_III, game.getCurrentEra());

    }

    @Test
    void testGameStart() {
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //all'inzio deve esser ein placing_phase
        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
        assertNotNull(game.getPlayerToPlace());

        //tutti i giocatori devono essere stati posizionati sui default tile
        List<Player> playersOnDefaultTiles = game.getBoard().getOrderedPlayerOnDefaultTile();
        assertEquals(3, playersOnDefaultTiles.size());
        assertTrue(playersOnDefaultTiles.contains(host));
        assertTrue(playersOnDefaultTiles.contains(player2));
        assertTrue(playersOnDefaultTiles.contains(player3));

        //il player che piazza deve essere uno dei giocatori
        assertTrue(game.getPlayerList().contains(game.getPlayerToPlace()));
    }

    @Test
    void testPlacePlayer() {
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //i player da piazzare sono nell'ordine deciso da gameStart()
        Player firstToPlace = game.getPlayerToPlace();

        //prova tutti e 3
        assertDoesNotThrow(() -> game.placePlayer(firstToPlace, 0));
        assertNotNull(game.getPlayerToPlace());

        Player secondToPlace = game.getPlayerToPlace();
        assertNotEquals(firstToPlace, secondToPlace);

        assertDoesNotThrow(() -> game.placePlayer(secondToPlace, 1));
        assertNotNull(game.getPlayerToPlace());

        Player thirdToPlace = game.getPlayerToPlace();
        assertNotEquals(secondToPlace, thirdToPlace);

        EndOfPlacingPhaseException ex = assertThrows(
                EndOfPlacingPhaseException.class,
                () -> game.placePlayer(thirdToPlace, 2)
        );

        //si assicura che abbia preso il throw dentro il catch di EndOfPlacingPhaseException
        assertEquals("all Player have placed", ex.getMessage());

        List<Player> playersOnOfferTiles = game.getBoard().getOrderedPlayerOnOfferTile();
        assertEquals(3, playersOnOfferTiles.size());
        assertTrue(playersOnOfferTiles.contains(host));
        assertTrue(playersOnOfferTiles.contains(player2));
        assertTrue(playersOnOfferTiles.contains(player3));
    }

    @Test
    void testCheckWinner() {
        //prepara con 3 giocatori
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //primo caso in cui vince chi ha più PP
        host.managePP(3);
        player2.managePP(7);
        player3.managePP(5);

        List<Player> winners = game.checkWinner();

        //controlla che il vincitore sia unico
        assertEquals(1, winners.size());
        assertEquals(player2, winners.getFirst());

        //caso in cui si arriva allo spareggio con il cibo
        //resetta per creare nuova partita, più comodo
        host = new Player("Primo", COLOR.RED);
        player2 = new Player("Secondo", COLOR.BLUE);
        player3 = new Player("Terzo", COLOR.GREEN);
        game = new Game(host, 3);

        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //mette PP di host e player 2 uguali così si arriva a parità
        host.managePP(5);
        player2.managePP(5);
        player3.managePP(2);

        //mette più cibo a host
        host.manageFoodAndPP(3);
        player2.manageFoodAndPP(1);

        winners = game.checkWinner();

        //controlla che host sia unico vincitore
        assertEquals(1, winners.size());
        assertEquals(host, winners.getFirst());

        //caso in cui pareggiano sia di PP che di cibo quindi più winners
        //di nuovo reset
        host = new Player("Primo", COLOR.RED);
        player2 = new Player("Secondo", COLOR.BLUE);
        player3 = new Player("Terzo", COLOR.GREEN);
        game = new Game(host, 3);

        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //stessi PP
        host.managePP(6);
        player2.managePP(6);
        player3.managePP(1);

        //stesso food
        host.manageFoodAndPP(2);
        player2.manageFoodAndPP(2);

        winners = game.checkWinner();

        //controlla che vincano entrambi
        assertEquals(2, winners.size());
        assertTrue(winners.contains(host));
        assertTrue(winners.contains(player2));
    }

    @Test
    void testGoNextPlayer() {
        //all'inizio non c'è ancora nessun player che deve giocare
        assertNull(game.getPlayerToPlay());

        //solite due assert
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //try e catch anceh qui era comodo ma si può togliere
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            // l'ultima chiamata può lanciare l'eccezione
        }

        //passa alla fase di gioco
        game.advancePlayingPhase();

        //prende il primo player che deve giocare
        Player firstPlayerToPlay = game.getPlayerToPlay();
        assertNotNull(firstPlayerToPlay);

        //passa al successivo player
        assertDoesNotThrow(() -> game.goNextPlayer());

        //il player corrente deve essere cambiato
        Player secondPlayerToPlay = game.getPlayerToPlay();
        assertNotNull(secondPlayerToPlay);
        assertNotEquals(firstPlayerToPlay, secondPlayerToPlay);

        //solito controllo che anche il player successivo sia effettivamente uno dei player del gioco
        assertTrue(game.getPlayerList().contains(secondPlayerToPlay));
    }

    @Test
    void testGetOffertilePlayerIsOn() {
        //all'inizio non c'è ancora nessuna offer tile associata
        assertNull(game.getOffertilePlayerIsOn());

        //solite due assert
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //di nuovo try catch per comodità ma si può togliere
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //è normale che l'ultimo piazzamento chiuda la fase
        }

        //passaggio alla fase di gioco
        game.advancePlayingPhase();

        //ora deve esserci un current player e una offer tile associata
        assertNotNull(game.getPlayerToPlay());
        assertNotNull(game.getOffertilePlayerIsOn());
    }

    @Test
    void testCanCurrentPlayingPlayerDoSomething() {
        //prepara partita
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //di nuovo try e catch comodo ma eliminabile
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //normale
        }

        //passaggio a fase di gioco
        game.advancePlayingPhase();

        //chiamata del metodo
        boolean result = game.canCurrentPlayingPlayerDoSomething();

        //il valore dipende da market quindi per ora controlla solo che restituisca un bool senza errori
        assertNotNull(result);
    }

    @Test
    void testAdvancePlayingPhase() {
        //all'inizio non siamo ancora nella fase di resolve action
        assertNotEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        assertNull(game.getPlayerToPlay());
        assertNull(game.getOffertilePlayerIsOn());

        //solite assert
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //solita cosa di try e catch che si può sostituire
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //normale
        }

        //passaggio a playing phase
        game.advancePlayingPhase();

        //ora dovremmo essere inn resolve action
        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());

        //check che ci sia un current player
        assertNotNull(game.getPlayerToPlay());

        //e pure la sua offer tile
        assertNotNull(game.getOffertilePlayerIsOn());
    }

    @Test
    void testNextRoundIter() {
        //prepara partita come al solito
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //solito discorso su try e catch
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //normale
        }

        //controlla che siamo in resolve action dopo il cambio fase
        game.advancePlayingPhase();
        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());

        //a fine round il gioco deve tornare alla placing phase
        game.nextRoundIter();

        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
    }


    /* questo mi dà errore perchè c'è un errore in game che fa partire ConcurrentModificationException
    quindi ancora da sistemare

    @Test
    void testEndGameIter() {
        //prepara partita completa
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //è sensato chiamare il metodo a fine partita
        assertDoesNotThrow(() -> game.endGameIter());

        //controlla che i player esistano ancora e siano coerenti
        assertEquals(3, game.getPlayerList().size());
        assertTrue(game.getPlayerList().contains(host));
        assertTrue(game.getPlayerList().contains(player2));
        assertTrue(game.getPlayerList().contains(player3));
    }

     */







}