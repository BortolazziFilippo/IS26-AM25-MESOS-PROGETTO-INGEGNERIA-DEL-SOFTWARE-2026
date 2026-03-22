package it.polimi.ingsw.am25.Model.Player;

import it.polimi.ingsw.am25.Model.Card.*;
import it.polimi.ingsw.am25.Model.Enums.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void manageFood() {
        Player player = new Player("Lorem Ipsum", COLOR.BLUE);
        //AddingFood
        player.manageFood(+7);
        assertEquals(7,player.getFood());
        //removing food
        player.manageFood(-7);
        assertEquals(0,player.getFood());
        //removing food under 0 should remove double PP
        player.manageFood(-3);
        assertEquals(-6,player.getPrestigePoint());
        assertEquals(0,player.getFood());
    }

    @Test
    void managePP() {
        Player player = new Player("Lorem Ipsum",COLOR.RED);
        player.managePP(+7);
        assertEquals(7,player.getPrestigePoint());
        player.managePP(-14);
        assertEquals(-7,player.getPrestigePoint());
    }

    @Test
    void addCardToTribe() {
        Player player = new Player("Lorem Ipsum",COLOR.RED);
        assertEquals(0, player.getNumberOfCard());
        player.addCardToTribe(new InventorCard(INV_ICON.ARROW, ERA.ERA_II, CARD_TYPE.INVENTOR));
        assertEquals(1,player.getNumberOfCard());
    }

    @Test
    void getBuilderDiscount() {
        Player player = new Player("Lorem Ipsum",COLOR.RED);
        player.addCardToTribe(new BuilderCard(ERA.ERA_II,CARD_TYPE.BUILDER,6,10));
        assertEquals(6,player.getBuilderDiscount());
        player.addCardToTribe(new BuilderCard(ERA.ERA_II,CARD_TYPE.BUILDER,10,10));
        assertEquals(16,player.getBuilderDiscount());
    }

    @Test
    void getGatherDiscount() {
        Player player = new Player("Lorem Ipsum",COLOR.RED);
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        assertEquals(3,player.getGatherDiscount());
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        player.addCardToTribe(new GathererCard(ERA.ERA_II,CARD_TYPE.GATHERER));
        assertEquals(9,player.getGatherDiscount());

    }

    @Test
    void getArtistNumber() {
        Player player = new Player("Lorem Ipsum",COLOR.RED);
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        player.addCardToTribe(new ArtistCard(ERA.ERA_II,CARD_TYPE.ARTIST));
        assertEquals(3,player.getArtistNumber());
    }
    @Test
    void  getShamanStar(){
        Player player = new Player("Lorem Ipsum",COLOR.RED);
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE));
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.ONE));
        player.addCardToTribe(new ShamanCard(ERA.ERA_II,CARD_TYPE.SHAMAN, SHAMAN_STAR.TWO));
        assertEquals(6,player.getShamanStarTotal());
    }
}