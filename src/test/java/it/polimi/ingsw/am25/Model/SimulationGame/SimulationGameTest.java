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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        Random random = new  Random();
        List<Integer> positions = new ArrayList<>();
        Map<Player, Integer> map = new HashMap<>();
        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);
        players.add(p3);
        int j=0;
        for (int i = 0; i < 5; i++) {
            positions.add(i);
        }

        for ( int i = 0; i < 10; i++ )
        {
            if (i == 0){ // nel primo turno si scelgono a piacimento l'ordine sulla default tile
                board.placePlayerOnDefaultTile(p1, 0);
                board.placePlayerOnDefaultTile(p2, 1);
                board.placePlayerOnDefaultTile(p3, 2);
                board.placePlayerOnOffertile(p1, 3);
                board.placePlayerOnOffertile(p2, 4);
                board.placePlayerOnOffertile(p3, 1);
                gioco.getMarket().selectCardFromTopList(random.nextInt(7), p1);
                gioco.getMarket().selectCardFromTopList(random.nextInt(6), p2);
                gioco.getMarket().selectCardFromTopList(random.nextInt(5), p2);
                gioco.getMarket().selectCardFromTopList(random.nextInt(5), p3);
                board.returnOnDefaultTiles();
                gioco.getMarket().endOfRoundMarketActions();
            }
            // inizio round i giocatori si posizionano sulla OfferTile, dato che sono già sistemati sulla DefaultTile dal
            // precedente round
            Collections.shuffle(positions);
            board.placePlayerOnOffertile(p1, positions.get(0));
            board.placePlayerOnOffertile(p2, positions.get(1));
            board.placePlayerOnOffertile(p3, positions.get(2));
            map.put(p1, positions.get(0));
            map.put(p2, positions.get(1));
            map.put(p3, positions.get(2));

            // risolvo le azioni sulla Offertile
            for(Map.Entry<Player, Integer> entry : map.entrySet()){
                Player player = entry.getKey();
                int position = entry.getValue();
                switch (position) {
                    case 0:
                        gioco.getMarket().selectCardFromBottomList(random.nextInt(7), player);
                        break;
                    case 1:
                        gioco.getMarket().selectCardFromTopList(random.nextInt(7), player);
                        break;
                    case 2:
                        gioco.getMarket().selectCardFromBottomList(random.nextInt(7), player);
                        gioco.getMarket().selectCardFromBottomList(random.nextInt(7), player);
                        break;
                    case 3:
                        gioco.getMarket().selectCardFromTopList(random.nextInt(7), player);
                        gioco.getMarket().selectCardFromBottomList(random.nextInt(7), player);
                        break;
                    case 4:
                        gioco.getMarket().selectCardFromTopList(random.nextInt(7), player);
                        gioco.getMarket().selectCardFromTopList(random.nextInt(7), player);
                        break;
                    default:
                        System.out.println("posizione non valida");

                }
            }

            // riposiziono in DefaultTile in ordine crescente dalla OfferTile
            board.returnOnDefaultTiles();
            gioco.getMarket().endOfRoundMarketActions();

            if(i%3 == 0){
                gioco.nextEra();
            }
        }


        Player w = p1;
        for (Player p : players) {
            if (p.getPrestigePoint() > w.getPrestigePoint()) {
                w = p;
                System.out.println(" PP: " +p.getPrestigePoint());
            }
        }
        assertEquals(w, gioco.checkWinner());
    }
}
