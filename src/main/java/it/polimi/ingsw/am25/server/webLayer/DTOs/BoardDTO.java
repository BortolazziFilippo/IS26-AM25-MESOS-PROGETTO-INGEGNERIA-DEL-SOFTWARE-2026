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

    public BoardDTO(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) {
        this.offerTileList = offerTileList;
        this.defaultTileList = defaultTileList;
    }

    public List<OffertileDTO> getOfferTileList() {
        return offerTileList;
    }

    public void setOfferTileList(List<OffertileDTO> offerTileList) {
        this.offerTileList = offerTileList;
    }

    public List<DefaultTileDTO> getDefaultTileList() {
        return defaultTileList;
    }

    public void setDefaultTileList(List<DefaultTileDTO> defaultTileList) {
        this.defaultTileList = defaultTileList;
    }
}
