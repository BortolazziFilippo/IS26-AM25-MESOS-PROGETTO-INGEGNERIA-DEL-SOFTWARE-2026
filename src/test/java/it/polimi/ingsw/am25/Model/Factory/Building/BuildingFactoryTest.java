package it.polimi.ingsw.am25.Model.Factory.Building;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Game.Game;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class BuildingFactoryTest {

    @Test
    void testShouldCreateRightAmountOfBuilding(){

        Game game= new Game(new Player("player",COLOR.RED),3);
        Board board= new Board(game);
        BuildingFactory buildingFactory = new BuildingFactory();
        //case two player
        List<BuildingCard> buildingCard = buildingFactory.createBuildingDeck(2,board);
        //total Number
        assertEquals(6,  buildingCard.size());
        //ERA I
        assertEquals(1,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_I).count());
        //ERA II
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_II).count());
        //ERA III
        assertEquals(3,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_III).count());


        //case three player
        buildingCard = buildingFactory.createBuildingDeck(3,board);
        assertEquals(8,  buildingCard.size());
        //ERA I
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_I).count());
        //ERA II
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_II).count());
        //ERA III
        assertEquals(4,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_III).count());


        // case four player
        buildingCard = buildingFactory.createBuildingDeck(4,board);
        assertEquals(9,  buildingCard.size());
        //ERA I
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_I).count());
        //ERA II
        assertEquals(3,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_II).count());
        //ERA III
        assertEquals(4,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_III).count());


        //case five player
        buildingCard = buildingFactory.createBuildingDeck(5,board);
        assertEquals(10,  buildingCard.size());
        //ERA I
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_I).count());
        //ERA II
        assertEquals(3,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_II).count());
        //ERA III
        assertEquals(5,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_III).count());
    }
}