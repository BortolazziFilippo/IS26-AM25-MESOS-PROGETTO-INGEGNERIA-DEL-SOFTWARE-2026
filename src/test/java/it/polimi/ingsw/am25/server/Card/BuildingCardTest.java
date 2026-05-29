package it.polimi.ingsw.am25.server.Card;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Effect.Building.TwentyFivePPEndGame;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingCardTest {

    private Player player;
    private BuildingCard buildingCard;

    @BeforeEach
    void setup(){
        player = new Player("Mario", COLOR.BLUE);
        buildingCard = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 4, 3, EVENT_TYPE.END_GAME);
        buildingCard.setBuildingEffect(new TwentyFivePPEndGame());
    }

    @Test
    void buildingCard_getters_returnConstructorValues(){
        assertEquals(ERA.ERA_I, buildingCard.getEra());
        assertEquals(CARD_TYPE.BUILDING, buildingCard.getCardType());
        assertEquals(1, buildingCard.getBuildingID());
        assertEquals(4, buildingCard.getFoodCost());
        assertEquals(3, buildingCard.getEndgamePP());
        assertEquals(EVENT_TYPE.END_GAME, buildingCard.getApplyOn());
    }

    @Test
    void addCard_buildingCard_addsToPlayerBuildingList() {
        buildingCard.addCardToPlayer(player);
        assertEquals(List.of(buildingCard), player.getBuildingCards());
    }

    @Test
    void applyEffect_buildingCard_awardsPrestigePoints() {
        buildingCard.addCardToPlayer(player);
        buildingCard.applyBuildingEffect(player);
        assertEquals(25, player.getPrestigePoint());
    }

    @Test
    void equals_sameFieldsAreEqual_differentFieldsOrTypeAreNotEqual() {
        BuildingCard card1 = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 4, 3, EVENT_TYPE.END_GAME);

        // same buildingID → equal
        BuildingCard card2 = new BuildingCard(ERA.ERA_II, CARD_TYPE.BUILDING, 1, 0, 0, EVENT_TYPE.HUNT);
        assertEquals(card1, card2);

        // different buildingID → not equal
        BuildingCard card3 = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 2, 4, 3, EVENT_TYPE.END_GAME);
        assertNotEquals(card1, card3);

        // different type → not equal
        ArtistCard artist = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertNotEquals(card1, artist);

        // null → not equal
        assertNotEquals(null, card1);
    }
}