package it.polimi.ingsw.am25.server.Effect.Building;

import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Effect.Building.PPPerCharType;
import it.polimi.ingsw.am25.server.model.Enums.*;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PPPerCharTypeTest {
    Player playerP1, playerP2;

    @BeforeEach
    void setUp() {
        playerP1= new Player("Lucinda", COLOR.BLUE);
        playerP2= new Player("Genoveffa", COLOR.RED);
    }

    @Test
    void testSet1() {
        HuntersCard card1 = new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true);
        playerP1.addCardToTribe(card1);
        BuilderCard card2 = new BuilderCard(ERA.ERA_II, CARD_TYPE.BUILDER, 2, 12);
        playerP1.addCardToTribe(card2);
        HuntersCard card3= new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, false);
        playerP1.addCardToTribe(card3);
        HuntersCard card4 = new HuntersCard(ERA.ERA_III, CARD_TYPE.HUNTER, true);
        playerP1.addCardToTribe(card4);
        InventorCard card5 = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD);
        playerP1.addCardToTribe(card5);
        ShamanCard card6 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        playerP1.addCardToTribe(card6);

        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_ROUND);
        buildingCard.setBuildingEffect(new PPPerCharType(3, CARD_TYPE.HUNTER));
        buildingCard.applyBuildingEffect(playerP1);

        assertEquals(9, playerP1.getPrestigePoint());

    }

    @Test
    void testSet2() {
        ArtistCard card1 = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        playerP1.addCardToTribe(card1);
        BuilderCard card2 = new BuilderCard(ERA.ERA_II, CARD_TYPE.BUILDER, 2, 12);
        playerP1.addCardToTribe(card2);
        GathererCard card3= new GathererCard(ERA.ERA_I, CARD_TYPE.GATHERER);
        playerP1.addCardToTribe(card3);
        HuntersCard card4 = new HuntersCard(ERA.ERA_III, CARD_TYPE.HUNTER, true);
        playerP1.addCardToTribe(card4);
        InventorCard card5 = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD);
        playerP1.addCardToTribe(card5);
        ShamanCard card6 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        playerP1.addCardToTribe(card6);

        ArtistCard card7 = new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST);
        playerP1.addCardToTribe(card7);
        BuilderCard card8 = new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 3, 14);
        playerP1.addCardToTribe(card8);
        GathererCard card9= new GathererCard(ERA.ERA_III, CARD_TYPE.GATHERER);
        playerP1.addCardToTribe(card9);
        HuntersCard card10 = new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false);
        playerP1.addCardToTribe(card10);
        InventorCard card11 = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.STONE);
        playerP1.addCardToTribe(card11);
        ShamanCard card12 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        playerP1.addCardToTribe(card12);

        ArtistCard card13 = new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST);
        playerP1.addCardToTribe(card13);
        GathererCard card14= new GathererCard(ERA.ERA_III, CARD_TYPE.GATHERER);
        playerP1.addCardToTribe(card14);
        ArtistCard card15 = new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST);
        playerP1.addCardToTribe(card15);
        ShamanCard card16 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        playerP1.addCardToTribe(card16);
        ShamanCard card17 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        playerP1.addCardToTribe(card17);
        ShamanCard card18 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        playerP1.addCardToTribe(card18);


        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_ROUND);
        buildingCard.setBuildingEffect(new PPPerCharType(4, CARD_TYPE.BUILDER));
        buildingCard.applyBuildingEffect(playerP1);

        assertEquals(8, playerP1.getPrestigePoint());

    }

    @Test
    void test3(){
        ArtistCard card1 = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        playerP1.addCardToTribe(card1);
        GathererCard card3= new GathererCard(ERA.ERA_I, CARD_TYPE.GATHERER);
        playerP1.addCardToTribe(card3);
        HuntersCard card4 = new HuntersCard(ERA.ERA_III, CARD_TYPE.HUNTER, true);
        playerP1.addCardToTribe(card4);
        InventorCard card5 = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD);
        playerP1.addCardToTribe(card5);
        ShamanCard card6 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO);
        playerP1.addCardToTribe(card6);

        ArtistCard card7 = new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST);
        playerP1.addCardToTribe(card7);
        GathererCard card9= new GathererCard(ERA.ERA_III, CARD_TYPE.GATHERER);
        playerP1.addCardToTribe(card9);
        HuntersCard card10 = new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false);
        playerP1.addCardToTribe(card10);
        InventorCard card11 = new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.STONE);
        playerP1.addCardToTribe(card11);
        ShamanCard card12 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        playerP1.addCardToTribe(card12);

        ArtistCard card13 = new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST);
        playerP1.addCardToTribe(card13);
        GathererCard card14= new GathererCard(ERA.ERA_III, CARD_TYPE.GATHERER);
        playerP1.addCardToTribe(card14);
        ArtistCard card15 = new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST);
        playerP1.addCardToTribe(card15);
        ShamanCard card16 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        playerP1.addCardToTribe(card16);
        ShamanCard card17 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        playerP1.addCardToTribe(card17);
        ShamanCard card18 = new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE);
        playerP1.addCardToTribe(card18);


        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_ROUND);
        buildingCard.setBuildingEffect(new PPPerCharType(4, CARD_TYPE.BUILDER));
        buildingCard.applyBuildingEffect(playerP1);

        assertEquals(0, playerP1.getPrestigePoint());
    }

}