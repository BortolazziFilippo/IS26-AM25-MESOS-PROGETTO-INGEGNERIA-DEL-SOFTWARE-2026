package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Card.ArtistCard;
import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.EventCard;
import it.polimi.ingsw.am25.Model.Effect.Building.OnEventHuntOneFoodAndOnePPPerHunter;
import it.polimi.ingsw.am25.Model.Effect.Building.OnEventPaintingsOneFoodPerArtist;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtistEventTest {

    @Test
    void solveEvent() {

        //testo se la logica funziona, se ha piu di artist needed allora guadagna PPtoMultiply per ogni artista, altrimenti perde due punti PP
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
        eventCard.setEventEffect(new ArtistEvent(3,2,3));
        eventCard.applyEventEffect(playerList);

        assertEquals(-2,player1.getPrestigePoint());
        assertEquals(-2,player2.getPrestigePoint());
        assertEquals(-2,player3.getPrestigePoint());
        assertEquals(9,player4.getPrestigePoint());

    }

    @Test
    void playerWithBuildingShouldGetFood(){
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
        eventCard.setEventEffect(new ArtistEvent(3,2,3));

        eventCard.applyEventEffect(playerList);

        assertEquals(-2,player1.getPrestigePoint());
        assertEquals(-2,player2.getPrestigePoint());
        assertEquals(-2,player3.getPrestigePoint());
        assertEquals(2,player3.getFood()); //Checking if it has applied the building effect
        assertNotEquals(2,player4.getFood()); //checking other pl PP are not changed
        assertEquals(9,player4.getPrestigePoint());
    }
}