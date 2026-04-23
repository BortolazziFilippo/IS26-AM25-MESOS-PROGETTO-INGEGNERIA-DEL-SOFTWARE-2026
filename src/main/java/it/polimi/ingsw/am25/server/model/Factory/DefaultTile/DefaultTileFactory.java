package it.polimi.ingsw.am25.server.model.Factory.DefaultTile;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultTileFactory {
    private static final String LOG_PREFIX = "[SERVER][DEFAULT_TILE_FACTORY]";

    /**
     * Creates a new default tile factory instance.
     */
    public DefaultTileFactory() {
    }
    /**
     * Executes build default tiles.
     * @param playerNumber parameter playerNumber.
     * @return the result of the operation.
     */
    public List<DefaultTile> buildDefaultTiles(int playerNumber){
        InputStream inputStream=null;
        switch (playerNumber){
            case 2:
                inputStream= DeckFactory.class.getResourceAsStream("/Board/json/Tiles/TwoPlayerDefaultTile.json");
                break;
            case 3:
                inputStream= DeckFactory.class.getResourceAsStream("/Board/json/Tiles/ThreePlayerDefaultTile.json");
                break;
            case 4:
                inputStream= DeckFactory.class.getResourceAsStream("/Board/json/Tiles/FourPlayerDefaultTile.json");
                break;
            case 5:
                inputStream= DeckFactory.class.getResourceAsStream("/Board/json/Tiles/FivePlayerDefaultTile.json");
                break;
            default:
                logServerError("Invalid player number: " + playerNumber);
        }
        if(inputStream==null){
            throw new RuntimeException(getClass()+" errore apertura file");
        }
        Reader reader= new InputStreamReader(inputStream);
        Gson gson= new Gson();
        DefaultTile[] defaultTiles= gson.fromJson(reader,DefaultTile[].class);
        return new ArrayList<>(Arrays.stream(defaultTiles).toList());

    }

    /**
     * Executes log server error.
     * @param message parameter message.
     */
    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }
}
