package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;
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
        builderCard = new BuilderCard(ERA.ERA_I, CARD_TYPE.BUILDER, 2, 5);
    }

    @Test
    void testIsBuilderCorrect(){

        assertEquals(ERA.ERA_I, builderCard.getEra());
        assertEquals(CARD_TYPE.BUILDER, builderCard.getCardType());
        assertEquals(2, builderCard.getFoodDiscount());
        assertEquals(5, builderCard.getFinalPrestigePoint());
    }

    @Test
    void testSetterFinalPP(){
        builderCard.setFinalPrestigePoint(10);
        assertEquals(10, builderCard.getFinalPrestigePoint());
    }

    @Test
    void testAddCardToPlayer(){
        builderCard.addCardToPlayer(player);
        assertEquals(1, player.getTribe().size());
        assertEquals(List.of(builderCard), player.getTribe());
    }

    @Test
    void testBuilderDiscountAppliedToPlayer(){
        builderCard.addCardToPlayer(player);
        assertEquals(2, player.getBuilderDiscount());
    }

    @Test void testMultipleBuildersDisc(){
        builderCard.addCardToPlayer(player);
        new BuilderCard(ERA.ERA_II, CARD_TYPE.BUILDER, 1, 3)
                .addCardToPlayer(player);

        assertEquals(3, player.getBuilderDiscount());
    }
}