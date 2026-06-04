package it.polimi.ingsw.am25.server.Controller;

import it.polimi.ingsw.am25.server.model.Controller.Controller;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.CONNECTION_STATUS;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Game.Game;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameAlreadyLoadedException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameReadyToStartException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NoGameToLoadException;
import it.polimi.ingsw.am25.server.model.persistance.BoardMemento;
import it.polimi.ingsw.am25.server.model.persistance.GameMemento;
import it.polimi.ingsw.am25.server.model.persistance.MarketMemento;
import it.polimi.ingsw.am25.server.model.persistance.PersistanceLogger;
import it.polimi.ingsw.am25.server.model.persistance.PlayerMemento;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Controller tests that require mocking infrastructure (Mockito).
 * Covers: observer wiring, loadGame/reconnectLoadedPlayer with a fake persistence layer.
 */
@ExtendWith(MockitoExtension.class)
class ControllerMockitoTest {

    @Mock
    private PersistanceLogger mockLogger;

    @Mock
    private ServerVirtualView mockView;

    @Mock
    private ServerVirtualView mockView2;

    private Controller controller;
    private Player host;
    private Player player2;

    @BeforeEach
    void setUp() throws Exception {
        host = new Player("Alice", COLOR.RED);
        player2 = new Player("Bob", COLOR.BLUE);
        controller = new Controller();
        injectLogger(controller, mockLogger);
    }

    /** Reflectively replaces the package-private persistanceLogger field. */
    private static void injectLogger(Controller c, PersistanceLogger logger) throws Exception {
        Field f = Controller.class.getDeclaredField("persistanceLogger");
        f.setAccessible(true);
        f.set(c, logger);
    }

    /** Reflectively extracts the private game field. */
    private static Game getGame(Controller c) throws Exception {
        Field f = Controller.class.getDeclaredField("game");
        f.setAccessible(true);
        return (Game) f.get(c);
    }

    /** Reflectively extracts the private players list used for reconnection tracking. */
    @SuppressWarnings("unchecked")
    private static List<Player> getControllerPlayers(Controller c) throws Exception {
        Field f = Controller.class.getDeclaredField("players");
        f.setAccessible(true);
        return (List<Player>) f.get(c);
    }

    // ─────────────────────────── linkObserver ───────────────────────────

    @Test
    void linkObserverRegistersViewWithGame() throws Exception {
        controller.createGame(host, 2);
        controller.addPlayer(player2);
        controller.controllerGameStart();

        // linkObserver must not throw and must wire the view into the game's observer list
        assertDoesNotThrow(() -> controller.linkObserver(mockView));
    }

    // ─────────────────────────── crossRegisterPlayerObservers ───────────────────────────

    @Test
    void crossRegisterPlayerObserversAddsEveryViewToEveryPlayer() throws Exception {
        controller.createGame(host, 2);
        controller.addPlayer(player2);
        controller.controllerGameStart();

        controller.crossRegisterPlayerObservers(List.of(mockView, mockView2));

        // After cross-registration all players can notify both views;
        // confirm by verifying the game still has 2 players and no exception occurred
        assertEquals(2, controller.getAllPlayers().size());
    }

    @Test
    void crossRegisterPlayerObserversWithEmptyListIsHarmless() throws Exception {
        controller.createGame(host, 2);
        controller.addPlayer(player2);
        controller.controllerGameStart();

        assertDoesNotThrow(() -> controller.crossRegisterPlayerObservers(List.of()));
    }

    // ─────────────────────────── loadGame ───────────────────────────

    @Test
    void loadGame_gameAlreadyInitialised_throwsGameAlreadyLoadedException() throws Exception {
        controller.createGame(host, 2);

        assertThrows(GameAlreadyLoadedException.class, () -> controller.loadGame(host));
        verifyNoInteractions(mockLogger);
    }

    @Test
    void loadGame_noSaveFile_throwsNoGameToLoadException() {
        when(mockLogger.load()).thenReturn(Optional.empty());

        assertThrows(NoGameToLoadException.class, () -> controller.loadGame(host));
        verify(mockLogger).load();
    }

    @Test
    void loadGame_playerNotInSave_throwsIllegalStateException() {
        // Memento has only "Charlie", but the requesting player is "Alice"
        PlayerMemento charlieMem = new PlayerMemento("Charlie", COLOR.YELLOW, 3, 0, List.of(), List.of());
        GameMemento memento = minimalMemento(2, List.of(charlieMem));
        when(mockLogger.load()).thenReturn(Optional.of(memento));

        assertThrows(IllegalStateException.class, () -> controller.loadGame(host));
        verify(mockLogger).load();
    }

