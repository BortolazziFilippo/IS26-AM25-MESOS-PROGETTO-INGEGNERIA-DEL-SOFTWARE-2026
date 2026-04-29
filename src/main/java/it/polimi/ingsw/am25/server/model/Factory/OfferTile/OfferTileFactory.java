package it.polimi.ingsw.am25.server.model.Factory.OfferTile;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
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
     * Executes offertile builder.
     * @param playerNumber parameter playerNumber.
     * @return the result of the operation.
     */
    public List<OfferTile> offertileBuilder(int playerNumber){
        InputStream inputStream=null;
        switch (playerNumber){
            case 2:
                inputStream= OfferTileFactory.class.getResourceAsStream("/Board/json/Tiles/TwoPlayerOfferTile.json");
                break;
            case 3:
                inputStream= OfferTileFactory.class.getResourceAsStream("/Board/json/Tiles/ThreePlayerOfferTile.json");
                break;
            case 4:
                inputStream= OfferTileFactory.class.getResourceAsStream("/Board/json/Tiles/FourPlayerOfferTile.json");
                break;
            case 5:
                inputStream= OfferTileFactory.class.getResourceAsStream("/Board/json/Tiles/FivePlayerOfferTile.json");
                break;
            default:
                logServerError("Invalid player number: " + playerNumber);
        }
        if(inputStream==null){
            throw new RuntimeException(getClass()+" errore apertura file");
        }
        Reader reader= new InputStreamReader(inputStream);
        Gson gson = new Gson();
        OfferTile[] offerTiles = gson.fromJson(reader, OfferTile[].class);

        return new ArrayList<>(Arrays.stream(offerTiles).toList());
    }

    /**
     * Executes log server error.
     * @param message parameter message.
     */
    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }
}
