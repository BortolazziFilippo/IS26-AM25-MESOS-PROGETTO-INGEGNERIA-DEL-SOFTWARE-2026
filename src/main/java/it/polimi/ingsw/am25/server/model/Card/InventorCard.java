package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents an Inventor tribe card. Each Inventor card carries an {@link it.polimi.ingsw.am25.server.model.Enums.INV_ICON};
 * having one of each distinct icon in the tribe grants a prestige-point bonus at end of game.
 */
public class InventorCard extends Card {
    private final INV_ICON invIcon;

    /**
     * @param era      the era this card belongs to.
     * @param cardType the card type (should be {@code CARD_TYPE.INVENTOR}).
     * @param invIcon  the invention icon carried by this card.
     */
    public InventorCard(ERA era, CARD_TYPE cardType, INV_ICON invIcon) {
        this.invIcon = invIcon;
        this.cardType = cardType;
        this.era = era;
    }

    /**
     * @return the invention icon carried by this card.
     */
    public INV_ICON getInvIcon() {
        return invIcon;
    }

    /**
     * Adds this card to the player's tribe.
     */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * @param o the object to compare against.
     * @return true if {@code o} is an InventorCard with the same era, card type, and icon.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InventorCard that)) return false;
        return invIcon == that.invIcon && this.cardType == that.cardType && this.era == that.era;
    }

    /**
     * @return a CardDTO snapshot of this card for network transfer.
     */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }

    /**
     * Checks whether this inventor card matches the given DTO by comparing card type, era, and invention icon.
     *
     * @param dto the DTO to compare against this card.
     * @return {@code true} if the DTO represents an InventorCard with the same era and invention icon.
     */
    @Override
    public boolean matchesDTO(CardDTO dto) {
        return dto.getCardType() == CARD_TYPE.INVENTOR
            && dto.getEra() == this.era
            && dto.getInvIcon() == this.invIcon;
    }
}
