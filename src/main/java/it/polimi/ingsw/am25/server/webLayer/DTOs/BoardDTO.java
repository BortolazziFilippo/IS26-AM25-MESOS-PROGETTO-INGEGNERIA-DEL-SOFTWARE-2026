package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class BoardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<OffertileDTO> offerTileList;
    private List<DefaultTileDTO> defaultTileList;
}
