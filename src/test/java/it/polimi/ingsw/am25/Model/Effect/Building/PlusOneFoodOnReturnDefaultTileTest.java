package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Game.GameView;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlusOneFoodOnReturnDefaultTileTest {
    Player  p1, p2;

    @BeforeEach
    void setUp() {
        p1= new Player("Lucinda", COLOR.BLUE);
        p2= new Player("Genoveffa", COLOR.RED);
    }

    @Test
    void Test1() {

        Board board = new Board(new GameView() {
            @Override
            public int getPlayerNumber() {
                return 2;
            }

            @Override
            public List<Player> getPlayerList() {
                return List.of(p1, p2);
            }

            @Override
            public ERA getCurrentEra() {
                return ERA.ERA_I;
            }

            @Override
            public void nextEra() {

            }
        });

        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_ROUND);
        PlusOneFoodOnReturnDefaultTile test = new PlusOneFoodOnReturnDefaultTile();
        buildingCard.setBuildingEffect(test);
        test.setBoardView(board);
        board.placePlayerOnDefaultTile(p1, 0); //the position index may need to change depending on how player positions are numbered
        buildingCard.applyBuildingEffect(p1);

        assertEquals(1, p1.getFood());
    }
}