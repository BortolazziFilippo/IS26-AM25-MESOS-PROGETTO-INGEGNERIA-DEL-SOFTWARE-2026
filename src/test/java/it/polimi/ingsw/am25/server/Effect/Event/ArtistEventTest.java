package it.polimi.ingsw.am25.server.Effect.Event;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Effect.Building.OnEventPaintingsOneFoodPerArtist;
import it.polimi.ingsw.am25.server.model.Effect.Event.ArtistEvent;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtistEventTest {
    @Test
    void solveEvent_insufficientArtists_deductsPP() {

        //tests the logic: if the player has more artists than required, they earn PPtoMultiply per artist; otherwise they lose two PP
        List<Player> playerList= new ArrayList<>();
        Player player1 = new Player("Lorem Ipsum", COLOR.RED);
        Player player2 = new Player("Lorem Ipsum", COLOR.RED);
        player2.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        Player player3 = new Player("Lorem Ipsum", COLOR.RED);
        player3.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        player3.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        Player player4 = new Player("Lorem Ipsum", COLOR.RED);
        player4.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        player4.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        player4.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));

        playerList.add(player1);
        playerList.add(player2);
        playerList.add(player3);
        playerList.add(player4);
        EventCard eventCard= new EventCard(ERA.ERA_III,CARD_TYPE.EVENT,10, EVENT_TYPE.PAINTINGS);
        eventCard.setEventEffect(new ArtistEvent(3,-2,3));
        eventCard.applyEventEffect(playerList);

        assertEquals(-2,player1.getPrestigePoint());
        assertEquals(-2,player2.getPrestigePoint());
        assertEquals(-2,player3.getPrestigePoint());
        assertEquals(9,player4.getPrestigePoint());

    }

    @Test
    void solveEvent_withBuildingBonus_awardsFoodBonus(){
        List<Player> playerList= new ArrayList<>();
        Player player1 = new Player("Lorem Ipsum", COLOR.RED);
        Player player2 = new Player("Lorem Ipsum", COLOR.RED);
        player2.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        Player player3 = new Player("Lorem Ipsum", COLOR.RED);
        player3.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        player3.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        Player player4 = new Player("Lorem Ipsum", COLOR.RED);
        player4.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        player4.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));
        player4.addCardToTribe(new ArtistCard(ERA.ERA_III, CARD_TYPE.ARTIST));

        playerList.add(player1);
        playerList.add(player2);
        playerList.add(player3);
        playerList.add(player4);

        BuildingCard buildingCard = new BuildingCard(ERA.ERA_III,CARD_TYPE.BUILDING,1,10,10,EVENT_TYPE.PAINTINGS);
        buildingCard.setBuildingEffect(new OnEventPaintingsOneFoodPerArtist());
        player3.addBuilding(buildingCard);

        EventCard eventCard= new EventCard(ERA.ERA_III,CARD_TYPE.EVENT,10, EVENT_TYPE.PAINTINGS);
        eventCard.setEventEffect(new ArtistEvent(3,-2,3));

        eventCard.applyEventEffect(playerList);

        assertEquals(-2,player1.getPrestigePoint());
        assertEquals(-2,player2.getPrestigePoint());
        assertEquals(-2,player3.getPrestigePoint());
        assertEquals(2,player3.getFood()); //Checking if it has applied the building effect
        assertNotEquals(2,player4.getFood()); //checking other pl PP are not changed
        assertEquals(9,player4.getPrestigePoint());
    }
}