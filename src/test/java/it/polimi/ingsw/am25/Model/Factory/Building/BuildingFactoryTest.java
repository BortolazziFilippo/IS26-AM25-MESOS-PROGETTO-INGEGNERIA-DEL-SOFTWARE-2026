package it.polimi.ingsw.am25.Model.Factory.Building;

import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class BuildingFactoryTest {

    @Test
    void testShouldCreateRightAmountOfBuilding(){
        BuildingFactory buildingFactory = new BuildingFactory();
        //case two player
        List<BuildingCard> buildingCard = buildingFactory.createBuildingDeck(2);
        //total Number
        assertEquals(6,buildingCard.stream().count());
        //ERA I
        assertEquals(1,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_I).count());
        //ERA II
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_II).count());
        //ERA III
        assertEquals(3,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_III).count());


        //case three player
        buildingCard = buildingFactory.createBuildingDeck(3);
        assertEquals(8,buildingCard.stream().count());
        //ERA I
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_I).count());
        //ERA II
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_II).count());
        //ERA III
        assertEquals(4,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_III).count());


        // case four player
        buildingCard = buildingFactory.createBuildingDeck(4);
        assertEquals(9,buildingCard.stream().count());
        //ERA I
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_I).count());
        //ERA II
        assertEquals(3,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_II).count());
        //ERA III
        assertEquals(4,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_III).count());


        //case five player
        buildingCard = buildingFactory.createBuildingDeck(5);
        assertEquals(10,buildingCard.stream().count());
        //ERA I
        assertEquals(2,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_I).count());
        //ERA II
        assertEquals(3,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_II).count());
        //ERA III
        assertEquals(5,buildingCard.stream().filter(BuildingCard->BuildingCard.getEra()== ERA.ERA_III).count());
    }
}