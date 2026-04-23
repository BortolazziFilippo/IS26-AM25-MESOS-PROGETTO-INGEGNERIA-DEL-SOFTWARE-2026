package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;

import java.io.Serial;
import java.io.Serializable;

public class CardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final CARD_TYPE cardType;
    private final ERA era;
    private INV_ICON invIcon;
    private SHAMAN_STAR starNumber;
    private int foodDiscount;
    private int finalPrestigePoint;
    private boolean hasIcon;

    /**
     * Constructs a CardDTO from an {@link ArtistCard}.
     *
     * @param card the source ArtistCard
     */
    public CardDTO(ArtistCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
    }

    /**
     * Creates a new card dto instance.
     * @param era parameter era.
     * @param cardType parameter cardType.
     */
    public CardDTO(ERA era,CARD_TYPE cardType){
        this.era=era;
        this.cardType=cardType;
    }
    /**
     * Constructs a CardDTO from a {@link GathererCard}.
     *
     * @param card the source GathererCard
     */
    public CardDTO(GathererCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
    }

    /**
     * Constructs a CardDTO from a {@link HuntersCard}.
     * Carries the icon flag that determines whether the card grants a food bonus.
     *
     * @param card the source HuntersCard
     */
    public CardDTO(HuntersCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
        this.hasIcon = card.getHasICON();
    }

    /**
     * Constructs a CardDTO from a {@link ShamanCard}.
     * Carries the star value used to calculate prestige points.
     *
     * @param card the source ShamanCard
     */
    public CardDTO(ShamanCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
        this.starNumber = card.getShamanStar();
    }

    /**
     * Constructs a CardDTO from an {@link InventorCard}.
     * Carries the invention icon used for set-completion scoring.
     *
     * @param card the source InventorCard
     */
    public CardDTO(InventorCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
        this.invIcon = card.getInvIcon();
    }

    /**
     * Constructs a CardDTO from a {@link BuilderCard}.
     * Carries the food discount on buildings and the end-game prestige points.
     *
     * @param card the source BuilderCard
     */
    public CardDTO(BuilderCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
        this.foodDiscount = card.getFoodDiscount();
        this.finalPrestigePoint = card.getFinalPrestigePoint();
    }


    /**
     * Returns card type.
     * @return the result of the operation.
     */
    public CARD_TYPE getCardType() {
        return cardType;
    }

    /**
     * Returns era.
     * @return the result of the operation.
     */
    public ERA getEra() {
        return era;
    }

    /**
     * Returns inv icon.
     * @return the result of the operation.
     */
    public INV_ICON getInvIcon() {
        return invIcon;
    }

    /**
     * Returns star number.
     * @return the result of the operation.
     */
    public SHAMAN_STAR getStarNumber() {
        return starNumber;
    }

    /**
     * Returns food discount.
     * @return the result of the operation.
     */
    public int getFoodDiscount() {
        return foodDiscount;
    }

    /**
     * Returns final prestige point.
     * @return the result of the operation.
     */
    public int getFinalPrestigePoint() {
        return finalPrestigePoint;
    }

    /**
     * Checks whether has icon.
     * @return the result of the operation.
     */
    public boolean isHasIcon() {
        return hasIcon;
    }

    /**
     * Executes to string.
     * @return the result of the operation.
     */
    @Override
    public String toString() {
        switch (cardType){
            case SHAMAN:
                return this.starNumber.toString();
            case HUNTER:
                if(hasIcon){
                    return "Con icona";

                }else{
                    return "Senza icona";

                }
            case BUILDER:
                return "PP: "+this.finalPrestigePoint + "Sconto: "+this.foodDiscount;

            case INVENTOR:
                return "Icona: "+this.invIcon;
            default:
                return this.cardType +" ";
        }
    }
}
