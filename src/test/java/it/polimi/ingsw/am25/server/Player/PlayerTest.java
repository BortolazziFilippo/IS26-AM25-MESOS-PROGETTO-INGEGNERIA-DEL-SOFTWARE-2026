package it.polimi.ingsw.am25.server.Player;

import it.polimi.ingsw.am25.server.model.Card.*;
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
        player.addCardToTribe(new BuilderCard( ERA.ERA_II, CARD_TYPE.BUILDER,10,10));

        List<Card> listToCompare= new ArrayList<>();

        listToCompare.add(new InventorCard( ERA.ERA_II, CARD_TYPE.INVENTOR,INV_ICON.ARROW));
        listToCompare.add(new ArtistCard( ERA.ERA_III, CARD_TYPE.ARTIST));
        listToCompare.add(new GathererCard( ERA.ERA_I, CARD_TYPE.GATHERER));
        listToCompare.add(new BuilderCard( ERA.ERA_II, CARD_TYPE.BUILDER,10,10));
        assertIterableEquals(listToCompare,player.getTribe());
    }

    @Test
    void getBuilderDiscount() {
        player.addCardToTribe(new BuilderCard(ERA.ERA_II,CARD_TYPE.BUILDER,6,10));
        assertEquals(6,player.getBuilderDiscount());
        player.addCardToTribe(new BuilderCard(ERA.ERA_II,CARD_TYPE.BUILDER,10,10));
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
}