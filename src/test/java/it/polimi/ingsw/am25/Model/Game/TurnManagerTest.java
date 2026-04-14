package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EndOfPlacingPhaseException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EndOfPlayingPhaseException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.GameReadyToStartException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.TileOccupiedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnManagerTest {

    private Player host;
    private Player player2;
    private Player player3;
    private Game game;
    private Board board;
    private TurnManager turnManager;

    @BeforeEach
    void setup() {
        host = new Player("primo", COLOR.RED);
        player2 = new Player("secondo", COLOR.BLUE);
        player3 = new Player("terzo", COLOR.GREEN);

        game = new Game(host, 3);
        game.addPlayer(player2);
        assertThrows(GameReadyToStartException.class,()->game.addPlayer(player3));

        board = game.getBoard();
        turnManager = new TurnManager(board);
    }

    @Test
    void testGetNextPlayingPlayer() throws TileOccupiedException {
        board.placePlayerOnOffertile(host, 0);
        board.placePlayerOnOffertile(player2, 1);
        board.placePlayerOnOffertile(player3, 2);

        turnManager.updatePlayingOrder();

        assertEquals(3, turnManager.getPlayingOrder().size());
        assertEquals(host, turnManager.getNextPlayingPlayer());
        assertEquals(2, turnManager.getPlayingOrder().size());
        assertEquals(player2, turnManager.getNextPlayingPlayer());
        assertEquals(1, turnManager.getPlayingOrder().size());
        assertEquals(player3, turnManager.getNextPlayingPlayer());
        assertTrue(turnManager.getPlayingOrder().isEmpty());
        assertThrows(EndOfPlayingPhaseException.class, () -> turnManager.getNextPlayingPlayer());
    }

    @Test
    void testGetNextPlacingPlayer() throws TileOccupiedException {
        board.placePlayerOnDefaultTile(host, 0);
        board.placePlayerOnDefaultTile(player2, 1);
        board.placePlayerOnDefaultTile(player3, 2);

        turnManager.updatePlacingOrder();

        assertEquals(3, turnManager.getPlacingOrder().size());
        assertEquals(host, turnManager.getNextPlacingPlayer());
        assertEquals(2, turnManager.getPlacingOrder().size());
        assertEquals(player2, turnManager.getNextPlacingPlayer());
        assertEquals(1, turnManager.getPlacingOrder().size());
        assertEquals(player3, turnManager.getNextPlacingPlayer());
        assertTrue(turnManager.getPlacingOrder().isEmpty());
        assertThrows(EndOfPlacingPhaseException.class, () -> turnManager.getNextPlacingPlayer());
    }

    @Test
    void testGetPlacingOrder() throws TileOccupiedException {
        assertNotNull(turnManager.getPlacingOrder());
        assertTrue(turnManager.getPlacingOrder().isEmpty());

        board.placePlayerOnDefaultTile(host, 0);
        board.placePlayerOnDefaultTile(player2, 1);
        board.placePlayerOnDefaultTile(player3, 2);

        turnManager.updatePlacingOrder();

        assertEquals(3, turnManager.getPlacingOrder().size());
        assertEquals(host, turnManager.getPlacingOrder().get(0));
        assertEquals(player2, turnManager.getPlacingOrder().get(1));
        assertEquals(player3, turnManager.getPlacingOrder().get(2));
    }

    @Test
    void testGetPlayingOrder() throws TileOccupiedException {
        assertNotNull(turnManager.getPlayingOrder());
        assertTrue(turnManager.getPlayingOrder().isEmpty());

        board.placePlayerOnOffertile(host, 0);
        board.placePlayerOnOffertile(player2, 1);
        board.placePlayerOnOffertile(player3, 2);

        turnManager.updatePlayingOrder();

        assertEquals(3, turnManager.getPlayingOrder().size());
        assertEquals(host, turnManager.getPlayingOrder().get(0));
        assertEquals(player2, turnManager.getPlayingOrder().get(1));
        assertEquals(player3, turnManager.getPlayingOrder().get(2));
    }

    @Test
    void testUpdatePlayingOrder() throws TileOccupiedException {
        board.placePlayerOnOffertile(player2, 0);
        board.placePlayerOnOffertile(host, 1);
        board.placePlayerOnOffertile(player3, 2);

        turnManager.updatePlayingOrder();

        assertEquals(3, turnManager.getPlayingOrder().size());
        assertEquals(player2, turnManager.getPlayingOrder().get(0));
        assertEquals(host, turnManager.getPlayingOrder().get(1));
        assertEquals(player3, turnManager.getPlayingOrder().get(2));
    }

    @Test
    void testUpdatePlacingOrder() throws TileOccupiedException {
        board.placePlayerOnDefaultTile(player3, 0);
        board.placePlayerOnDefaultTile(host, 1);
        board.placePlayerOnDefaultTile(player2, 2);

        turnManager.updatePlacingOrder();

        assertEquals(3, turnManager.getPlacingOrder().size());
        assertEquals(player3, turnManager.getPlacingOrder().get(0));
        assertEquals(host, turnManager.getPlacingOrder().get(1));
        assertEquals(player2, turnManager.getPlacingOrder().get(2));
    }




    //TODO:these last two tests still need to be reviewed

    @Test
    void testGetNextPlayingPlayerEmptyOrder() {
        assertTrue(turnManager.getPlayingOrder().isEmpty());
        assertThrows(EndOfPlayingPhaseException.class, () -> turnManager.getNextPlayingPlayer());
    }

    @Test
    void testGetNextPlacingPlayerEmptyOrder() {
        assertTrue(turnManager.getPlacingOrder().isEmpty());
        assertThrows(EndOfPlacingPhaseException.class, () -> turnManager.getNextPlacingPlayer());
    }




}