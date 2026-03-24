package it.polimi.ingsw.am25.Model.Tile;

import java.util.List;

public class OfferTrack {
    private List<OfferTile> offerTiles;
    private final DefaultTile defaultTile;

    public OfferTrack(int playerNumber) {
        this.defaultTile = new DefaultTile(playerNumber);
    }

    public List<OfferTile> getAvailableOfferTiles() {
        return this.offerTiles;
    }

    //questp getter serve perchè defaultTile è privato
    public DefaultTile getDefaultTile() {
        return this.defaultTile;
    }

}
