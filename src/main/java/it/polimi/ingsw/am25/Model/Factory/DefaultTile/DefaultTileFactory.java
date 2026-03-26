package it.polimi.ingsw.am25.Model.Factory.DefaultTile;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.Model.Board.DefaultTile;
import it.polimi.ingsw.am25.Model.Factory.Deck.DeckFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultTileFactory {

    public DefaultTileFactory() {
    }
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
                System.err.println(getClass() +": Errore numero giocatori");
        }
        if(inputStream==null){
            throw new RuntimeException(getClass()+" errore apertura file");
        }
        Reader reader= new InputStreamReader(inputStream);
        Gson gson= new Gson();
        DefaultTile[] defaultTiles= gson.fromJson(reader,DefaultTile[].class);
        return new ArrayList<>(Arrays.stream(defaultTiles).toList());

    }
}
