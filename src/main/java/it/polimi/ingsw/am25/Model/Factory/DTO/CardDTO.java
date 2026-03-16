package it.polimi.ingsw.am25.Model.Factory.DTO;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.INV_ICON;
import it.polimi.ingsw.am25.Model.Enums.SHAMAN_STAR;

public class CardDTO {
    private CARD_TYPE cardType;
    private ERA era;
    private INV_ICON invIcon;
    private SHAMAN_STAR starNumber;
    private int foodDiscount;
    private int finalPrestigePoint;
    private boolean hasIcon;

    /**
     * constructor for gatherer e artist
     * @param cardType type of card
     * @param era era of the card
     */
    public CardDTO(ERA era,CARD_TYPE cardType) {
        this.cardType = cardType;
        this.era = era;
    }

    /**
     * constructor for hunter
     * @param cardType type of card
     * @param hasIcon icon hunter
     * @param era era of the card
     */
    public CardDTO( ERA era,CARD_TYPE cardType, boolean hasIcon) {
        this.cardType = cardType;
        this.hasIcon = hasIcon;
        this.era = era;
    }

    /**
     * constructor for Builder
     * @param cardType type of card
     * @param era era of the card
     * @param foodDiscount discount builder
     * @param finalPrestigePoint endgmame builder pp
     */
    public CardDTO(ERA era,CARD_TYPE cardType,  int foodDiscount, int finalPrestigePoint) {
        this.cardType = cardType;
        this.era = era;
        this.foodDiscount = foodDiscount;
        this.finalPrestigePoint = finalPrestigePoint;
    }

    /**
     * constructor for shaman
     * @param era era of the card
     * @param cardType type of card
     * @param starNumber number of star
     */
    public CardDTO(ERA era, CARD_TYPE cardType, SHAMAN_STAR starNumber) {
        this.era = era;
        this.cardType = cardType;
        this.starNumber = starNumber;
    }

    /**
     * constructor for inventor
     * @param era era of the card
     * @param cardType type of card
     * @param invIcon inventor icon
     */
    public CardDTO(ERA era,CARD_TYPE cardType,  INV_ICON invIcon) {
        this.cardType = cardType;
        this.era = era;
        this.invIcon = invIcon;
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
