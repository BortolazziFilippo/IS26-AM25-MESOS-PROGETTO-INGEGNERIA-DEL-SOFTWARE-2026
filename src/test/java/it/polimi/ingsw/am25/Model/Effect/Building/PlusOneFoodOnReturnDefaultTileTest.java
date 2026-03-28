package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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


        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_ROUND);
        buildingCard.setBuildingEffect(new PlusOneFoodOnReturnDefaultTile());
        buildingCard.applyBuildingEffect(p1);



    }
}