package it.polimi.ingsw.am25.server.Board;

import it.polimi.ingsw.am25.server.model.Board.Board;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Game.Game;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameReadyToStartException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.TileOccupiedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    private Player p1;
    private Player p2;
    private Player p3;
    private Player p4;
    private Game game;
    @BeforeEach
    void createPlayer(){
        this.p1 = new Player("P1", COLOR.RED);
        this.p2 = new Player("P2", COLOR.BLUE);
        this.p3 = new Player("P3", COLOR.YELLOW);
        this.p4 = new Player("P4", COLOR.GREEN);
        this.game = new Game(p1,4);
        game.addPlayer(p2);
        game.addPlayer(p3);
        assertThrows(GameReadyToStartException.class,()-> game.addPlayer(p4));
    }

    @Test
    void placePlayer() {
        Board board= game.getBoard();
        board.placePlayerOnDefaultTile(p1,0);
        board.placePlayerOnDefaultTile(p2,1);
        board.placePlayerOnDefaultTile(p3,2);
        board.placePlayerOnDefaultTile(p4,3);

        assertEquals(p1,board.getDefaultTiles().get(0).getPlayerOn());
        assertEquals(p2,board.getDefaultTiles().get(1).getPlayerOn());
        assertEquals(p3,board.getDefaultTiles().get(2).getPlayerOn());
        assertEquals(p4,board.getDefaultTiles().get(3).getPlayerOn());

        board.placePlayerOnOffertile(p1,0);
        board.placePlayerOnOffertile(p2,1);
        board.placePlayerOnOffertile(p3,2);
        board.placePlayerOnOffertile(p4,3);

        assertNull(board.getDefaultTiles().get(0).getPlayerOn());
        assertNull(board.getDefaultTiles().get(1).getPlayerOn());
        assertNull(board.getDefaultTiles().get(2).getPlayerOn());
        assertNull(board.getDefaultTiles().get(3).getPlayerOn());

        assertEquals(p1,board.getOfferTiles().get(0).getPlayerOn());
        assertEquals(p2,board.getOfferTiles().get(1).getPlayerOn());
        assertEquals(p3,board.getOfferTiles().get(2).getPlayerOn());
        assertEquals(p4,board.getOfferTiles().get(3).getPlayerOn());
    }
    @Test
    void returnDefaultTileTest(){
        Board board= game.getBoard();
        board.placePlayerOnDefaultTile(p1,0);
        board.placePlayerOnDefaultTile(p2,1);
        board.placePlayerOnDefaultTile(p3,2);
        board.placePlayerOnDefaultTile(p4,3);

        board.placePlayerOnOffertile(p1,0);
        board.placePlayerOnOffertile(p2,1);
        board.placePlayerOnOffertile(p3,2);
        board.placePlayerOnOffertile(p4,3);

        board.returnOnDefaultTiles();

        assertNull(board.getOfferTiles().get(0).getPlayerOn());
        assertNull(board.getOfferTiles().get(1).getPlayerOn());
        assertNull(board.getOfferTiles().get(2).getPlayerOn());
        assertNull(board.getOfferTiles().get(3).getPlayerOn());

        assertEquals(p1,board.getDefaultTiles().get(0).getPlayerOn());
        assertEquals(p2,board.getDefaultTiles().get(1).getPlayerOn());
        assertEquals(p3,board.getDefaultTiles().get(2).getPlayerOn());
        assertEquals(p4,board.getDefaultTiles().get(3).getPlayerOn());

        assertEquals(2,p1.getFood());
        assertEquals(1,p2.getFood());
        assertEquals(0,p3.getFood());
        assertEquals(-2,p4.getPrestigePoint());
    }
    @Test

    void theOrderShouldBeCorrect(){
        Board board= game.getBoard();
        board.placePlayerOnDefaultTile(p1,0);
        board.placePlayerOnDefaultTile(p2,1);
        board.placePlayerOnDefaultTile(p3,2);
        board.placePlayerOnDefaultTile(p4,3);

        board.placePlayerOnOffertile(p1,3);
        board.placePlayerOnOffertile(p2,5);
        board.placePlayerOnOffertile(p3,0);
        board.placePlayerOnOffertile(p4,2);

        board.returnOnDefaultTiles();

        assertEquals(p3,board.getDefaultTiles().get(0).getPlayerOn());
        assertEquals(p4,board.getDefaultTiles().get(1).getPlayerOn());
        assertEquals(p1,board.getDefaultTiles().get(2).getPlayerOn());
        assertEquals(p2,board.getDefaultTiles().get(3).getPlayerOn());
    }
    @Test
    void shouldLaunchCorrectedException(){
        Board board= game.getBoard();
        board.placePlayerOnDefaultTile(p1,0);
        assertThrows(TileOccupiedException.class, () -> board.placePlayerOnDefaultTile(p2, 0));
        assertThrows(IndexOutOfBoundsException.class,()->board.placePlayerOnDefaultTile(p2,4));
        assertThrows(IndexOutOfBoundsException.class,()->board.placePlayerOnDefaultTile(p2,-1));
        board.placePlayerOnDefaultTile(p3,2);
        board.placePlayerOnDefaultTile(p4,3);


        assertThrows(IndexOutOfBoundsException.class, ()->board.placePlayerOnOffertile(p3,6));
        assertThrows(IndexOutOfBoundsException.class, ()->board.placePlayerOnOffertile(p3,-1));

        board.placePlayerOnOffertile(p3,0);
        assertThrows(TileOccupiedException.class,()->board.placePlayerOnOffertile(p1,0));
    }

}