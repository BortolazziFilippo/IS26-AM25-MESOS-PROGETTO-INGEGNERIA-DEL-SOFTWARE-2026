package it.polimi.ingsw.am25.Model.Tile;

import java.util.List;

public class OfferTrack {
    private List<OfferTile> offerTiles;
    private DefaultTile defaultTile;

    public OfferTrack(int playerNumber) {
        this.defaultTile = new DefaultTile(playerNumber);
    }
}
