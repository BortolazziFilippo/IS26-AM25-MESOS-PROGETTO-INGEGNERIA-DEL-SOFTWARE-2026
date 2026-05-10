package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;

import java.io.Serial;
import java.io.Serializable;

/**
 * DTO for a Mesos card, carrying the type, era, and any role-specific attributes
 * (invention icon, shaman star, food discount, prestige points, hunter icon flag).
 */
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
    private int builderID;
    private int eventID;

    /**
     * Constructs a CardDTO from an {@link ArtistCard}.
     *
     * @param card the source ArtistCard.
     */
    public CardDTO(ArtistCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
    }

    /**
     * Constructs a CardDTO with only the era and card type (no role-specific attributes).
     *
     * @param era      the era this card belongs to.
     * @param cardType the type of this card.
     */
    public CardDTO(ERA era, CARD_TYPE cardType) {
        this.era = era;
        this.cardType = cardType;
    }

    /**
     * Constructs a CardDTO from a {@link GathererCard}.
     *
     * @param card the source GathererCard.
     */
    public CardDTO(GathererCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
    }

    /**
     * Constructs a CardDTO from a {@link HuntersCard}.
     * Carries the icon flag that determines whether the card grants a food bonus.
     *
     * @param card the source HuntersCard.
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
     * @param card the source ShamanCard.
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
     * @param card the source InventorCard.
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
     * @param card the source BuilderCard.
     */
    public CardDTO(BuilderCard card) {
        this.era = card.getEra();
        this.cardType = card.getCardType();
        this.foodDiscount = card.getFoodDiscount();
        this.finalPrestigePoint = card.getFinalPrestigePoint();
        this.builderID = card.getBuilderID();
    }

    /**
     * @return the type of this card (tribe role, building, or event).
     */
    public CARD_TYPE getCardType() {
        return cardType;
    }

    /**
     * @return the era this card belongs to.
     */
    public ERA getEra() {
        return era;
    }

    /**
     * @return the invention icon on this card, or {@code null} if not an inventor card.
     */
    public INV_ICON getInvIcon() {
        return invIcon;
    }

    /**
     * @return the shaman star value, or {@code null} if not a shaman card.
     */
    public SHAMAN_STAR getStarNumber() {
        return starNumber;
    }

    /**
     * @return the food discount this builder provides when buying a building.
     */
    public int getFoodDiscount() {
        return foodDiscount;
    }

    /**
     * @return the prestige points this builder awards at end of game.
     */
    public int getFinalPrestigePoint() {
        return finalPrestigePoint;
    }

    public int getBuilderID() {
        return builderID;
    }

    public int getEventID() {
        return eventID;
    }

    /**
     * @return whether this hunter card carries an icon (granting an immediate food bonus).
     */
    public boolean isHasIcon() {
        return hasIcon;
    }

    /**
     * Returns a human-readable string shown in the TUI describing the card's role-specific attributes.
     */
    @Override
    public String toString() {
        switch (cardType) {
            case SHAMAN:
                return this.starNumber.toString();
            case HUNTER:
                if (hasIcon) {
                    return "Con icona";

                } else {
                    return "Senza icona";

                }
            case BUILDER:
                return "PP: " + this.finalPrestigePoint + "Sconto: " + this.foodDiscount;

            case INVENTOR:
                return "Icona: " + this.invIcon;
            default:
                return this.cardType + " ";
        }
    }
}
