package it.polimi.ingsw.am25.server.Controller;

import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Controller.Controller;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Game.Game;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotSelectableCardException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.TileOccupiedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    private Player host;
    private Player player2;
    private Player player3;
    private Controller controller;

    @BeforeEach
    void setUp() {
        host = new Player("Primo", COLOR.RED);
        player2 = new Player("Secondo", COLOR.BLUE);
        player3 = new Player("Terzo", COLOR.GREEN);
        controller = new Controller();
        controller.createGame(host,3);
    }

    /** Reflectively extracts the private {@code game} field from the controller. */
    private Game getGame(Controller c) throws Exception {
        Field f = Controller.class.getDeclaredField("game");
        f.setAccessible(true);
        return (Game) f.get(c);
    }

    /**
     * Places all three players on offer tiles (indices 1, 2, 3) so the game advances
     * to RESOLVE_ACTION and returns the resulting Game instance.
     */
    private Game advanceToResolveAction() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3); // triggers gameStart → PLACING_PHASE

        Game game = getGame(controller);
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);
        controller.placingPlayer(game.getPlayerToPlace(), 3);
        return game;
    }

    // ─────────────────────────── addPlayer ───────────────────────────

    @Test
    void addingPlayersDuringSetupTransitionsToPlacingPhase() throws Exception {
        controller.addPlayer(player2);
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());

        // adding the last player triggers gameStart → PLACING_PHASE
        controller.addPlayer(player3);
        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
    }

    @Test
    void addingPlayerAfterGameStartedShouldBeIgnored() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3); // triggers gameStart

        Game game = getGame(controller);
        Player extraPlayer = new Player("Extra", COLOR.YELLOW);
        int sizeBefore = game.getPlayerList().size();
        controller.addPlayer(extraPlayer);
        assertEquals(sizeBefore, game.getPlayerList().size());
    }

    // ─────────────────────────── placingPlayer ───────────────────────────

    @Test
    void placingPlayerInPlacingPhase() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3); // game started, now PLACING_PHASE

        Game game = getGame(controller);
        Player playerToPlace = game.getPlayerToPlace();

        assertDoesNotThrow(() -> controller.placingPlayer(playerToPlace, 1));
    }

    @Test
    void placingWrongPlayerInPlacingPhaseShouldBeIgnored() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        Player playerToPlace = game.getPlayerToPlace();
        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(playerToPlace))
                .findFirst()
                .orElseThrow();

        GAME_PHASE phaseBefore = game.getGamePhase();
        assertDoesNotThrow(() -> controller.placingPlayer(wrongPlayer, 0));
        assertEquals(phaseBefore, game.getGamePhase());
    }

    @Test
    void placingPlayerOutOfBoundsThrowsIndexOutOfBoundsException() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        Player playerToPlace = game.getPlayerToPlace();
        assertThrows(IndexOutOfBoundsException.class,
                () -> controller.placingPlayer(playerToPlace, 999));
    }

    @Test
    void placingPlayerOnOccupiedTileThrowsTileOccupiedException() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        Player first = game.getPlayerToPlace();
        controller.placingPlayer(first, 0);

        Player second = game.getPlayerToPlace();
        assertThrows(TileOccupiedException.class,
                () -> controller.placingPlayer(second, 0));
    }

    @Test
    void placingPlayerInWrongPhaseIsIgnored() throws Exception {
        // still in SETUP, no placing should happen
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertDoesNotThrow(() -> controller.placingPlayer(host, 0));
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
    }

    @Test
    void fullPlacingPhaseTransitionsToResolveAction() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);

        controller.placingPlayer(game.getPlayerToPlace(), 0);
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
    }

    // ─────────────────────────── selectCardFromTopList ───────────────────────────

    @Test
    void selectCardFromTopListInWrongPhaseIsIgnored() throws Exception {
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertDoesNotThrow(() ->
                controller.selectCardFromTopList(host, CARD_TYPE.ARTIST, 0));
    }

    @Test
    void selectCardFromTopListWrongPlayerIsIgnored() throws Exception {
        Game game = advanceToResolveAction();

        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(game.getPlayerToPlay()))
                .findFirst()
                .orElseThrow();

        int tribeBefore = wrongPlayer.getTribe().size();
        assertDoesNotThrow(() ->
                controller.selectCardFromTopList(wrongPlayer, CARD_TYPE.ARTIST, 0));
        assertEquals(tribeBefore, wrongPlayer.getTribe().size());
    }

    @Test
    void selectCardFromTopListWhenTileHasNoTopActionIsIgnored() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        // Place players so that the first to play sits on tile index 0 (tile 'B': drawTop=0, drawBot=1).
        Player p1 = game.getPlayerToPlace();
        controller.placingPlayer(p1, 0); // tile B: drawTop=0
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player playerToPlay = game.getPlayerToPlay();
        // tile B has drawTop=0, so top-list selection must be silently ignored
        assertEquals(0, game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop());

        int tribeBefore = playerToPlay.getTribe().size();
        assertDoesNotThrow(() ->
                controller.selectCardFromTopList(playerToPlay, CARD_TYPE.ARTIST, 0));
        assertEquals(tribeBefore, playerToPlay.getTribe().size());
    }

    @Test
    void selectCardFromTopListOutOfBoundsThrowsIndexOutOfBoundsException() throws Exception {
        Game game = advanceToResolveAction();
        Player playerToPlay = game.getPlayerToPlay();

        // Only attempt the assertion when the current tile actually allows top draws
        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop() > 0) {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> controller.selectCardFromTopList(playerToPlay, CARD_TYPE.ARTIST, 9999));
        }
    }

    @Test
    void selectCardFromTopListThrowsNotSelectableCardExceptionForEventType() throws Exception {
        Game game = advanceToResolveAction();
        Player playerToPlay = game.getPlayerToPlay();

        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop() > 0) {
            // Find the index of a non-event card to use — we pass CARD_TYPE.EVENT as the
            // requested type so Game throws NotSelectableCardException regardless of position.
            assertThrows(NotSelectableCardException.class,
                    () -> controller.selectCardFromTopList(playerToPlay, CARD_TYPE.EVENT, 0));
        }
    }

    @Test
    void selectCardFromTopListWithRightPlayerAddsCardToTribe() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);

        // Place so the first player ends up on a tile with at least one top-draw action.
        // Tile index 2 = 'D' (drawTop=0, drawBot=2) in 3-player config
        // Tile index 3 = 'E' (drawTop=1, drawBot=1) — first placed → plays first
        controller.placingPlayer(game.getPlayerToPlace(), 3); // tile E
        controller.placingPlayer(game.getPlayerToPlace(), 4); // tile F
        controller.placingPlayer(game.getPlayerToPlace(), 1); // tile C

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player playerToPlay = game.getPlayerToPlay();

        int tribeSizeBefore = playerToPlay.getTribe().size();
        int topListSizeBefore = game.getMarket().getTopCardList().stream()
                .filter(c -> c.getCardType() != CARD_TYPE.EVENT).toList().size();

        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop() > 0
                && topListSizeBefore > 0) {
            // Find a non-event card index to pick
            int idx = 0;
            List<Card> topList = game.getMarket().getTopCardList();
            for (int i = 0; i < topList.size(); i++) {
                if (topList.get(i).getCardType() != CARD_TYPE.EVENT) {
                    idx = i;
                    break;
                }
            }
            final int finalIdx = idx;
            assertDoesNotThrow(() ->
                    controller.selectCardFromTopList(playerToPlay, CARD_TYPE.ARTIST, finalIdx));
            assertEquals(tribeSizeBefore + 1, playerToPlay.getTribe().size());
        }
    }

    // ─────────────────────────── selectCardFromBottomList ───────────────────────────

    @Test
    void selectCardFromBottomListInWrongPhaseIsIgnored() throws Exception {
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertDoesNotThrow(() ->
                controller.selectCardFromBottomList(host, CARD_TYPE.ARTIST, 0));
    }

    @Test
    void selectCardFromBottomListWrongPlayerIsIgnored() throws Exception {
        Game game = advanceToResolveAction();

        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(game.getPlayerToPlay()))
                .findFirst()
                .orElseThrow();

        int tribeBefore = wrongPlayer.getTribe().size();
        assertDoesNotThrow(() ->
                controller.selectCardFromBottomList(wrongPlayer, CARD_TYPE.ARTIST, 0));
        assertEquals(tribeBefore, wrongPlayer.getTribe().size());
    }

    @Test
    void selectCardFromBottomListWhenTileHasNoBottomActionIsIgnored() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        // Tile index 1 = 'C': drawTop=1, drawBot=0.
        // Place p1 on tile 1 and the others on higher indices (2, 3) so that tile 1
        // is the lowest occupied index and p1 is therefore first to play.
        Player p1 = game.getPlayerToPlace();
        controller.placingPlayer(p1, 1); // tile C: drawBot=0
        controller.placingPlayer(game.getPlayerToPlace(), 2);
        controller.placingPlayer(game.getPlayerToPlace(), 3);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player playerToPlay = game.getPlayerToPlay();
        assertEquals(0, game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom());

        int tribeBefore = playerToPlay.getTribe().size();
        assertDoesNotThrow(() ->
                controller.selectCardFromBottomList(playerToPlay, CARD_TYPE.ARTIST, 0));
        assertEquals(tribeBefore, playerToPlay.getTribe().size());
    }

    @Test
    void selectCardFromBottomListOutOfBoundsThrowsIndexOutOfBoundsException() throws Exception {
        Game game = advanceToResolveAction();
        Player playerToPlay = game.getPlayerToPlay();

        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom() > 0) {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> controller.selectCardFromBottomList(playerToPlay, CARD_TYPE.ARTIST, 9999));
        }
    }

    @Test
    void selectCardFromBottomListThrowsNotSelectableCardExceptionForEventType() throws Exception {
        Game game = advanceToResolveAction();
        Player playerToPlay = game.getPlayerToPlay();

        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom() > 0) {
            assertThrows(NotSelectableCardException.class,
                    () -> controller.selectCardFromBottomList(playerToPlay, CARD_TYPE.EVENT, 0));
        }
    }

    @Test
    void selectCardFromBottomListWithRightPlayerAddsCardToTribe() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        // Tile index 0 = 'B': drawTop=0, drawBot=1 → first placed = first to play
        controller.placingPlayer(game.getPlayerToPlace(), 0); // tile B
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player playerToPlay = game.getPlayerToPlay();

        List<Card> bottomList = game.getMarket().getBottomCardList();
        long nonEventCount = bottomList.stream()
                .filter(c -> c.getCardType() != CARD_TYPE.EVENT).count();

        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom() > 0
                && nonEventCount > 0) {
            int idx = 0;
            for (int i = 0; i < bottomList.size(); i++) {
                if (bottomList.get(i).getCardType() != CARD_TYPE.EVENT) {
                    idx = i;
                    break;
                }
            }
            final int finalIdx = idx;
            int tribeBefore = playerToPlay.getTribe().size();
            assertDoesNotThrow(() ->
                    controller.selectCardFromBottomList(playerToPlay, CARD_TYPE.ARTIST, finalIdx));
            assertEquals(tribeBefore + 1, playerToPlay.getTribe().size());
        }
    }

    // ─────────────────────────── playerDoNothing ───────────────────────────

    @Test
    void playerDoNothingInWrongPhaseIsIgnored() throws Exception {
        // SETUP phase — call must not throw and must not change state
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertDoesNotThrow(() -> controller.playerDoNothing(host));
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
    }

    @Test
    void playerDoNothingWrongPlayerIsIgnored() throws Exception {
        Game game = advanceToResolveAction();

        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(game.getPlayerToPlay()))
                .findFirst()
                .orElseThrow();

        Player currentBefore = game.getPlayerToPlay();
        assertDoesNotThrow(() -> controller.playerDoNothing(wrongPlayer));
        assertEquals(currentBefore, game.getPlayerToPlay());
    }

    @Test
    void playerDoNothingThrowsWhenPlayerCanStillPlay() throws Exception {
        Game game = advanceToResolveAction();
        Player playerToPlay = game.getPlayerToPlay();

        // The market is freshly initialised and contains selectable cards,
        // so canCurrentPlayingPlayerDoSomething() must return true.
        assertTrue(game.canCurrentPlayingPlayerDoSomething());
        assertThrows(Exception.class, () -> controller.playerDoNothing(playerToPlay));
    }

    @Test
    void playerDoNothingAdvancesTurnWhenMarketIsEmpty() throws Exception {
        Game game = advanceToResolveAction();
        Player firstToPlay = game.getPlayerToPlay();

        // Drain both card lists so canCurrentPlayingPlayerDoSomething() returns false.
        game.getMarket().getTopCardList().clear();
        game.getMarket().getBottomCardList().clear();

        assertFalse(game.canCurrentPlayingPlayerDoSomething());
        assertDoesNotThrow(() -> controller.playerDoNothing(firstToPlay));
        // Turn must have advanced to a different player (or round ended)
        assertNotEquals(firstToPlay, game.getPlayerToPlay());
    }

    @Test
    void playerDoNothingLastPlayerEndsRound() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        // Place all three players and advance to RESOLVE_ACTION
        controller.placingPlayer(game.getPlayerToPlace(), 0);
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);
        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());

        // Drain market so no player can act
        game.getMarket().getTopCardList().clear();
        game.getMarket().getBottomCardList().clear();

        // Skip every player's turn — each reference must be effectively final for the lambda
        Player p1 = game.getPlayerToPlay();
        assertDoesNotThrow(() -> controller.playerDoNothing(p1));
        Player p2 = game.getPlayerToPlay();
        assertDoesNotThrow(() -> controller.playerDoNothing(p2));
        Player p3 = game.getPlayerToPlay();
        assertDoesNotThrow(() -> controller.playerDoNothing(p3));

        // After all three players pass, the round must have advanced (→ PLACING_PHASE)
        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
    }

    // ─────────────────────────── selectCardFromTopList (last action advances turn) ───────────────────────────

    @Test
    void selectCardFromTopListLastActionAdvancesToNextPlayer() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        // Tile 'C' (index 1): drawTop=1, drawBot=0 — single top-draw action.
        // Place firstPlacer on tile 1 and the others on higher indices (2, 3) so that
        // tile 1 is the lowest occupied index and firstPlacer is first to play.
        Player firstPlacer = game.getPlayerToPlace();
        controller.placingPlayer(firstPlacer, 1); // tile C (drawTop=1)
        controller.placingPlayer(game.getPlayerToPlace(), 2);
        controller.placingPlayer(game.getPlayerToPlace(), 3);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player firstToPlay = game.getPlayerToPlay();
        assertEquals(1, game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop());

        List<Card> topList = game.getMarket().getTopCardList();
        long nonEvents = topList.stream().filter(c -> c.getCardType() != CARD_TYPE.EVENT).count();
        if (nonEvents > 0) {
            int idx = 0;
            for (int i = 0; i < topList.size(); i++) {
                if (topList.get(i).getCardType() != CARD_TYPE.EVENT) {
                    idx = i;
                    break;
                }
            }
            final int finalIdx = idx;
            // After consuming the single top action, the turn must advance automatically
            assertDoesNotThrow(() ->
                    controller.selectCardFromTopList(firstToPlay, CARD_TYPE.ARTIST, finalIdx));
            assertNotEquals(firstToPlay, game.getPlayerToPlay());
        }
    }

    // ─────────────────────────── selectCardFromBottomList (last action advances turn) ───────────────────────────

    @Test
    void selectCardFromBottomListLastActionAdvancesToNextPlayer() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        // Tile 'B' (index 0): drawTop=0, drawBot=1 — single bottom-draw action
        Player firstPlacer = game.getPlayerToPlace();
        controller.placingPlayer(firstPlacer, 0); // tile B (drawBot=1)
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player firstToPlay = game.getPlayerToPlay();
        assertEquals(1, game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom());

        List<Card> bottomList = game.getMarket().getBottomCardList();
        long nonEvents = bottomList.stream().filter(c -> c.getCardType() != CARD_TYPE.EVENT).count();
        if (nonEvents > 0) {
            int idx = 0;
            for (int i = 0; i < bottomList.size(); i++) {
                if (bottomList.get(i).getCardType() != CARD_TYPE.EVENT) {
                    idx = i;
                    break;
                }
            }
            final int finalIdx = idx;
            assertDoesNotThrow(() ->
                    controller.selectCardFromBottomList(firstToPlay, CARD_TYPE.ARTIST, finalIdx));
            // Turn must have advanced automatically after consuming the last action
            assertNotEquals(firstToPlay, game.getPlayerToPlay());
        }
    }
}