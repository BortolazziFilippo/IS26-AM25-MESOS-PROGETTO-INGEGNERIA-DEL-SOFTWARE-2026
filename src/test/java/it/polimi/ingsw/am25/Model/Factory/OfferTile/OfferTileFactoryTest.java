package it.polimi.ingsw.am25.Model.Factory.OfferTile;

import it.polimi.ingsw.am25.Model.Board.Action;
import it.polimi.ingsw.am25.Model.Board.OfferTile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfferTileFactoryTest {

    @Test
    void shouldCreateTheRightOfferTiles(){
        OfferTileFactory offerTileFactory= new OfferTileFactory();

        //TWO PLAYER
        List<OfferTile> offerTiles= offerTileFactory.offertileBuilder(2);
        assertEquals(4,offerTiles.size());
        assertEquals('B',offerTiles.getFirst().getOfferTileID());
        assertEquals(new Action(0,1),offerTiles.getFirst().getActionAvailable());
        assertEquals('C',offerTiles.get(1).getOfferTileID());
        assertEquals('E',offerTiles.get(2).getOfferTileID());
        assertEquals('F',offerTiles.get(3).getOfferTileID());

        //TWO PLAYER
        offerTiles= offerTileFactory.offertileBuilder(3);
        assertEquals(5,offerTiles.size());
        assertEquals('B',offerTiles.getFirst().getOfferTileID());
        assertEquals('C',offerTiles.get(1).getOfferTileID());
        assertEquals('D',offerTiles.get(2).getOfferTileID());
        assertEquals('E',offerTiles.get(3).getOfferTileID());
        assertEquals('F',offerTiles.get(4).getOfferTileID());

        //TWO PLAYER
        offerTiles= offerTileFactory.offertileBuilder(4);
        assertEquals(6,offerTiles.size());
        assertEquals('B',offerTiles.getFirst().getOfferTileID());
        assertEquals('C',offerTiles.get(1).getOfferTileID());
        assertEquals('D',offerTiles.get(2).getOfferTileID());
        assertEquals('E',offerTiles.get(3).getOfferTileID());
        assertEquals('F',offerTiles.get(4).getOfferTileID());
        assertEquals('G',offerTiles.get(5).getOfferTileID());

        //TWO PLAYER
        offerTiles= offerTileFactory.offertileBuilder(5);
        assertEquals(7,offerTiles.size());
        assertEquals('A',offerTiles.getFirst().getOfferTileID());
        assertEquals('B',offerTiles.get(1).getOfferTileID());
        assertEquals('C',offerTiles.get(2).getOfferTileID());
        assertEquals('D',offerTiles.get(3).getOfferTileID());
        assertEquals('E',offerTiles.get(4).getOfferTileID());
        assertEquals('F',offerTiles.get(5).getOfferTileID());
        assertEquals('G',offerTiles.get(6).getOfferTileID());


    }

}