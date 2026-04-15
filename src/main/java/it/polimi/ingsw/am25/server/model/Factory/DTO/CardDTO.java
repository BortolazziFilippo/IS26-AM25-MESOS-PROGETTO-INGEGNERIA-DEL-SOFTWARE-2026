package it.polimi.ingsw.am25.server.model.Factory.DTO;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;

public class CardDTO {
    private  CARD_TYPE cardType;
    private  ERA era;
    private INV_ICON invIcon;
    private SHAMAN_STAR starNumber;
    private int foodDiscount;
    private int finalPrestigePoint;
    private boolean hasIcon;

    public CARD_TYPE getCardType() {
        return cardType;
    }

    public ERA getEra() {
        return era;
    }

    public INV_ICON getInvIcon() {
        return invIcon;
    }

    public SHAMAN_STAR getStarNumber() {
        return starNumber;
    }

    public int getFoodDiscount() {
        return foodDiscount;
    }

    public int getFinalPrestigePoint() {
        return finalPrestigePoint;
    }

    public boolean isHasIcon() {
        return hasIcon;
    }
}
