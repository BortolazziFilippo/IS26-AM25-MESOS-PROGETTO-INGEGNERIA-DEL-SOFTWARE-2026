package it.polimi.ingsw.am25.server.Effect.Building;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.GathererCard;
import it.polimi.ingsw.am25.server.model.Effect.Building.DiscountFoodOnSustenance;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscountFoodOnSustenanceTest {

    @Test
    //at the end the player should have a discounted cost on end of sustenance
    void applyEffectTest() {
        Player player=new Player("Lorem Ipsum", COLOR.RED);
        player.manageFoodAndPP(15);
        player.addCardToTribe(new GathererCard(ERA.ERA_III, CARD_TYPE.GATHERER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        //now i "Apply the effect" manually
        //first i subtract the amount foreach card in the collection
        player.manageFoodAndPP(-(player.getTribe().size()));
        assertEquals(13,player.getFood());
        player.manageFoodAndPP(2);
        //now i test with the discount effect
        BuildingCard buildingCard= new BuildingCard(ERA.ERA_II,CARD_TYPE.BUILDING,1,10,10, EVENT_TYPE.SUSTENANCE);
        buildingCard.setBuildingEffect(new DiscountFoodOnSustenance(CARD_TYPE.GATHERER));
        buildingCard.applyBuildingEffect(player);
        player.manageFoodAndPP(-(player.getTribe().size()));
        assertEquals(14,player.getFood()); //the food removed is one instead of two
        buildingCard.setBuildingEffect(new DiscountFoodOnSustenance(CARD_TYPE.ARTIST));
        buildingCard.applyBuildingEffect(player);
        player.manageFoodAndPP(-(player.getTribe().size()));
        assertEquals(13,player.getFood());


    }
}