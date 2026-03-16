package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.INV_ICON;

public class InventorCard extends Card{
    private final INV_ICON invIcon;
    public InventorCard(INV_ICON invIcon, ERA era, CARD_TYPE cardType){
        this.invIcon=invIcon;
        this.cardType=cardType;
        this.era=era;
    }

    public INV_ICON getInvIcon() {
        return invIcon;
    }
}
