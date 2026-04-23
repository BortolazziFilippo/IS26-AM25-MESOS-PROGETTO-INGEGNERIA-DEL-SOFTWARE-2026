package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.OfferTile;

import java.io.Serial;
import java.io.Serializable;

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
