package it.polimi.ingsw.am25.server.Factory.DefaultTile;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Factory.DefaultTile.DefaultTileFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultTileFactoryTest {

    @Test
    void buildDefaultTiles_variousPlayerCounts_createsCorrectCount() {
        DefaultTileFactory factory = new DefaultTileFactory();

        //twoPlayer
        List<DefaultTile> defaultTiles = factory.buildDefaultTiles(2);
        assertEquals(2, defaultTiles.size());
        assertEquals(1,defaultTiles.get(0).getFoodPerSlotPosition());
        assertEquals(-1,defaultTiles.get(1).getFoodPerSlotPosition());

        //three player
        defaultTiles = factory.buildDefaultTiles(3);
        assertEquals(3, defaultTiles.size());
        assertEquals(2,defaultTiles.get(0).getFoodPerSlotPosition());
        assertEquals(0,defaultTiles.get(1).getFoodPerSlotPosition());
        assertEquals(-1,defaultTiles.get(2).getFoodPerSlotPosition());

        //four Player
        defaultTiles = factory.buildDefaultTiles(4);
        assertEquals(4, defaultTiles.size());
        assertEquals(2,defaultTiles.get(0).getFoodPerSlotPosition());
        assertEquals(1,defaultTiles.get(1).getFoodPerSlotPosition());
        assertEquals(0,defaultTiles.get(2).getFoodPerSlotPosition());
        assertEquals(-1,defaultTiles.get(3).getFoodPerSlotPosition());

        //five player
        defaultTiles = factory.buildDefaultTiles(5);
        assertEquals(5, defaultTiles.size());
        assertEquals(3,defaultTiles.get(0).getFoodPerSlotPosition());
        assertEquals(1,defaultTiles.get(1).getFoodPerSlotPosition());
        assertEquals(0,defaultTiles.get(2).getFoodPerSlotPosition());
        assertEquals(0,defaultTiles.get(3).getFoodPerSlotPosition());
        assertEquals(-1,defaultTiles.get(4).getFoodPerSlotPosition());
    }

}