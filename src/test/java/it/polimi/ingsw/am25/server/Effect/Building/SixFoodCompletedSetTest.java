package it.polimi.ingsw.am25.server.Effect.Building;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Effect.Building.SixFoodCompletedSet;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SixFoodCompletedSetTest {
    @Test
    public void applyEffect_completedSet_awardsSixFood() {
        Player player = new Player("Lorem Ipsum", COLOR.RED);
        player.manageFoodAndPP(5);
        BuildingCard buildingCard= new BuildingCard(ERA.ERA_III, CARD_TYPE.BUILDING,1,10,10, EVENT_TYPE.END_ROUND);
        buildingCard.setBuildingEffect(new SixFoodCompletedSet());
        buildingCard.applyBuildingEffect(player);
        assertEquals(5,player.getFood());
        //for semplicity I used the same class but changed type of card
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        buildingCard.applyBuildingEffect(player);
        assertEquals(5,player.getFood());
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.GATHERER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.BUILDER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.SHAMAN));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.HUNTER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        buildingCard.applyBuildingEffect(player);
        assertEquals(10,player.getFood()); //this confirms the "each set completed add five food"


        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.GATHERER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.BUILDER));
        buildingCard.applyBuildingEffect(player);
        assertEquals(10,player.getFood());
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.BUILDER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.HUNTER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        buildingCard.applyBuildingEffect(player);
        assertEquals(10,player.getFood());
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.SHAMAN));
        buildingCard.applyBuildingEffect(player);
        assertEquals(15,player.getFood());


        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.GATHERER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.BUILDER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.SHAMAN));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.HUNTER));

        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.GATHERER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.BUILDER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.SHAMAN));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.HUNTER));

        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.GATHERER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.BUILDER));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.SHAMAN));
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.HUNTER));
        buildingCard.applyBuildingEffect(player);
        assertEquals(15,player.getFood());
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        buildingCard.applyBuildingEffect(player);
        assertEquals(20,player.getFood());
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        buildingCard.applyBuildingEffect(player);
        assertEquals(25,player.getFood());
        player.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));

    }

}