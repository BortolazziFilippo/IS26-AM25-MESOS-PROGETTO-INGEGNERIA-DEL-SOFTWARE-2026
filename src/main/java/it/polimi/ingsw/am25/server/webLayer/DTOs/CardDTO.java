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
    /** The type of this card (tribe role, building, or event). */
    private final CARD_TYPE cardType;
    /** The era this card belongs to. */
    private final ERA era;
    /** The invention icon; set only for inventor cards. */
    private INV_ICON invIcon;
    /** The shaman star value; set only for shaman cards. */
    private SHAMAN_STAR starNumber;
    /** Food discount when buying a building; set only for builder cards. */
    private int foodDiscount;
    /** End-game prestige points; set only for builder cards. */
    private int finalPrestigePoint;
    /** Whether this hunter card carries an icon granting an immediate food bonus. */
    private boolean hasIcon;
    /** The builder ID shared by cards with the same effect; set only for builder cards. */
    private int builderID;
    /** The unique event identifier; only meaningful for {@link it.polimi.ingsw.am25.server.model.Card.EventCard} DTOs. */
    protected int eventID;

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
     * Constructs a CardDTO from {@link EventCard}.
     *
     * @param era      the era this card belongs to.
     * @param eventID  the unique event identifier.
     * @param cardType the type of this card.
     */
    public CardDTO(ERA era, int eventID,CARD_TYPE cardType) {
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
     * Returns the type of this card (tribe role, building, or event).
     *
     * @return the card type.
     */
    public CARD_TYPE getCardType() {
        return cardType;
    }

    /**
     * Returns the era this card belongs to.
     *
     * @return the era.
     */
    public ERA getEra() {
        return era;
    }

    /**
     * Returns the invention icon on this card.
     *
     * @return the invention icon, or {@code null} if not an inventor card.
     */
    public INV_ICON getInvIcon() {
        return invIcon;
    }

    /**
     * Returns the shaman star value of this card.
     *
     * @return the shaman star value, or {@code null} if not a shaman card.
     */
    public SHAMAN_STAR getStarNumber() {
        return starNumber;
    }

    /**
     * Returns the food discount this builder provides when buying a building.
     *
     * @return the food discount, or {@code 0} if not a builder card.
     */
    public int getFoodDiscount() {
        return foodDiscount;
    }

    /**
     * Returns the prestige points this builder awards at end of game.
     *
     * @return the end-game prestige points, or {@code 0} if not a builder card.
     */
    public int getFinalPrestigePoint() {
        return finalPrestigePoint;
    }

    /**
     * Returns the unique identifier of the builder associated with this card.
     *
     * @return the builder ID, or {@code 0} if the card is not of builder type.
     */
    public int getBuilderID() {
        return builderID;
    }

    /**
     * Returns the identifier of the event associated with this event card.
     *
     * @return the event ID, or {@code 0} if the card is not of event type.
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * Returns whether this hunter card carries an icon, which grants an immediate food bonus.
     *
     * @return {@code true} if this hunter card has an icon, {@code false} otherwise.
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
