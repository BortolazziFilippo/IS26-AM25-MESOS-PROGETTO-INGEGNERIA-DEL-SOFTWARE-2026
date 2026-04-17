package it.polimi.ingsw.am25.server.webLayer.DTOs;

import java.io.Serial;
import java.io.Serializable;

public class OffertileDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final char offerTileID;

    public OffertileDTO(char offerTileID) {
        this.offerTileID = offerTileID;
    }
}
