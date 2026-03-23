package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Card.ShamanCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.Model.Player.Player;
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
    void testMaggioranzaVince(){
        addShamanStars(p1, 3);
        addShamanStars(p2, 1);

        shamanEvent.solveEvent(List.of(p1, p2));

        assertEquals(10, p1.getPrestigePoint());
        assertEquals(-5, p2.getPrestigePoint());
    }

    @Test
    void testParitaMax(){
        addShamanStars(p1, 3);
        addShamanStars(p2, 3);
        addShamanStars(p3, 1);

        shamanEvent.solveEvent(List.of(p1, p2, p3));

        assertEquals(10, p1.getPrestigePoint());
        assertEquals(10, p2.getPrestigePoint());
        assertEquals(-5, p3.getPrestigePoint());
    }

    @Test
    void testParitaMin(){
        addShamanStars(p1, 3);
        addShamanStars(p2, 1);
        addShamanStars(p3, 1);

        shamanEvent.solveEvent(List.of(p1, p2, p3));

        assertEquals(10, p1.getPrestigePoint());
        assertEquals(-5, p2.getPrestigePoint());
        assertEquals(-5, p3.getPrestigePoint());
    }

    @Test
    void testParita(){
        addShamanStars(p1, 2);
        addShamanStars(p2, 2);
        addShamanStars(p3, 2);

        shamanEvent.solveEvent(List.of(p1, p2, p3));
        assertEquals(5, p1.getPrestigePoint());
        assertEquals(5, p2.getPrestigePoint());
        assertEquals(5, p3.getPrestigePoint());
    }
}