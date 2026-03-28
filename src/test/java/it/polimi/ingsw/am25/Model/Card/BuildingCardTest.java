package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Effect.Building.TwentyFivePPEndGame;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingCardTest {

    private Player player;
    private BuildingCard buildingCard;

    @BeforeEach
    void setup(){
        player = new Player("Mario", COLOR.BLUE);
        buildingCard = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 4, 3, EVENT_TYPE.END_GAME);
        buildingCard.setBuildingEffect(new TwentyFivePPEndGame());
    }

    @Test
    void testIsBuilderOK(){
        assertEquals(ERA.ERA_I, buildingCard.getEra());
        assertEquals(CARD_TYPE.BUILDING, buildingCard.getCardType());
        assertEquals(1, buildingCard.getBuildingID());
        assertEquals(4, buildingCard.getFoodCost());
        assertEquals(3, buildingCard.getEndgamePP());
        assertEquals(EVENT_TYPE.END_GAME, buildingCard.getApplyOn());
    }

    @Test
    void testAddCardToPlayer() {
        buildingCard.addCardToPlayer(player);
        assertEquals(List.of(buildingCard), player.getBuildingCards());
    }

    @Test
    void testApplyBuildingEffect() {
        buildingCard.addCardToPlayer(player);
        buildingCard.applyBuildingEffect(player);
        assertEquals(25, player.getPrestigePoint());
    }
}