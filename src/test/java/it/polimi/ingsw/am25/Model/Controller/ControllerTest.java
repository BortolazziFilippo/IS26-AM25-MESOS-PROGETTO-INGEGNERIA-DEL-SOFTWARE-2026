package it.polimi.ingsw.am25.Model.Controller;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.COLOR;
import it.polimi.ingsw.am25.Model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.Model.Game.Game;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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
        controller = new Controller(host, 3);
    }

    private Game getGame(Controller c) throws Exception {
        Field f = Controller.class.getDeclaredField("game");
        f.setAccessible(true);
        return (Game) f.get(c);
    }

    @Test
    void addingPlayersDuringSetupTransitionsToPlacingPhase() throws Exception {
        controller.addPlayer(player2);
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());

        // adding the last player triggers gameStart -> PLACING_PHASE
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

    @Test
    void placingPlayerInPlacingPhase() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3); // game started, now PLACING_PHASE

        Game game = getGame(controller);
        Player playerToPlace = game.getPlayerToPlace();

        // find a free offer-tile position; tiles 0-5 exist
        assertDoesNotThrow(() -> controller.placingPlayer(playerToPlace, 1));
    }

    @Test
    void placingWrongPlayerInPlacingPhaseShouldBeIgnored() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);
        Player playerToPlace = game.getPlayerToPlace();
        // choose a player who is NOT supposed to place now
        Player wrongPlayer = game.getPlayerList().stream()
                .filter(p -> !p.equals(playerToPlace))
                .findFirst()
                .orElseThrow();

        GAME_PHASE phaseBefore = game.getGamePhase();
        // should not throw and should not advance
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
    void selectCardFromTopListInWrongPhaseIsIgnored() throws Exception {
        // game is still in SETUP; action should be silently ignored
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertDoesNotThrow(() ->
                controller.selectCardFromTopList(host, CARD_TYPE.ARTIST, 0));
    }

    @Test
    void selectCardFromBottomListInWrongPhaseIsIgnored() throws Exception {
        Game game = getGame(controller);
        assertEquals(GAME_PHASE.SETUP, game.getGamePhase());
        assertDoesNotThrow(() ->
                controller.selectCardFromBottomList(host, CARD_TYPE.ARTIST, 0));
    }

    @Test
    void fullPlacingPhaseTransitionsToResolveAction() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);

        // place all 3 players
        Player p1 = game.getPlayerToPlace();
        controller.placingPlayer(p1, 0);
        Player p2 = game.getPlayerToPlace();
        controller.placingPlayer(p2, 1);
        Player p3 = game.getPlayerToPlace();
        controller.placingPlayer(p3, 2);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
    }

    @Test
    void selectCardFromTopListDuringResolveActionWithRightPlayer() throws Exception {
        controller.addPlayer(player2);
        controller.addPlayer(player3);

        Game game = getGame(controller);

        Player p1 = game.getPlayerToPlace();
        controller.placingPlayer(p1, 1);
        Player p2 = game.getPlayerToPlace();
        controller.placingPlayer(p2, 2);
        Player p3 = game.getPlayerToPlace();
        controller.placingPlayer(p3, 3);

        assertEquals(GAME_PHASE.RESOLVE_ACTION, game.getGamePhase());
        Player playerToPlay = game.getPlayerToPlay();

        int tribeSizeBefore = playerToPlay.getTribe().size();
        int topListSizeBefore = game.getMarket().getTopCardList().stream()
                .filter(c -> c.getCardType() != CARD_TYPE.EVENT).toList().size();

        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop() > 0
                && topListSizeBefore > 0) {
            int idx = 0;
            for (int i = 0; i < game.getMarket().getTopCardList().size(); i++) {
                if (game.getMarket().getTopCardList().get(i).getCardType() != CARD_TYPE.EVENT) {
                    idx = i;
                    break;
                }
            }
            final int finalIdx = idx;
            assertDoesNotThrow(() ->
                    controller.selectCardFromTopList(playerToPlay, CARD_TYPE.ARTIST, finalIdx));
        }
    }
}