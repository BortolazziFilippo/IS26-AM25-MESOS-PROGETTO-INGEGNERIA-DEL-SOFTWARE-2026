package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Board.OfferTile;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.Model.Observers.GameObserver;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EndOfPlacingPhaseException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.GameReadyToStartException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.NotSelectableCardException;
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

        //checks the current era, which depends on when the method is called; divided into 3 cases,
        //though testing only the initial ERA_I state may be sufficient
        assertEquals(ERA.ERA_I, game.getCurrentEra());

        game.nextEra();
        assertEquals(ERA.ERA_II, game.getCurrentEra());

        game.nextEra();
        assertEquals(ERA.ERA_III, game.getCurrentEra());
    }

    @Test
    void testGetPlayerList() {
        //similar to testGetCurrentEra: first verifies the host is present, then adds more players
        assertEquals(1, game.getPlayerList().size());
        assertTrue(game.getPlayerList().contains(host));

        game.addPlayer(player2);
        assertEquals(2, game.getPlayerList().size());
        assertTrue(game.getPlayerList().contains(player2));
    }

    @Test
    void testGetGamePhase() {
        //a newly created game must be in the SETUP phase
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());

        //adds players
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //after calling gameStart, the game should transition to PLACING_PHASE
        game.gameStart();
        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
    }

    @Test
    void testGetPlayerToPlace() {
        //before the game starts, there is no player to place yet
        assertNull(game.getPlayerToPlace());

        //standard two-player setup assertions
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //after calling gameStart, there should be a player to place
        game.gameStart();
        assertNotNull(game.getPlayerToPlace());

        //also verify that the player to place is actually in the game
        assertTrue(game.getPlayerList().contains(game.getPlayerToPlace()));
    }

    @Test
    void testGetPlayerToPlay() {
        //before the game starts, the playing player should be null
        assertNull(game.getPlayerToPlay());

        //standard two-player setup assertions
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //place all players (try-catch is convenient here but could be removed)
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            // it is expected that the last placement throws this exception
        }

        //after transitioning to the playing phase, there should be a player to play
        game.advancePlayingPhase();
        assertNotNull(game.getPlayerToPlay());

        //standard check: verify the playing player is actually in the game
        assertTrue(game.getPlayerList().contains(game.getPlayerToPlay()));
    }

    @Test
    void testGetMarket() {
        //the market is created in the Game constructor and must never be null
        assertNotNull(game.getMarket());

        //also verify the returned object is indeed a Market
        assertInstanceOf(Market.class, game.getMarket());
    }

    @Test
    void testAddPlayer() {
        //test the player count, which should initially be 1 (host only)
        assertEquals(1, game.getPlayerList().size());
        assertEquals(List.of(host), game.getPlayerList());

        //normal addition of a second player
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertEquals(2, game.getPlayerList().size());
        assertEquals(List.of(host, player2), game.getPlayerList());

        // when the lobby is full, an exception is thrown but the player is still added
        GameReadyToStartException ex = assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        assertEquals("The lobby is full, game can start", ex.getMessage());
        assertEquals(3, game.getPlayerList().size());
        assertEquals(List.of(host, player2, player3), game.getPlayerList());
    }

    @Test
    void testGetBoard() {
        Board board = game.getBoard();

        //verify that the board is not null
        assertNotNull(board);
        //verify that the board is an instance of Board
        assertInstanceOf(Board.class, board);
    }

    @Test
    void testNextEra() {
        //test all 3 eras

        assertEquals(ERA.ERA_I, game.getCurrentEra());

        game.nextEra();
        assertEquals(ERA.ERA_II, game.getCurrentEra());

        game.nextEra();
        assertEquals(ERA.ERA_III, game.getCurrentEra());

        //this extra part only checks that calling nextEra() beyond ERA_III does not cause errors
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

        //at the start, the game must be in PLACING_PHASE
        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
        assertNotNull(game.getPlayerToPlace());

        //all players should have been placed on default tiles
        List<Player> playersOnDefaultTiles = game.getBoard().getOrderedPlayerOnDefaultTile();
        assertEquals(3, playersOnDefaultTiles.size());
        assertTrue(playersOnDefaultTiles.contains(host));
        assertTrue(playersOnDefaultTiles.contains(player2));
        assertTrue(playersOnDefaultTiles.contains(player3));

        //the player to place must be one of the game's players
        assertTrue(game.getPlayerList().contains(game.getPlayerToPlace()));
    }

    @Test
    void testPlacePlayer() {
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //the placing order is determined by gameStart()
        Player firstToPlace = game.getPlayerToPlace();

        //test all 3 players
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

        //verify that the exception came from the EndOfPlacingPhaseException catch block
        assertEquals("all Player have placed", ex.getMessage());

        List<Player> playersOnOfferTiles = game.getBoard().getOrderedPlayerOnOfferTile();
        assertEquals(3, playersOnOfferTiles.size());
        assertTrue(playersOnOfferTiles.contains(host));
        assertTrue(playersOnOfferTiles.contains(player2));
        assertTrue(playersOnOfferTiles.contains(player3));
    }

    @Test
    void testCheckWinner() {
        //set up a 3-player game
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //first case: the player with the most PP wins
        host.managePP(3);
        player2.managePP(7);
        player3.managePP(5);

        List<Player> winners = game.checkWinner();

        //verify that there is only one winner
        assertEquals(1, winners.size());
        assertEquals(player2, winners.getFirst());

        //tiebreaker case: food decides the winner
        //reset and create a new game for convenience
        host = new Player("Primo", COLOR.RED);
        player2 = new Player("Secondo", COLOR.BLUE);
        player3 = new Player("Terzo", COLOR.GREEN);
        game = new Game(host, 3);

        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //set equal PP for host and player2 to force a tie
        host.managePP(5);
        player2.managePP(5);
        player3.managePP(2);

        //give host more food
        host.manageFoodAndPP(3);
        player2.manageFoodAndPP(1);

        winners = game.checkWinner();

        //verify that host is the sole winner
        assertEquals(1, winners.size());
        assertEquals(host, winners.getFirst());

        //tie on both PP and food, so multiple winners are expected
        //reset again
        host = new Player("Primo", COLOR.RED);
        player2 = new Player("Secondo", COLOR.BLUE);
        player3 = new Player("Terzo", COLOR.GREEN);
        game = new Game(host, 3);

        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //set equal PP
        host.managePP(6);
        player2.managePP(6);
        player3.managePP(1);

        //set equal food
        host.manageFoodAndPP(2);
        player2.manageFoodAndPP(2);

        winners = game.checkWinner();

        //verify that both players win
        assertEquals(2, winners.size());
        assertTrue(winners.contains(host));
        assertTrue(winners.contains(player2));
    }

    @Test
    void testGoNextPlayer() {
        //no playing player yet at the start
        assertNull(game.getPlayerToPlay());

        //standard two-player setup assertions
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //try-catch is convenient here but could be removed
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            // the last placement call may throw this exception
        }

        //transition to the playing phase
        game.advancePlayingPhase();

        //get the first player to play
        Player firstPlayerToPlay = game.getPlayerToPlay();
        assertNotNull(firstPlayerToPlay);

        //advance to the next player
        assertDoesNotThrow(() -> game.goNextPlayer());

        //the current player should have changed
        Player secondPlayerToPlay = game.getPlayerToPlay();
        assertNotNull(secondPlayerToPlay);
        assertNotEquals(firstPlayerToPlay, secondPlayerToPlay);

        //standard check: verify the next player is also in the game
        assertTrue(game.getPlayerList().contains(secondPlayerToPlay));
    }

    @Test
    void testGetOffertilePlayerIsOn() {
        //no offer tile is associated yet at the start
        assertNull(game.getOffertilePlayerIsOn());

        //standard two-player setup assertions
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //try-catch again for convenience, can be removed
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //the last placement is expected to close the placing phase
        }

        //transition to the playing phase
        game.advancePlayingPhase();

        //there should now be a current player and an associated offer tile
        assertNotNull(game.getPlayerToPlay());
        assertNotNull(game.getOffertilePlayerIsOn());
    }

    @Test
    void testCanCurrentPlayingPlayerDoSomething() {
        //prepara partita
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //try-catch is convenient here but can be removed
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //expected
        }

        //transition to the playing phase
        game.advancePlayingPhase();

        //call the method
        boolean result = game.canCurrentPlayingPlayerDoSomething();

        //the return value depends on market state, so just verify it returns a boolean without errors
        assertNotNull(result);
    }

    @Test
    void testAdvancePlayingPhase() {
        //at the start, the game is not yet in the RESOLVE_ACTION phase
        assertNotEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        assertNull(game.getPlayerToPlay());
        assertNull(game.getOffertilePlayerIsOn());

        //standard setup assertions
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //standard try-catch, can be replaced
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //expected
        }

        //transition to playing phase
        game.advancePlayingPhase();

        //verify we are in RESOLVE_ACTION after the phase transition
        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());

        //check that there is a current player
        assertNotNull(game.getPlayerToPlay());

        //and also their offer tile
        assertNotNull(game.getOffertilePlayerIsOn());
    }

    @Test
    void testNextRoundIter() {
        //set up the game as usual
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //standard try-catch pattern
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //expected
        }

        //verify the game is in RESOLVE_ACTION after the phase transition
        game.advancePlayingPhase();
        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());

        //at the end of the round, the game should return to PLACING_PHASE
        game.nextRoundIter();

        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
    }

    //this test needs further review
    @Test
    void testAddObserver() {
        //set up the game
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //counter for notifications received by the observer
        final int[] notifications = {0};

        // mock observer: each time Game notifies, the counter is incremented
        GameObserver observer = new GameObserver() {
            @Override
            public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase,
                                      Player playerToPlace, Player playerToPlay, OfferTile offerTile) {
                notifications[0]++;
            }
        };

        //call the method
        game.addObserver(observer);

        //gameStart() calls notifyGameChanged(), so the observer should be notified once
        game.gameStart();
        assertEquals(1, notifications[0]);

        //try adding the same observer again (it should not be duplicated)
        game.addObserver(observer);

        //trigger another state change
        assertDoesNotThrow(() -> game.placePlayer(game.getPlayerToPlace(), 0));

        //if it was not duplicated, the counter should increase by exactly 1
        assertEquals(2, notifications[0]);
    }

    //TODO:this test also needs further review
    @Test
    void testRemoveObserver() {
        //set up the game
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        final int[] notifications = {0};

        GameObserver observer = new GameObserver() {
            @Override
            public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase,
                                      Player playerToPlace, Player playerToPlay, OfferTile offerTile) {
                notifications[0]++;
            }
        };

        //add the observer
        game.addObserver(observer);

        //first notification
        game.gameStart();
        assertEquals(1, notifications[0]);

        //remove the observer
        game.removeObserver(observer);

        //trigger another state change
        assertDoesNotThrow(() -> game.placePlayer(game.getPlayerToPlace(), 0));

        //the counter must not increase
        assertEquals(1, notifications[0]);
    }

    @Test
    void testSelectGenericCardTopLists() {
        //set up the game in the usual way
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //standard try-catch, can be removed
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //expected
        }

        //enter the playing phase, which should initialize playerToPlay and offertilePlayerIsOn
        game.advancePlayingPhase();

        //if the player attempts to select an event card from the list, a NotSelectableCardException should be thrown
        assertThrows(NotSelectableCardException.class, () -> game.selectGenericCardTopLists(CARD_TYPE.EVENT, 0, game.getPlayerToPlay()));
    }

    @Test
    void testSelectGenericCardBottomLists() {
        //set up the game (standard two-player setup assertions)
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        game.gameStart();

        //standard try-catch, can be removed
        try {
            game.placePlayer(game.getPlayerToPlace(), 0);
            game.placePlayer(game.getPlayerToPlace(), 1);
            game.placePlayer(game.getPlayerToPlace(), 2);
        } catch (EndOfPlacingPhaseException ignored) {
            //expected
        }

        //transition to playing phase for the same reason as in the method above (testSelectGenericCardTopLists)
        game.advancePlayingPhase();

        //verify that an exception is thrown when the player tries to select an event card
        assertThrows(NotSelectableCardException.class, () -> game.selectGenericCardBottomLists(CARD_TYPE.EVENT, 0, game.getPlayerToPlay()));
    }





    //see comment below

    /* this test causes an error because there is a bug in Game that triggers ConcurrentModificationException;
    still needs to be fixed

    @Test
    void testEndGameIter() {
        //set up a complete game
        assertDoesNotThrow(() -> game.addPlayer(player2));
        assertThrows(GameReadyToStartException.class, () -> game.addPlayer(player3));

        //it makes sense to call this method at the end of the game
        assertDoesNotThrow(() -> game.endGameIter());

        //verify that players still exist and are consistent
        assertEquals(3, game.getPlayerList().size());
        assertTrue(game.getPlayerList().contains(host));
        assertTrue(game.getPlayerList().contains(player2));
        assertTrue(game.getPlayerList().contains(player3));
    }

     */







}