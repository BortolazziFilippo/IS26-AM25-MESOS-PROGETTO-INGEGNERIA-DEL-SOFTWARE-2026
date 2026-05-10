package it.polimi.ingsw.am25.server.model.persistance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class PersistanceLogger {
    private static final String LOG_PREFIX= "[SERVER][LOGGER] ";
    private static final Path SAVE_PATH=Path.of(System.getProperty("user.dir"),
            "saves",
            "game_save.json"
    );
    public PersistanceLogger() {
    }
    public void save(GameMemento memento){
        try {
            Files.createDirectories(SAVE_PATH.getParent());
            Files.writeString(SAVE_PATH,new Gson().toJson(memento));
            UtilitiesFunction.logInfo(LOG_PREFIX,"Game state saved to " + SAVE_PATH);
        } catch (IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX,"Failed to save game");
        }
    }

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

    public void deleteFile(){
        try {
            Files.deleteIfExists(SAVE_PATH);
            UtilitiesFunction.logInfo(LOG_PREFIX,"Save file deleted from " + SAVE_PATH);
        } catch (IOException e){
            UtilitiesFunction.logError(LOG_PREFIX,"Failed to delete save file");
        }
    }
}
