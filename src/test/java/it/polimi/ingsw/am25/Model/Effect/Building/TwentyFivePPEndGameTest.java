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

class TwentyFivePPEndGameTest {
    private Player playerP1, playerP2;

    @BeforeEach
    void setUp() {
        playerP1= new Player("Giorgino", COLOR.BLUE);
        playerP2= new Player("Beppe", COLOR.RED);
    }


    @Test
    void applyEffect() {

        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_GAME);
        buildingCard.setBuildingEffect(new TwentyFivePPEndGame());
        buildingCard.applyBuildingEffect(playerP1);

        assertEquals(25, playerP1.getPrestigePoint());
    }
}