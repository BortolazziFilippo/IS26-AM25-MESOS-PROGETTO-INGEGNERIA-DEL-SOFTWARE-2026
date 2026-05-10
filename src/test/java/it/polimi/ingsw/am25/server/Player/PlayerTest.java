package it.polimi.ingsw.am25.server.Player;

import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Effect.Building.PPPerCharType;
import it.polimi.ingsw.am25.server.model.Effect.Building.TwentyFivePPEndGame;
import it.polimi.ingsw.am25.server.model.Enums.*;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotEnoughFoodException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    Player player;
    @BeforeEach
    void createPlayer(){
        this.player = new Player("Lorem Ipsum", COLOR.BLUE);
    }
    @Test
    void manageFoodAndPP() {

        //AddingFood
        player.manageFoodAndPP(+7);
        assertEquals(7,player.getFood());
        //removing food
        player.manageFoodAndPP(-7);
        assertEquals(0,player.getFood());
        //removing food under 0 should remove double PP
        player.manageFoodAndPP(-3);
        assertEquals(-6,player.getPrestigePoint());
        assertEquals(0,player.getFood());
    }

    @Test
    void tryBuyBuilding() throws NotEnoughFoodException {
        player.manageFoodAndPP(5);
        BuildingCard buildingCard= new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING,1,10,10, EVENT_TYPE.END_ROUND);
        assertThrows(NotEnoughFoodException.class,()->player.tryBuyBuilding(buildingCard));
        player.manageFoodAndPP(5);
        player.tryBuyBuilding(buildingCard);
        assertEquals(0,player.getFood());
        assertIterableEquals(List.of(new BuildingCard(ERA.ERA_I,CARD_TYPE.BUILDING,1,10,10,EVENT_TYPE.END_ROUND)), player.getBuildingCards());    }

    @Test
    void managePP() {
        player.managePP(+7);
        assertEquals(7,player.getPrestigePoint());
        player.managePP(-14);
        assertEquals(-7,player.getPrestigePoint());
    }

    @Test
    void addCardToTribe() {
        assertEquals(0, player.getNumberOfCard());
        player.addCardToTribe(new InventorCard( ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.ARROW));
        assertEquals(1,player.getNumberOfCard());
        player.addCardToTribe(new ArtistCard( ERA.ERA_III, CARD_TYPE.ARTIST));
        player.addCardToTribe(new GathererCard( ERA.ERA_I, CARD_TYPE.GATHERER));
        player.addCardToTribe(new BuilderCard( ERA.ERA_II, CARD_TYPE.BUILDER,10,10, 0));

        List<Card> listToCompare= new ArrayList<>();

        listToCompare.add(new InventorCard( ERA.ERA_II, CARD_TYPE.INVENTOR,INV_ICON.ARROW));
        listToCompare.add(new ArtistCard( ERA.ERA_III, CARD_TYPE.ARTIST));
        listToCompare.add(new GathererCard( ERA.ERA_I, CARD_TYPE.GATHERER));
        listToCompare.add(new BuilderCard( ERA.ERA_II, CARD_TYPE.BUILDER,10,10, 0));
        assertIterableEquals(listToCompare,player.getTribe());
    }

    @Test
    void getBuilderDiscount() {
        player.addCardToTribe(new BuilderCard(ERA.ERA_II,CARD_TYPE.BUILDER,6,10, 0));
        assertEquals(6,player.getBuilderDiscount());
        player.addCardToTribe(new BuilderCard(ERA.ERA_II,CARD_TYPE.BUILDER,10,10, 0));
        assertEquals(16,player.getBuilderDiscount());
    }

    @Test
    void getGatherDiscount() {
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        assertEquals(3,player.getGatherDiscount());
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        assertEquals(9,player.getGatherDiscount());
    }

    @Test
    void getArtistNumber() {
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        assertEquals(3,player.getArtistNumber());
    }
    @Test
    void  getShamanStar(){
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE));
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE));
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO));
        assertEquals(6,player.getShamanStarTotal());
    }

    @Test
    void getHunterNumber() {
        assertEquals(0, player.getHunterNumber());
        player.addCardToTribe(new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true));
        player.addCardToTribe(new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false));
        assertEquals(2, player.getHunterNumber());
    }

    @Test
    void getNumberOfDifferentInventorIcon() {
        player.addCardToTribe(new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.BREAD));
        player.addCardToTribe(new InventorCard(ERA.ERA_II, CARD_TYPE.INVENTOR, INV_ICON.BREAD));
        assertEquals(1, player.getNumberOfDifferentInventorIcon());

        player.addCardToTribe(new InventorCard(ERA.ERA_III, CARD_TYPE.INVENTOR, INV_ICON.ARROW));
        assertEquals(2, player.getNumberOfDifferentInventorIcon());
    }

    @Test
    void getNickname() {
        assertEquals("Lorem Ipsum", player.getNickname());
    }

    @Test
    void getTotem() {
        assertEquals(COLOR.BLUE, player.getTotem().color());
    }

    @Test
    void equalsTest() {
        Player same = new Player("Lorem Ipsum", COLOR.BLUE);
        Player different = new Player("Other", COLOR.RED);
        assertEquals(player, same);
        assertNotEquals(player, different);
    }

    @Test
    void connectionStatus() {
        player.setConnection(CONNECTION_STATUS.CONNECTED);
        assertEquals(CONNECTION_STATUS.CONNECTED, player.getConnection());
        player.setConnection(CONNECTION_STATUS.DISCONNECTED);
        assertEquals(CONNECTION_STATUS.DISCONNECTED, player.getConnection());
    }

    @Test
    void tryBuyBuildingWithBuilderDiscount() {
        // discount(15) > cost(10) → clamped to 0, no food spent
        player.addCardToTribe(new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 15, 10, 0));
        BuildingCard building = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 10, 10, EVENT_TYPE.END_ROUND);
        player.manageFoodAndPP(3);
        assertDoesNotThrow(() -> player.tryBuyBuilding(building));
        assertEquals(3, player.getFood());
    }

    @Test
    void triggerEndRoundBuilding() {
        BuildingCard building = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_ROUND);
        building.setBuildingEffect(new PPPerCharType(3, CARD_TYPE.HUNTER));
        player.addBuilding(building);
        player.addCardToTribe(new HuntersCard(ERA.ERA_I, CARD_TYPE.HUNTER, true));
        player.addCardToTribe(new HuntersCard(ERA.ERA_II, CARD_TYPE.HUNTER, false));

        player.triggerEndRoundBuilding();

        assertEquals(6, player.getPrestigePoint());
    }

    @Test
    void triggerEndGameBuilding() {
        BuildingCard building = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 30, EVENT_TYPE.END_GAME);
        building.setBuildingEffect(new TwentyFivePPEndGame());
        player.addBuilding(building);

        player.triggerEndGameBuilding();

        assertEquals(25, player.getPrestigePoint());
    }

    @Test
    void checkpoints() {
        // 4 artists → 2 pairs → 20 PP
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST));
        // 1 builder with finalPrestigePoint=8
        player.addCardToTribe(new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 2, 8, 0));
        // 2 inventors with 2 distinct icons → 2 * 2 = 4 PP
        player.addCardToTribe(new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.BREAD));
        player.addCardToTribe(new InventorCard(ERA.ERA_I, CARD_TYPE.INVENTOR, INV_ICON.ARROW));

        assertEquals(32, player.checkpoints()); // 20 + 8 + 4
    }
}