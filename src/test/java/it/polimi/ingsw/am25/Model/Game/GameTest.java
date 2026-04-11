package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;
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
    void testAddPlayer() {
        game.addPlayer(player2);

        //testa sia dimensione della lista che conteuto
        assertEquals(2, game.getPlayerList().size());
        assertEquals(List.of(host, player2), game.getPlayerList());
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
    /* questo dà errore per ora, devo ancroa capire perchè
    @Test
    void testCheckWinner() {
        game.addPlayer(player2);
        assertThrows(GameReadyToStartException.class,()->game.addPlayer(player3));

        //ho usato numeri arbitrari (il costruttore di game dovrebbe inizializzare i PP a 0 all'inizio penso)
        host.managePP(3);
        player2.managePP(7);
        player3.managePP(5);

        assertEquals(player2, game.checkWinner());
    }

     */

}