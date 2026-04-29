package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.OfferTile;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data-transfer object for an offer tile, carrying the tile's letter ID
 * (A–E depending on player count) so the client can identify which tile was occupied.
 */
public class OffertileDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final char offerTileID;

    /**
     * Creates a new offertile dto instance.
     * @param offerTile parameter offerTile.
     */
    public OffertileDTO(OfferTile offerTile) {
        this.offerTileID = offerTile.getOfferTileID();
    }
}
