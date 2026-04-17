package it.polimi.ingsw.am25.server.webLayer.DTOs;

import java.io.Serial;
import java.io.Serializable;

public class DefaultTileDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int foodPerSlotPosition;

    public DefaultTileDTO(int foodPerSlotPosition) {
        this.foodPerSlotPosition = foodPerSlotPosition;
    }
}
