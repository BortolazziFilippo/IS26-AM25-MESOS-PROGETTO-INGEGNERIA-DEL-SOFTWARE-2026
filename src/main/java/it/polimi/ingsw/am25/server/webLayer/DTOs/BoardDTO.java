package it.polimi.ingsw.am25.server.webLayer.DTOs;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class BoardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<OffertileDTO> offerTileList;
    private List<DefaultTileDTO> defaultTileList;

    /**
     * Creates a new board dto instance.
     * @param offerTileList parameter offerTileList.
     * @param defaultTileList parameter defaultTileList.
     */
    public BoardDTO(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) {
        this.offerTileList = offerTileList;
        this.defaultTileList = defaultTileList;
    }

    /**
     * Returns offer tile list.
     * @return the result of the operation.
     */
    public List<OffertileDTO> getOfferTileList() {
        return offerTileList;
    }

    /**
     * Sets offer tile list.
     * @param offerTileList parameter offerTileList.
     */
    public void setOfferTileList(List<OffertileDTO> offerTileList) {
        this.offerTileList = offerTileList;
    }

    /**
     * Returns default tile list.
     * @return the result of the operation.
     */
    public List<DefaultTileDTO> getDefaultTileList() {
        return defaultTileList;
    }

    /**
     * Sets default tile list.
     * @param defaultTileList parameter defaultTileList.
     */
    public void setDefaultTileList(List<DefaultTileDTO> defaultTileList) {
        this.defaultTileList = defaultTileList;
    }
}
