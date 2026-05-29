package it.polimi.ingsw.am25.server.Effect.Event;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.ShamanCard;
import it.polimi.ingsw.am25.server.model.Effect.Building.DoublePPOnShamanEvent;
import it.polimi.ingsw.am25.server.model.Effect.Building.NoPPLostOnShaman;
import it.polimi.ingsw.am25.server.model.Effect.Building.ThreeMoreShamanStar;
import it.polimi.ingsw.am25.server.model.Effect.Event.ShamanEvent;
import it.polimi.ingsw.am25.server.model.Enums.*;
import it.polimi.ingsw.am25.server.model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShamanEventTest {
    private Player p1, p2, p3;
    private ShamanEvent shamanEvent;

    @BeforeEach
    void solveEvent() {
        p1 = new Player("Fabrizio Corona", COLOR.RED);
        p2 = new Player("J.E.", COLOR.BLUE);
        p3 = new Player("A.H.", COLOR.YELLOW);
        shamanEvent = new ShamanEvent(10, 5);
    }

    private void addShamanStars(Player player, int stars){
        SHAMAN_STAR star = switch (stars){
            case 1 -> SHAMAN_STAR.ONE;
            case 2 -> SHAMAN_STAR.TWO;
            case 3 -> SHAMAN_STAR.THREE;
            default -> throw new IllegalArgumentException("Stelle non valide: " + stars);
        };
        player.addCardToTribe(new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, star));
    }

    @Test
    void solveEvent_majorityWins_awardsPrestigeAndDeductsPP(){
        addShamanStars(p1, 3);
        addShamanStars(p2, 1);

        shamanEvent.solveEvent(List.of(p1, p2));

        assertEquals(10, p1.getPrestigePoint());
        assertEquals(-5, p2.getPrestigePoint());
    }

    @Test
    void solveEvent_tieAllHighestStars_bothWin(){
        addShamanStars(p1, 3);
        addShamanStars(p2, 3);
        addShamanStars(p3, 1);

        shamanEvent.solveEvent(List.of(p1, p2, p3));

        assertEquals(10, p1.getPrestigePoint());
        assertEquals(10, p2.getPrestigePoint());
        assertEquals(-5, p3.getPrestigePoint());
    }

    @Test
    void solveEvent_tieAllLowestStars_bothLose(){
        addShamanStars(p1, 3);
        addShamanStars(p2, 1);
        addShamanStars(p3, 1);

        shamanEvent.solveEvent(List.of(p1, p2, p3));

        assertEquals(10, p1.getPrestigePoint());
        assertEquals(-5, p2.getPrestigePoint());
        assertEquals(-5, p3.getPrestigePoint());
    }

    @Test
    void solveEvent_perfectTieAllEqual_allWinSamePP(){
        addShamanStars(p1, 2);
        addShamanStars(p2, 2);
        addShamanStars(p3, 2);

        shamanEvent.solveEvent(List.of(p1, p2, p3));
        assertEquals(5, p1.getPrestigePoint());
        assertEquals(5, p2.getPrestigePoint());
        assertEquals(5, p3.getPrestigePoint());
    }

    @Test
    void solveEvent_withBuildingBonus_increasesEffectiveStarCount(){
        addShamanStars(p1, 1);
        addShamanStars(p2, 3);

        BuildingCard building = new BuildingCard(
                ERA.ERA_I, CARD_TYPE.BUILDING, 4,0, 0, EVENT_TYPE.SHAMANIC_RIT);
        building.setBuildingEffect(new ThreeMoreShamanStar());
        p1.addBuilding(building);
        shamanEvent.solveEvent(List.of(p1, p2));
        assertEquals(10, p1.getPrestigePoint());
        assertEquals(-5, p2.getPrestigePoint());
    }

    @Test
    void solveEvent_withDoublePPBuilding_doublesPrestigePoints(){
        addShamanStars(p1, 3);
        addShamanStars(p2, 1);
        p1.managePP(-20);
        BuildingCard building = new BuildingCard(
                ERA.ERA_I, CARD_TYPE.BUILDING, 4,0, 0, EVENT_TYPE.SHAMANIC_RIT);
        building.setBuildingEffect(new DoublePPOnShamanEvent());
        p1.addBuilding(building);
        shamanEvent.solveEvent(List.of(p1, p2));
        assertEquals(0, p1.getPrestigePoint());
        assertEquals(-5, p2.getPrestigePoint());
    }

    @Test
    void solveEvent_withNoPPLossBuilding_preventsPrestigeLoss(){
        addShamanStars(p1, 1);
        addShamanStars(p2, 3);
        p1.managePP(5);
        BuildingCard building = new BuildingCard(
                ERA.ERA_I, CARD_TYPE.BUILDING, 4,0, 0, EVENT_TYPE.SHAMANIC_RIT);
        building.setBuildingEffect(new NoPPLostOnShaman());
        p1.addBuilding(building);
        shamanEvent.solveEvent(List.of(p1, p2));
        assertEquals(5, p1.getPrestigePoint());
        assertEquals(10, p2.getPrestigePoint());
    }
}