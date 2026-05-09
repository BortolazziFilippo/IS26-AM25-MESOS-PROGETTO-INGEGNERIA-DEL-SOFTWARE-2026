package it.polimi.ingsw.am25.server.Controller;

import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Controller.Controller;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Game.Game;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.ActionNotAvailable;
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
        player3 = new Player("Terzo", COLOR.YELLOW);
        controller = new Controller();
        controller.createGame(host, 3);
    }

    /** Reflectively extracts the private {@code game} field from the controller. */
    private Game getGame(Controller c) throws Exception {
        Field f = Controller.class.getDeclaredField("game");
        f.setAccessible(true);
        return (Game) f.get(c);
    }

    /**
     * Adds the remaining players, starts the game, places all three players on offer
     * tiles (indices 1, 2, 3) so the game advances to RESOLVE_ACTION and returns the
     * resulting Game instance.
     */
    private Game advanceToResolveAction() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

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

        controller.addPlayer(player3);
        controller.controllerGameStar();
        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
    }

    @Test
    void addingPlayerAfterGameStartedShouldBeIgnored() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

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
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);
        Player playerToPlace = game.getPlayerToPlace();

        assertDoesNotThrow(() -> controller.placingPlayer(playerToPlace, 1));
    }

    @Test
    void placingWrongPlayerInPlacingPhaseShouldBeIgnored() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);
        Player playerToPlace = game.getPlayerToPlace();
        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(playerToPlace))
                .findFirst()
                .orElseThrow();

        GAME_PHASE phaseBefore = game.getGamePhase();
        assertThrows(ActionNotAvailable.class, () -> controller.placingPlayer(wrongPlayer, 0));
        assertEquals(phaseBefore, game.getGamePhase());
    }

    @Test
    void placingPlayerOutOfBoundsThrowsIndexOutOfBoundsException() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);
        Player playerToPlace = game.getPlayerToPlace();
        assertThrows(IndexOutOfBoundsException.class,
                () -> controller.placingPlayer(playerToPlace, 999));
    }

    @Test
    void placingPlayerOnOccupiedTileThrowsTileOccupiedException() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);
        Player first = game.getPlayerToPlace();
        controller.placingPlayer(first, 0);

        Player second = game.getPlayerToPlace();
        assertThrows(TileOccupiedException.class,
                () -> controller.placingPlayer(second, 0));
    }

    @Test
    void placingPlayerInWrongPhaseThrowsActionNotAvailable() throws Exception {
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertThrows(ActionNotAvailable.class, () -> controller.placingPlayer(host, 0));
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
    }

    @Test
    void fullPlacingPhaseTransitionsToResolveAction() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);

        controller.placingPlayer(game.getPlayerToPlace(), 0);
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
    }

    // ─────────────────────────── selectCardFromTopList ───────────────────────────

    @Test
    void selectCardFromTopListInWrongPhaseThrowsActionNotAvailable() throws Exception {
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertThrows(ActionNotAvailable.class, () ->
                controller.selectCardFromTopList(host, CARD_TYPE.ARTIST, 0));
    }

    @Test
    void selectCardFromTopListWrongPlayerThrowsActionNotAvailable() throws Exception {
        Game game = advanceToResolveAction();

        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(game.getPlayerToPlay()))
                .findFirst()
                .orElseThrow();

        int tribeBefore = wrongPlayer.getTribe().size();
        assertThrows(ActionNotAvailable.class, () ->
                controller.selectCardFromTopList(wrongPlayer, CARD_TYPE.ARTIST, 0));
        assertEquals(tribeBefore, wrongPlayer.getTribe().size());
    }

    @Test
    void selectCardFromTopListWhenTileHasNoTopActionThrowsActionNotAvailable() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);
        Player p1 = game.getPlayerToPlace();
        controller.placingPlayer(p1, 0); // tile B: drawTop=0
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player playerToPlay = game.getPlayerToPlay();
        assertEquals(0, game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop());

        int tribeBefore = playerToPlay.getTribe().size();
        assertThrows(ActionNotAvailable.class, () ->
                controller.selectCardFromTopList(playerToPlay, CARD_TYPE.ARTIST, 0));
        assertEquals(tribeBefore, playerToPlay.getTribe().size());
    }

    @Test
    void selectCardFromTopListOutOfBoundsThrowsIndexOutOfBoundsException() throws Exception {
        Game game = advanceToResolveAction();
        Player playerToPlay = game.getPlayerToPlay();

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
            assertThrows(NotSelectableCardException.class,
                    () -> controller.selectCardFromTopList(playerToPlay, CARD_TYPE.EVENT, 0));
        }
    }

    @Test
    void selectCardFromTopListWithRightPlayerAddsCardToTribe() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);

        controller.placingPlayer(game.getPlayerToPlace(), 3); // tile E: drawTop=1
        controller.placingPlayer(game.getPlayerToPlace(), 4);
        controller.placingPlayer(game.getPlayerToPlace(), 1);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player playerToPlay = game.getPlayerToPlay();

        int tribeSizeBefore = playerToPlay.getTribe().size();
        int topListSizeBefore = game.getMarket().getTopCardList().stream()
                .filter(c -> c.getCardType() != CARD_TYPE.EVENT).toList().size();

        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop() > 0
                && topListSizeBefore > 0) {
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
    void selectCardFromBottomListInWrongPhaseThrowsActionNotAvailable() throws Exception {
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertThrows(ActionNotAvailable.class, () ->
                controller.selectCardFromBottomList(host, CARD_TYPE.ARTIST, 0));
    }

    @Test
    void selectCardFromBottomListWrongPlayerThrowsActionNotAvailable() throws Exception {
        Game game = advanceToResolveAction();

        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(game.getPlayerToPlay()))
                .findFirst()
                .orElseThrow();

        int tribeBefore = wrongPlayer.getTribe().size();
        assertThrows(ActionNotAvailable.class, () ->
                controller.selectCardFromBottomList(wrongPlayer, CARD_TYPE.ARTIST, 0));
        assertEquals(tribeBefore, wrongPlayer.getTribe().size());
    }

    @Test
    void selectCardFromBottomListWhenTileHasNoBottomActionThrowsActionNotAvailable() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);
        Player p1 = game.getPlayerToPlace();
        controller.placingPlayer(p1, 1); // tile C: drawBot=0
        controller.placingPlayer(game.getPlayerToPlace(), 2);
        controller.placingPlayer(game.getPlayerToPlace(), 3);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player playerToPlay = game.getPlayerToPlay();
        assertEquals(0, game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom());

        int tribeBefore = playerToPlay.getTribe().size();
        assertThrows(ActionNotAvailable.class, () ->
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
        controller.controllerGameStar();

        Game game = getGame(controller);
        controller.placingPlayer(game.getPlayerToPlace(), 0); // tile B: drawBot=1
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
    void playerDoNothingInWrongPhaseThrowsActionNotAvailable() throws Exception {
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertThrows(ActionNotAvailable.class, () -> controller.playerDoNothing(host));
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
    }

    @Test
    void playerDoNothingWrongPlayerThrowsActionNotAvailable() throws Exception {
        Game game = advanceToResolveAction();

        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(game.getPlayerToPlay()))
                .findFirst()
                .orElseThrow();

        Player currentBefore = game.getPlayerToPlay();
        assertThrows(ActionNotAvailable.class, () -> controller.playerDoNothing(wrongPlayer));
        assertEquals(currentBefore, game.getPlayerToPlay());
    }

    @Test
    void playerDoNothingThrowsWhenPlayerCanStillPlay() throws Exception {
        Game game = advanceToResolveAction();
        Player playerToPlay = game.getPlayerToPlay();

        assertTrue(game.canCurrentPlayingPlayerDoSomething());
        assertThrows(Exception.class, () -> controller.playerDoNothing(playerToPlay));
    }

    @Test
    void playerDoNothingAdvancesTurnWhenMarketIsEmpty() throws Exception {
        Game game = advanceToResolveAction();
        Player firstToPlay = game.getPlayerToPlay();

        game.getMarket().getTopCardList().clear();
        game.getMarket().getBottomCardList().clear();

        assertFalse(game.canCurrentPlayingPlayerDoSomething());
        assertDoesNotThrow(() -> controller.playerDoNothing(firstToPlay));
        assertNotEquals(firstToPlay, game.getPlayerToPlay());
    }

    @Test
    void playerDoNothingLastPlayerEndsRound() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);
        controller.placingPlayer(game.getPlayerToPlace(), 0);
        controller.placingPlayer(game.getPlayerToPlace(), 1);
        controller.placingPlayer(game.getPlayerToPlace(), 2);
        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());

        game.getMarket().getTopCardList().clear();
        game.getMarket().getBottomCardList().clear();

        Player p1 = game.getPlayerToPlay();
        assertDoesNotThrow(() -> controller.playerDoNothing(p1));
        Player p2 = game.getPlayerToPlay();
        assertDoesNotThrow(() -> controller.playerDoNothing(p2));
        Player p3 = game.getPlayerToPlay();
        assertDoesNotThrow(() -> controller.playerDoNothing(p3));

        assertEquals(GAME_PHASE.PLACING_PHASE, game.getGamePhase());
    }

    // ─────────────────────────── selectCardFromTopList (last action advances turn) ───────────────────────────

    @Test
    void selectCardFromTopListLastActionAdvancesToNextPlayer() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);
        controller.controllerGameStar();

        Game game = getGame(controller);
        Player firstPlacer = game.getPlayerToPlace();
        controller.placingPlayer(firstPlacer, 1); // tile C: drawTop=1
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
        controller.controllerGameStar();

        Game game = getGame(controller);
        Player firstPlacer = game.getPlayerToPlace();
        controller.placingPlayer(firstPlacer, 0); // tile B: drawBot=1
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
            assertNotEquals(firstToPlay, game.getPlayerToPlay());
        }
    }
}
