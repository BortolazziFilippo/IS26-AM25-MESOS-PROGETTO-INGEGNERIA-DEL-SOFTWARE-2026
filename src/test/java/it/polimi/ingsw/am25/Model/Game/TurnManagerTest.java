package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Player.Player;
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
        game.addPlayer(player3);

        board = game.getBoard();
        turnManager = new TurnManager(board);
    }

    @Test
    void testGetCurrentPlayingPlayer() throws TileOccupiedException {
        board.placePlayerOnOffertile(host, 0);
        board.placePlayerOnOffertile(player2, 1);
        board.placePlayerOnOffertile(player3, 2);

        turnManager.updatePlayingOrder();

        assertEquals(host, turnManager.getCurrentPlayingPlayer());
    }

    @Test
    void testGetCurrentPlacingPlayer() throws TileOccupiedException {
        board.placePlayerOnDefaultTile(host, 0);
        board.placePlayerOnDefaultTile(player2, 1);
        board.placePlayerOnDefaultTile(player3, 2);

        turnManager.updatePlacingOrder();

        assertEquals(host, turnManager.getCurrentPlacingPlayer());
    }

    @Test
    void testGetPlacingOrder() throws TileOccupiedException {
        board.placePlayerOnDefaultTile(host, 0);
        board.placePlayerOnDefaultTile(player2, 1);
        board.placePlayerOnDefaultTile(player3, 2);

        turnManager.updatePlacingOrder();

        assertEquals(3, turnManager.getPlacingOrder().size());

        //verifica che l'ordine sia quello giusto dall'alto verso il basso della default tile
        //in realtà quindi è come se testasse anche il metodo updatePlacingOrder()
        assertEquals(host, turnManager.getPlacingOrder().get(0));
        assertEquals(player2, turnManager.getPlacingOrder().get(1));
        assertEquals(player3, turnManager.getPlacingOrder().get(2));
    }

    @Test
    void testGetPlayingOrder() throws TileOccupiedException {
        board.placePlayerOnOffertile(host, 0);
        board.placePlayerOnOffertile(player2, 1);
        board.placePlayerOnOffertile(player3, 2);

        turnManager.updatePlayingOrder();

        assertEquals(3, turnManager.getPlayingOrder().size());

        //controlla che il primo che ha posizionato sulla offertile è il primo a giocare e stesso per gli altri
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

        assertEquals(player3, turnManager.getPlacingOrder().get(0));
        assertEquals(host, turnManager.getPlacingOrder().get(1));
        assertEquals(player2, turnManager.getPlacingOrder().get(2));
    }
}