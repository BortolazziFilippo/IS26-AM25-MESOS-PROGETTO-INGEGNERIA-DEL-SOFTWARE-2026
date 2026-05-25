package it.polimi.ingsw.am25.server.model.Factory.OfferTile;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the set of offer tiles for a given player count by loading tile definitions from JSON resources.
 */
public class OfferTileFactory {
    private static final String LOG_PREFIX = "[SERVER][OFFER_TILE_FACTORY]";

    /**
     * Creates a new offer tile factory instance.
     */
    public OfferTileFactory() {
    }

    /**
     * Builds the list of offer tiles for the given player count by loading the
     * correct JSON resource.
     *
     * @param playerNumber the number of players in the game (2–5).
     * @return list of {@link OfferTile}s in slot order.
     */
    public List<OfferTile> offertileBuilder(int playerNumber) {
        String jsonFile = switch (playerNumber) {
            case 2 -> "/Board/json/Tiles/TwoPlayerOfferTile.json";
            case 3 -> "/Board/json/Tiles/ThreePlayerOfferTile.json";
            case 4 -> "/Board/json/Tiles/FourPlayerOfferTile.json";
            case 5 -> "/Board/json/Tiles/FivePlayerOfferTile.json";
            default -> {
                logServerError("Invalid player number: " + playerNumber);
                yield null;
            }
        };
        InputStream inputStream = OfferTileFactory.class.getResourceAsStream(jsonFile);
        if (inputStream == null) {
            throw new RuntimeException(getClass() + " errore apertura file");
        }
        OfferTile[] offerTiles = new Gson().fromJson(new InputStreamReader(inputStream), OfferTile[].class);
        return new ArrayList<>(List.of(offerTiles));
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
