package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Card.ShamanCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;

import java.io.Serial;
import java.io.Serializable;

public class CardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private  CARD_TYPE cardType;
    private  ERA era;
    private INV_ICON invIcon;
    private SHAMAN_STAR starNumber;
    private int foodDiscount;
    private int finalPrestigePoint;
    private boolean hasIcon;

    /**
     * Constructs a CardDTO from a generic Card.
     * Uses Java Pattern Matching to safely map specific fields.
     */
    public CardDTO(Card card) {
        //common fields
        this.era = card.getEra();
        this.cardType = card.getCardType();

        switch (card) {
            case HuntersCard hc -> this.hasIcon = hc.getHasICON();
            case ShamanCard sc -> this.starNumber = sc.getShamanStar();
            case InventorCard ic -> this.invIcon = ic.getInvIcon();
            case BuilderCard bc -> {
                this.foodDiscount = bc.getFoodDiscount();
                this.finalPrestigePoint = bc.getFinalPrestigePoint();
            }
            default -> {
                //Artist card don't need other parameters
            }
        }
    }


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
