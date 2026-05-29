package it.polimi.ingsw.am25.server.Card;

import it.polimi.ingsw.am25.server.model.Card.ArtistCard;
import it.polimi.ingsw.am25.server.model.Card.BuilderCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuilderCardTest {

    private Player player;
    private BuilderCard builderCard;

    @BeforeEach
    void setup(){
        player = new Player("Luigi", COLOR.RED);
        builderCard = new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 2, 5, 0);
    }

    @Test
    void builderCard_getters_returnConstructorValues(){

        assertEquals(ERA.ERA_I, builderCard.getEra());
        assertEquals(CARD_TYPE.BUILDER, builderCard.getCardType());
        assertEquals(2, builderCard.getFoodDiscount());
        assertEquals(5, builderCard.getFinalPrestigePoint());
    }

    @Test
    void setFinalPrestigePoint_newValue_updatesCorrectly(){
        builderCard.setFinalPrestigePoint(10);
        assertEquals(10, builderCard.getFinalPrestigePoint());
    }

    @Test
    void addCard_builderCard_addsToTribe(){
        builderCard.addCardToPlayer(player);
        assertEquals(1, player.getTribe().size());
        assertEquals(List.of(builderCard), player.getTribe());
    }

    @Test
    void addCard_builderCard_appliesDiscountToPlayer(){
        builderCard.addCardToPlayer(player);
        assertEquals(2, player.getBuilderDiscount());
    }

    @Test void addCard_multipleBuilders_sumsDiscounts(){
        builderCard.addCardToPlayer(player);
        new BuilderCard(ERA.ERA_II, CARD_TYPE.BUILDER, 1, 3, 0)
                .addCardToPlayer(player);

        assertEquals(3, player.getBuilderDiscount());
    }

    @Test
    void equals_sameFieldsAreEqual_differentFieldsOrTypeAreNotEqual() {
        BuilderCard card1 = new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 2, 5, 0);

        // same fields → equal
        BuilderCard card2 = new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 2, 5, 0);
        assertEquals(card1, card2);

        // different era → not equal
        BuilderCard card3 = new BuilderCard(ERA.ERA_II, CARD_TYPE.BUILDER, 2, 5, 0);
        assertNotEquals(card1, card3);

        // different foodDiscount → not equal
        BuilderCard card4 = new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 1, 5, 0);
        assertNotEquals(card1, card4);

        // different finalPrestigePoint → not equal
        BuilderCard card5 = new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 2, 10, 0);
        assertNotEquals(card1, card5);

        // different type → not equal
        ArtistCard artist = new ArtistCard(ERA.ERA_I, CARD_TYPE.ARTIST);
        assertNotEquals(card1, artist);

        // null → not equal
        assertNotEquals(null, card1);
    }
}