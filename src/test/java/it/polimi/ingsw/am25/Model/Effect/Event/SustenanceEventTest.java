package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Card.ArtistCard;
import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.EventCard;
import it.polimi.ingsw.am25.Model.Effect.Building.DiscountFoodOnSustenance;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SustenanceEventTest {
    private Player p1;
    private Player p2;
    private Player p3;
    private EventCard eventCard;


    private List<Player> players=new ArrayList<>();
    @BeforeEach
    void createPlayer(){
        this.p1= new Player("Lorem Ipsum", COLOR.RED);
        this.p2= new Player("Lorem Ipsum", COLOR.RED);
        this.p3= new Player("Lorem Ipsum", COLOR.RED);
        p1.manageFoodAndPP(10);
        p2.manageFoodAndPP(5);
        p3.manageFoodAndPP(0);
        players.add(p1);
        players.add(p2);
        players.add(p3);
        eventCard=new EventCard(ERA.ERA_III, CARD_TYPE.EVENT,1, EVENT_TYPE.SUSTENANCE);
        eventCard.setEventEffect(new SustenanceEvent(1,2));
        p1.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        p1.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));

        p2.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.GATHERER));
        p2.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        p2.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        p2.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));

        p3.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        p3.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        p3.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));


    }

    @Test
    void solveEvent() {
        eventCard.applyEventEffect(players);
        assertEquals(8,p1.getFood());//p1 should pay 2 food
        assertEquals(4,p2.getFood()); //p2 food amount should pay only one food since it has one gatherer (-3 food discount)
        assertEquals(-6,p3.getPrestigePoint()); //p3 since doesn't have food should lose 2 pp per villager
        p2.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.GATHERER));
        eventCard.applyEventEffect(players);
        assertEquals(4,p2.getFood()); //p2 food amount shouldn't change
        p3.manageFoodAndPP(2);
        p3.managePP(12);
        eventCard.applyEventEffect(players);
        assertEquals(-2,p3.getPrestigePoint());
        assertEquals(0,p3.getFood());

    }

    @Test
    void buildingShouldAlsoGiveDiscount(){
        BuildingCard buildingCard= new BuildingCard(ERA.ERA_III,CARD_TYPE.BUILDING,1,10,10,EVENT_TYPE.SUSTENANCE);
        buildingCard.setBuildingEffect(new DiscountFoodOnSustenance(CARD_TYPE.ARTIST));
        p1.addBuilding(buildingCard);
        eventCard.applyEventEffect(players);
        assertEquals(10,p1.getFood());//since player has a building, should not lose food
        Player p4= new Player("Lorem Ipsum",COLOR.RED);
        p4.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.ARTIST));
        p4.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.GATHERER));
        p4.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        p4.addBuilding(buildingCard);
        players.add(p4);
        p4.manageFoodAndPP(10);
        eventCard.applyEventEffect(players);
        assertEquals(10,p4.getFood());
        p4.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        p4.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        p4.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        p4.addCardToTribe(new ArtistCard(ERA.ERA_III,CARD_TYPE.INVENTOR));
        eventCard.applyEventEffect(players);
        assertEquals(7,p4.getFood());

    }
}