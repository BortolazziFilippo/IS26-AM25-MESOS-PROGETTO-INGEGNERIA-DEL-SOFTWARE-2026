package it.polimi.ingsw.am25.server.Effect.Event;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Card.HuntersCard;
import it.polimi.ingsw.am25.server.model.Effect.Building.OnEventHuntOneFoodAndOnePPPerHunter;
import it.polimi.ingsw.am25.server.model.Effect.Event.HuntEvent;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HuntEventTest {

    @Test
    void solveEvent() {
        List<Player> playerList=new ArrayList<>();
        Player player1=new Player("Lorem Ipsum", COLOR.RED);
        Player player2=new Player("Lorem Ipsum", COLOR.RED);
        Player player3=new Player("Lorem Ipsum", COLOR.RED);
        EventCard eventCard= new EventCard(ERA.ERA_III, CARD_TYPE.EVENT,1, EVENT_TYPE.HUNT);
        eventCard.setEventEffect(new HuntEvent(1,3));

        player1.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));

        player2.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));
        player2.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));

        player3.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));
        player3.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));
        player3.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));

        playerList.add(player1);
        playerList.add(player2);
        playerList.add(player3);
        eventCard.applyEventEffect(playerList);

        assertEquals(1,player1.getFood());
        assertEquals(3,player1.getPrestigePoint());

        assertEquals(1,player2.getFood());
        assertEquals(6,player2.getPrestigePoint());

        assertEquals(1,player3.getFood());
        assertEquals(9,player3.getPrestigePoint());



    }

    @Test
    void playerWithBuildingShouldGetMorePP(){
        List<Player> playerList=new ArrayList<>();
        Player player1=new Player("Lorem Ipsum", COLOR.RED);
        Player player2=new Player("Lorem Ipsum", COLOR.RED);
        Player player3=new Player("Lorem Ipsum", COLOR.RED);
        EventCard eventCard= new EventCard(ERA.ERA_III, CARD_TYPE.EVENT,1, EVENT_TYPE.HUNT);
        eventCard.setEventEffect(new HuntEvent(1,3));

        player1.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));

        player2.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));
        player2.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));

        player3.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));
        player3.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));
        player3.addCardToTribe(new HuntersCard(ERA.ERA_III,CARD_TYPE.HUNTER,false));

        BuildingCard buildingCard= new BuildingCard(ERA.ERA_III,CARD_TYPE.BUILDING,1,10,1,EVENT_TYPE.HUNT);
        buildingCard.setBuildingEffect(new OnEventHuntOneFoodAndOnePPPerHunter());
        player3.addBuilding(buildingCard);

        playerList.add(player1);
        playerList.add(player2);
        playerList.add(player3);
        eventCard.applyEventEffect(playerList);

        assertEquals(1,player1.getFood());
        assertEquals(3,player1.getPrestigePoint());

        assertEquals(1,player2.getFood());
        assertEquals(6,player2.getPrestigePoint());
        //since player 3 has a building which applies on HUNT event it gets one more Food and one more PP per hunter
        assertEquals(2,player3.getFood());
        assertEquals(12,player3.getPrestigePoint());
    }
}