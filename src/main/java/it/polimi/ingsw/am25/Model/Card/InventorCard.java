package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.INV_ICON;

public class InventorCard extends Card{
    private final INV_ICON invIcon;
    InventorCard(INV_ICON invIcon, ERA era){
        this.invIcon=invIcon;
        this.era=era;
    }

    public INV_ICON getInvIcon() {
        return invIcon;
    }
}