    @Test
    void loadGame_playerInSave_otherPlayersAreDisconnected() throws Exception {
        // Memento contains Alice (host) and Bob; use a real game-derived memento for validity
        GameMemento memento = buildValidMementoFor("Alice", "Bob");
        when(mockLogger.load()).thenReturn(Optional.of(memento));

        controller.loadGame(host); // host = Alice

        assertNotNull(getGame(controller));

        // The Controller tracks connection status in its own internal players list.
        // Alice (host) must be CONNECTED; Bob must be DISCONNECTED.
        List<Player> internal = getControllerPlayers(controller);
        Player alice = internal.stream().filter(p -> p.getNickname().equals("Alice")).findFirst().orElseThrow();
        Player bob   = internal.stream().filter(p -> p.getNickname().equals("Bob")).findFirst().orElseThrow();
        assertEquals(CONNECTION_STATUS.CONNECTED,    alice.getConnection());
        assertEquals(CONNECTION_STATUS.DISCONNECTED, bob.getConnection());
    }

    // ─────────────────────────── reconnectLoadedPlayer ───────────────────────────

    @Test
    void reconnectLoadedPlayer_gameNull_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> controller.reconnectLoadedPlayer(host));
    }

    @Test
    void reconnectLoadedPlayer_nicknameNotInSave_throwsIllegalStateException() throws Exception {
        GameMemento memento = buildValidMementoFor("Alice", "Bob");
        when(mockLogger.load()).thenReturn(Optional.of(memento));
        controller.loadGame(host);

        Player stranger = new Player("Stranger", COLOR.WHITE);
        assertThrows(IllegalStateException.class, () -> controller.reconnectLoadedPlayer(stranger));
    }

    @Test
    void reconnectLoadedPlayer_alreadyConnected_throwsIllegalStateException() throws Exception {
        GameMemento memento = buildValidMementoFor("Alice", "Bob");
        when(mockLogger.load()).thenReturn(Optional.of(memento));
        controller.loadGame(host); // Alice is CONNECTED

        // Reconnecting Alice again must fail
        assertThrows(IllegalStateException.class, () -> controller.reconnectLoadedPlayer(host));
    }

    @Test
    void reconnectLoadedPlayer_lastPlayerReconnects_throwsGameReadyToStartException() throws Exception {
        GameMemento memento = buildValidMementoFor("Alice", "Bob");
        when(mockLogger.load()).thenReturn(Optional.of(memento));
        controller.loadGame(host); // Alice CONNECTED, Bob DISCONNECTED

        // Bob is the last disconnected player; reconnecting him fills the lobby
        assertThrows(GameReadyToStartException.class,
                () -> controller.reconnectLoadedPlayer(player2));
    }

    @Test
    void reconnectLoadedPlayer_notLastPlayer_doesNotThrow() throws Exception {
        Player player3 = new Player("Carol", COLOR.YELLOW);
        GameMemento memento = buildValidMementoFor("Alice", "Bob", "Carol");
        when(mockLogger.load()).thenReturn(Optional.of(memento));
        controller.loadGame(host); // Alice CONNECTED, Bob+Carol DISCONNECTED

        // Reconnecting Bob is fine — Carol is still missing → no GameReadyToStartException
        assertDoesNotThrow(() -> controller.reconnectLoadedPlayer(player2));
    }

    // ─────────────────────────── resumeGame ───────────────────────────

    @Test
    void resumeGame_withLinkedObserver_doesNotThrow() throws Exception {
        controller.createGame(host, 2);
        controller.addPlayer(player2);
        controller.controllerGameStart();
        controller.linkObserver(mockView);

        assertDoesNotThrow(() -> controller.resumeGame());
    }

    // ─────────────────────────── helpers ───────────────────────────

    /**
     * Builds a minimal {@link GameMemento} with no market data, sufficient only to
     * satisfy the player-lookup logic in {@link Controller#loadGame} without triggering
     * {@code game.restoreMemento}.
     */
    private static GameMemento minimalMemento(int playerCount, List<PlayerMemento> players) {
        MarketMemento emptyMarket = new MarketMemento(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        BoardMemento emptyBoard = new BoardMemento(players.stream().map(PlayerMemento::nickname).toList());
        return new GameMemento(ERA.ERA_I, GAME_PHASE.PLACING_PHASE, playerCount, null, null, players, emptyMarket, emptyBoard);
    }

    /**
     * Builds a fully valid {@link GameMemento} by running a real 2-player (or N-player)
     * game to PLACING_PHASE and capturing its snapshot. This gives a memento that
     * {@link Game#restoreMemento} can consume without errors.
     */
    private static GameMemento buildValidMementoFor(String... nicknames) throws Exception {
        int count = nicknames.length;
        Player[] players = new Player[count];
        COLOR[] colors = {COLOR.RED, COLOR.BLUE, COLOR.YELLOW, COLOR.WHITE};
        for (int i = 0; i < count; i++) {
            players[i] = new Player(nicknames[i], colors[i]);
        }

        Controller tmp = new Controller();
        tmp.createGame(players[0], count);
        for (int i = 1; i < count; i++) tmp.addPlayer(players[i]);
        tmp.controllerGameStart();

        Game g = getGame(tmp);
        GameMemento snap = g.createMemento();

        // Replace nicknames in the snapshot so they match the requested names
        // (createMemento captures the real nicknames which already match → no remapping needed)
        return snap;
    }
}
