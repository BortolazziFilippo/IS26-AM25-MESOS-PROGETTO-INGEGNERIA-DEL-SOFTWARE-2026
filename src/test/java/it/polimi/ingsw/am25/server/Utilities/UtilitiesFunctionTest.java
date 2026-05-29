package it.polimi.ingsw.am25.server.Utilities;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesConstant;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilitiesFunctionTest {

    // ─────────────────────────── shuffledFromYToXExclusive ───────────────────────────

    @Test
    void shuffled_yGreaterThanX_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> UtilitiesFunction.shuffledFromYToXExclusive(5, 3));
    }

    @Test
    void shuffled_equalBounds_returnsEmptyList() {
        List<Integer> result = UtilitiesFunction.shuffledFromYToXExclusive(3, 3);
        assertTrue(result.isEmpty());
    }

    @Test
    void shuffled_validRange_containsExactlyAllNumbers() {
        List<Integer> result = UtilitiesFunction.shuffledFromYToXExclusive(0, 5);
        assertEquals(5, result.size());
        assertTrue(result.containsAll(List.of(0, 1, 2, 3, 4)));
    }

    @Test
    void shuffled_nonZeroLowerBound_containsCorrectNumbers() {
        List<Integer> result = UtilitiesFunction.shuffledFromYToXExclusive(2, 6);
        assertEquals(4, result.size());
        assertTrue(result.containsAll(List.of(2, 3, 4, 5)));
    }

    // ─────────────────────────── bindCorrectNumberOfTopListCard ───────────────────────────

    @Test
    void bindTopCard_allValidPlayerCounts() {
        assertEquals(UtilitiesConstant.TWO_PLAYER_TOP_CARD,   UtilitiesFunction.bindCorrectNumberOfTopListCard(2));
        assertEquals(UtilitiesConstant.THREE_PLAYER_TOP_CARD, UtilitiesFunction.bindCorrectNumberOfTopListCard(3));
        assertEquals(UtilitiesConstant.FOUR_PLAYER_TOP_CARD,  UtilitiesFunction.bindCorrectNumberOfTopListCard(4));
        assertEquals(UtilitiesConstant.FIVE_PLAYER_TOP_CARD,  UtilitiesFunction.bindCorrectNumberOfTopListCard(5));
    }

    @Test
    void bindTopCard_invalidPlayerNumber_returnsMinusOne() {
        assertEquals(-1, UtilitiesFunction.bindCorrectNumberOfTopListCard(1));
        assertEquals(-1, UtilitiesFunction.bindCorrectNumberOfTopListCard(6));
    }

    // ─────────────────────────── bindCorrectNumberOfBottomListCard ───────────────────────────

    @Test
    void bindBottomCard_allValidPlayerCounts() {
        assertEquals(UtilitiesConstant.TWO_PLAYER_BOTTOM_CARD,   UtilitiesFunction.bindCorrectNumberOfBottomListCard(2));
        assertEquals(UtilitiesConstant.THREE_PLAYER_BOTTOM_CARD, UtilitiesFunction.bindCorrectNumberOfBottomListCard(3));
        assertEquals(UtilitiesConstant.FOUR_PLAYER_BOTTOM_CARD,  UtilitiesFunction.bindCorrectNumberOfBottomListCard(4));
        assertEquals(UtilitiesConstant.FIVE_PLAYER_BOTTOM_CARD,  UtilitiesFunction.bindCorrectNumberOfBottomListCard(5));
    }

    @Test
    void bindBottomCard_invalidPlayerNumber_returnsMinusOne() {
        assertEquals(-1, UtilitiesFunction.bindCorrectNumberOfBottomListCard(0));
        assertEquals(-1, UtilitiesFunction.bindCorrectNumberOfBottomListCard(7));
    }

    // ─────────────────────────── stringToIntegerBinder ───────────────────────────

    @Test
    void stringToInt_allValidValues() {
        assertEquals(2, UtilitiesFunction.stringToIntegerBinder("2"));
        assertEquals(3, UtilitiesFunction.stringToIntegerBinder("3"));
        assertEquals(4, UtilitiesFunction.stringToIntegerBinder("4"));
        assertEquals(5, UtilitiesFunction.stringToIntegerBinder("5"));
    }

    @Test
    void stringToInt_invalidValue_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> UtilitiesFunction.stringToIntegerBinder("1"));
        assertThrows(IllegalStateException.class, () -> UtilitiesFunction.stringToIntegerBinder("6"));
        assertThrows(IllegalStateException.class, () -> UtilitiesFunction.stringToIntegerBinder("abc"));
    }

    // ─────────────────────────── getScore ───────────────────────────

    @Test
    void getScore_allValidPlayerCounts() {
        // 2 players: 10, 5
        assertEquals(10, UtilitiesFunction.getScore(2, 1));
        assertEquals(5,  UtilitiesFunction.getScore(2, 2));
        // 3 players: 15, 8, 3
        assertEquals(15, UtilitiesFunction.getScore(3, 1));
        assertEquals(8,  UtilitiesFunction.getScore(3, 2));
        assertEquals(3,  UtilitiesFunction.getScore(3, 3));
        // 4 players: 20, 12, 6, 2
        assertEquals(20, UtilitiesFunction.getScore(4, 1));
        assertEquals(2,  UtilitiesFunction.getScore(4, 4));
        // 5 players: 25, 16, 9, 4, 1
        assertEquals(25, UtilitiesFunction.getScore(5, 1));
        assertEquals(1,  UtilitiesFunction.getScore(5, 5));
    }

    @Test
    void getScore_invalidPlayerCount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> UtilitiesFunction.getScore(1, 1));
        assertThrows(IllegalArgumentException.class, () -> UtilitiesFunction.getScore(6, 1));
    }

    // ─────────────────────────── countOccurrence ───────────────────────────

    @Test
    void countOccurrence_eventCard_hitsDefaultBranchWithoutCounting() {
        // EVENT and BUILDING types are not in the switch → fall to default (logged, not counted)
        EventCard event = new EventCard(ERA.ERA_I, CARD_TYPE.EVENT, 1, EVENT_TYPE.SUSTENANCE);
        List<Card> cards = List.of(event);
        List<Integer> counts = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0));

        assertDoesNotThrow(() -> UtilitiesFunction.countOccurrence(cards, counts));
        assertEquals(0, counts.stream().mapToInt(i -> i).sum());
    }

    @Test
    void countOccurrence_buildingCard_hitsDefaultBranchWithoutCounting() {
        BuildingCard building = new BuildingCard(ERA.ERA_I, CARD_TYPE.BUILDING, 1, 5, 10, EVENT_TYPE.END_ROUND);
        List<Card> cards = List.of(building);
        List<Integer> counts = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0));

        assertDoesNotThrow(() -> UtilitiesFunction.countOccurrence(cards, counts));
        assertEquals(0, counts.stream().mapToInt(i -> i).sum());
    }

    // ─────────────────────────── logInfo / logError con writer attivo ───────────────────────────

    @Test
    void logInfo_whenWriterInitialised_doesNotThrow() {
        UtilitiesFunction.initLog();
        assertDoesNotThrow(() -> UtilitiesFunction.logInfo("[TEST]", "messaggio di test"));
    }

    @Test
    void logError_whenWriterInitialised_doesNotThrow() {
        UtilitiesFunction.initLog();
        assertDoesNotThrow(() -> UtilitiesFunction.logError("[TEST]", "errore di test"));
        assertDoesNotThrow(() -> UtilitiesFunction.logError("errore senza prefisso"));
    }
}
