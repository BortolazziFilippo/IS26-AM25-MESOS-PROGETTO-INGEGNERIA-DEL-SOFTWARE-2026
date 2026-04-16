package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;

import java.util.List;

public class BoardDTO {
    private List<OfferTile> offerTileList;
    private List<DefaultTile> defaultTileList;
}
