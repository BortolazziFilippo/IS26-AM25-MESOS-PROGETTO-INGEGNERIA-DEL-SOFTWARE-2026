package it.polimi.ingsw.am25.server.persistance;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.persistance.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PersistanceLoggerTest {

    // Deve corrispondere esattamente al SAVE_PATH interno di PersistanceLogger
    private static final Path SAVE_PATH =
            Path.of(System.getProperty("user.dir"), "saves", "game_save.json");

    private final PersistanceLogger logger = new PersistanceLogger();

    @BeforeEach
    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(SAVE_PATH);
    }

    // ─────────────────────────── load ───────────────────────────

    @Test
    void load_noFile_returnsEmpty() {
        Optional<GameMemento> result = logger.load();
        assertTrue(result.isEmpty());
    }

    @Test
    void load_corruptedFile_throwsJsonSyntaxException() throws IOException {
        Files.createDirectories(SAVE_PATH.getParent());
        Files.writeString(SAVE_PATH, "{ questo non è json valido %%%");

        // load() catches only IOException; a malformed JSON causes JsonSyntaxException to propagate
        assertThrows(JsonSyntaxException.class, () -> logger.load());
    }

    // ─────────────────────────── save + load (round-trip) ───────────────────────────

    @Test
    void saveAndLoad_roundTrip_preservesAllFields() {
        GameMemento original = sampleMemento();

        logger.save(original);
        Optional<GameMemento> loaded = logger.load();

        assertTrue(loaded.isPresent());
        GameMemento m = loaded.get();
        assertEquals(ERA.ERA_I,              m.currentEra());
        assertEquals(GAME_PHASE.PLACING_PHASE, m.gamePhase());
        assertEquals(2,                      m.playerNumber());
        assertEquals("Alice",                m.playerToPlaceNickname());
        assertEquals(1,                      m.players().size());

        PlayerMemento p = m.players().get(0);
        assertEquals("Alice",  p.nickname());
        assertEquals(COLOR.RED, p.totemColor());
        assertEquals(3,        p.food());
        assertEquals(7,        p.prestigePoints());
    }

    @Test
    void save_twice_secondOverwritesFirst() {
        GameMemento first  = sampleMemento();
        GameMemento second = new GameMemento(
                ERA.ERA_II, GAME_PHASE.RESOLVE_ACTION, 3, "Bob", "Carol",
                List.of(new PlayerMemento("Bob", COLOR.BLUE, 5, 0, List.of(), List.of())),
                new MarketMemento(List.of(), List.of(), List.of(), List.of(), List.of(), List.of()),
                new BoardMemento(List.of("Bob")));

        logger.save(first);
        logger.save(second);

        Optional<GameMemento> loaded = logger.load();
        assertTrue(loaded.isPresent());
        assertEquals(ERA.ERA_II, loaded.get().currentEra());
        assertEquals(3,          loaded.get().playerNumber());
        assertEquals("Bob",      loaded.get().players().get(0).nickname());
    }

    @Test
    void save_createsSavesDirectoryIfMissing() throws IOException {
        // cleanup rimuove solo il file; la cartella potrebbe già esistere
        // questo test verifica che save non esploda se la cartella c'è già
        assertDoesNotThrow(() -> logger.save(sampleMemento()));
        assertTrue(Files.exists(SAVE_PATH));
    }

    // ─────────────────────────── deleteFile ───────────────────────────

    @Test
    void deleteFile_existingFile_removesIt() {
        logger.save(sampleMemento());
        assertTrue(Files.exists(SAVE_PATH));

        logger.deleteFile();

        assertFalse(Files.exists(SAVE_PATH));
    }

    @Test
    void deleteFile_noFile_doesNotThrow() {
        assertFalse(Files.exists(SAVE_PATH));
        assertDoesNotThrow(() -> logger.deleteFile());
    }

    // ─────────────────────────── helper ───────────────────────────

    private static GameMemento sampleMemento() {
        PlayerMemento player = new PlayerMemento("Alice", COLOR.RED, 3, 7, List.of(), List.of());
        MarketMemento market = new MarketMemento(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        BoardMemento  board  = new BoardMemento(List.of("Alice"));
        return new GameMemento(ERA.ERA_I, GAME_PHASE.PLACING_PHASE, 2, "Alice", null,
                List.of(player), market, board);
    }
}
