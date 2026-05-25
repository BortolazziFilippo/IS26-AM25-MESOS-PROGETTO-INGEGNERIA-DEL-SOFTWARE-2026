package it.polimi.ingsw.am25.server.model.Factory.DefaultTile;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the set of default tiles for a given player count by loading tile
 * definitions from JSON resources.
 */
public class DefaultTileFactory {
    private static final String LOG_PREFIX = "[SERVER][DEFAULT_TILE_FACTORY]";

    /**
     * Creates a new default tile factory instance.
     */
    public DefaultTileFactory() {
    }

    /**
     * Builds the list of default tiles for the given player count by loading the
     * correct JSON resource.
     *
     * @param playerNumber the number of players in the game (2–5).
     * @return list of {@link DefaultTile}s in slot order.
     */
    public List<DefaultTile> buildDefaultTiles(int playerNumber) {
        String jsonFile = switch (playerNumber) {
            case 2 -> "/Board/json/Tiles/TwoPlayerDefaultTile.json";
            case 3 -> "/Board/json/Tiles/ThreePlayerDefaultTile.json";
            case 4 -> "/Board/json/Tiles/FourPlayerDefaultTile.json";
            case 5 -> "/Board/json/Tiles/FivePlayerDefaultTile.json";
            default -> {
                logServerError("Invalid player number: " + playerNumber);
                yield null;
            }
        };
        InputStream inputStream = DefaultTileFactory.class.getResourceAsStream(jsonFile);
        if (inputStream == null) {
            throw new RuntimeException(getClass() + " errore apertura file");
        }
        DefaultTile[] defaultTiles = new Gson().fromJson(new InputStreamReader(inputStream), DefaultTile[].class);
        return new ArrayList<>(List.of(defaultTiles));
    }

    /**
     * Executes log server error.
     *
     * @param message parameter message.
     */
    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }
}
