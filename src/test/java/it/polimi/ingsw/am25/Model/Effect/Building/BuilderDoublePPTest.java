package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.BuilderCard;
import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuilderDoublePPTest {

    @Test
    //this On endGame should double the amount of PP of the builders
    void applyEffecTest() {
        Player player = new Player("Lorem Ipsum", COLOR.RED);
        player.addCardToTribe(new BuilderCard(ERA.ERA_II, CARD_TYPE.BUILDER,7,2));
        player.addCardToTribe(new BuilderCard(ERA.ERA_II, CARD_TYPE.BUILDER,7,3));
        player.addCardToTribe(new BuilderCard(ERA.ERA_II, CARD_TYPE.BUILDER,7,4));
        BuildingCard buildingCard=new BuildingCard(ERA.ERA_II,CARD_TYPE.BUILDING,1,10,10,EVENT_TYPE.END_GAME);
        buildingCard.setBuildingEffect(new BuilderDoublePP());
        buildingCard.applyEventEffect(player);
        assertEquals(18,player.getTribe().stream().filter(card -> card.getCardType()==CARD_TYPE.BUILDER).map((card->(BuilderCard)card)).mapToInt(BuilderCard::getFinalPrestigePoint).sum());

    }
}