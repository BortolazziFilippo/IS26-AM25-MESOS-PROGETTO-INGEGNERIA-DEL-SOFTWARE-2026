package it.polimi.ingsw.am25.Model.SimulationGame;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Board.DefaultTile;
import it.polimi.ingsw.am25.Model.Board.OfferTile;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.Model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.Model.Factory.OfferTile.OfferTileFactory;
import it.polimi.ingsw.am25.Model.Game.Game;
import it.polimi.ingsw.am25.Model.Game.GameView;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SimulationGameTest {
    Player p1, p2, p3;


    @BeforeEach
    void setUp()
    {
        p1 = new Player("Gigi", COLOR.RED);
        p2 = new Player("Maria", COLOR.BLUE);
        p3 = new Player("Bruno", COLOR.YELLOW);
    }

    @Test
    void testSimulationGame() {
        Board board = new Board(new GameView() {
            @Override
            public int getPlayerNumber() {
                return 3;
            }

            @Override
            public List<Player> getPlayerList() {
                return List.of(p1, p2, p3);
            }

            @Override
            public ERA getCurrentEra() {
                return ERA.ERA_I;
            }

            @Override
            public void nextEra() {

            }
        });

        Game gioco = new Game(p1, 3);

        for ( int i = 0; i < 10; i++ )
        {
            if (i == 0){ // nel primo turno si scelgono a piacimento l'ordine sulla default tile
                board.placePlayerOnDefaultTile(p1, 0);
                board.placePlayerOnDefaultTile(p2, 1);
                board.placePlayerOnDefaultTile(p3, 2);
                board.placePlayerOnOffertile(p1, 3);
                board.placePlayerOnDefaultTile(p2, 4);
                board.placePlayerOnDefaultTile(p3, 1);
                //gioco.getMarket().selectCardFromBottomList( , );
            }













            if(i%3 == 0){
                gioco.nextEra();
            }
        }


    }
}
