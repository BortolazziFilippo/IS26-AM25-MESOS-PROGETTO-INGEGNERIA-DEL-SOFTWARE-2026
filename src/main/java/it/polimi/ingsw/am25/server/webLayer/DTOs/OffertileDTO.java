package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.OfferTile;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data-transfer object for an offer tile, carrying the tile's letter ID
 * and the number of top/bottom draws available on it.
 */
public class OffertileDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    /** The single-character identifier of this offer tile (e.g. 'A', 'B', …). */
    private final char offerTileID;
    /** The number of top-row draws available on this tile. */
    private final int drawTop;
    /** The number of bottom-row draws available on this tile. */
    private final int drawBot;

    /**
     * Creates a new offertile dto instance.
     *
     * @param offerTile parameter offerTile.
     */
    public OffertileDTO(OfferTile offerTile) {
        this.offerTileID = offerTile.getOfferTileID();
        this.drawTop = offerTile.getActionAvailable().getDrawTop();
        this.drawBot = offerTile.getActionAvailable().getDrawFromBottom();
    }

    /**
     * Returns the single-character identifier of this offer tile (e.g. 'A', 'B', …).
     *
     * @return the tile ID character.
     */
    public char getOfferTileID() {
        return offerTileID;
    }

    /**
     * Returns the number of top-row draws available on this tile.
     *
     * @return top draws.
     */
    public int getDrawTop() {
        return drawTop;
    }

    /**
     * Returns the number of bottom-row draws available on this tile.
     *
     * @return bottom draws.
     */
    public int getDrawBot() {
        return drawBot;
    }

}
