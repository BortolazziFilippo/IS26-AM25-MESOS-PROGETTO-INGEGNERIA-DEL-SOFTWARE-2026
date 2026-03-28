package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.InventorCard;
import it.polimi.ingsw.am25.Model.Enums.*;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodOnNewCoupleInventorsTest {
    private Player playerP1, playerP2;

    @BeforeEach
    void setUp() {
        playerP1= new Player("Lucinda", COLOR.BLUE);
        playerP2= new Player("Genoveffa", COLOR.RED);
    }

    @Test
    void OneCoupleOfInventorsTest() {

        InventorCard card1 = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD);
        playerP1.addCardToTribe(card1);
        InventorCard card2 = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD);
        playerP1.addCardToTribe(card2);

        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_ROUND);
        buildingCard.setBuildingEffect(new FoodOnNewCoupleInventors());
        buildingCard.applyBuildingEffect(playerP1);

        assertEquals(3, playerP1.getFood());

    }
}