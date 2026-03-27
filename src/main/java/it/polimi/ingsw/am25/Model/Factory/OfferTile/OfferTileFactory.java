package it.polimi.ingsw.am25.Model.Factory.OfferTile;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.Model.Board.OfferTile;
import it.polimi.ingsw.am25.Model.Factory.Deck.DeckFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OfferTileFactory {
    public OfferTileFactory() {
    }
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
                System.err.println(getClass() +": Errore numero giocatori");
        }
        if(inputStream==null){
            throw new RuntimeException(getClass()+" errore apertura file");
        }
        Reader reader= new InputStreamReader(inputStream);
        Gson gson = new Gson();
        OfferTile[] offerTiles = gson.fromJson(reader, OfferTile[].class);

        return new ArrayList<>(Arrays.stream(offerTiles).toList());
    }

}
