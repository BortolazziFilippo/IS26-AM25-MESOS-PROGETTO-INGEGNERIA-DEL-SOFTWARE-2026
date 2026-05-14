package it.polimi.ingsw.am25.server.model.persistance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Manages saving and loading the game state to and from disk in JSON format.
 * The save file is written to {@code saves/game_save.json} relative to the current working directory.
 */
public class PersistanceLogger {
    private static final String LOG_PREFIX= "[SERVER][LOGGER] ";
    private static final Path SAVE_PATH=Path.of(System.getProperty("user.dir"),
            "saves",
            "game_save.json"
    );

    /**
     * Creates a new persistence logger instance.
     * The save file path is fixed to {@code saves/game_save.json} in the current working directory.
     */
    public PersistanceLogger() {
    }

    /**
     * Serialises the game memento to JSON and writes it to disk.
     * Creates the target directory if it does not exist. On I/O error,
     * logs an error message without propagating the exception.
     *
     * @param memento the game snapshot to save.
     */
    public void save(GameMemento memento){
        try {
            Files.createDirectories(SAVE_PATH.getParent());
            Files.writeString(SAVE_PATH,new Gson().toJson(memento));
            UtilitiesFunction.logInfo(LOG_PREFIX,"Game state saved to " + SAVE_PATH);
        } catch (IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX,"Failed to save game");
        }
    }

    /**
     * Reads the save file from disk and deserialises the game memento.
     * Returns {@link Optional#empty()} if the file does not exist or an I/O error occurs.
     *
     * @return an {@link Optional} containing the loaded {@link GameMemento}, or empty if unavailable.
     */
    public Optional<GameMemento> load(){
        if(!Files.exists(SAVE_PATH)){
            UtilitiesFunction.logInfo(LOG_PREFIX,"No save file found at " + SAVE_PATH);
            return Optional.empty();
        }
        try {
            String json=Files.readString(SAVE_PATH);
            UtilitiesFunction.logInfo(LOG_PREFIX,"Game state loaded from " + SAVE_PATH);
            return Optional.ofNullable(new Gson().fromJson(json,GameMemento.class));
        }catch (IOException e){
            UtilitiesFunction.logError(LOG_PREFIX,"Failed to load game");
            return Optional.empty();
        }
    }

    /**
     * Deletes the save file from disk if it exists.
     * Called at the end of the game to prevent a completed session from being inadvertently restored.
     * On I/O error, logs an error message without propagating the exception.
     */
    public void deleteFile(){
        try {
            Files.deleteIfExists(SAVE_PATH);
            UtilitiesFunction.logInfo(LOG_PREFIX,"Save file deleted from " + SAVE_PATH);
        } catch (IOException e){
            UtilitiesFunction.logError(LOG_PREFIX,"Failed to delete save file");
        }
    }
}
