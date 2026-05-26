package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

/**
 * Represents a Hunter tribe card. Hunters grant food and prestige points during hunt events.
 * Cards with an icon grant an immediate food bonus equal to the player's current hunter count
 * when added to the tribe.
 */
public class HuntersCard extends Card {

    private final boolean hasICON;

    /**
     * Creates a new HuntersCard for the specified era and card type.
     *
     * @param era      the era this card belongs to.
     * @param cardType the card type (should be {@code CARD_TYPE.HUNTER}).
     * @param hasICON  whether this card carries an icon granting an immediate food bonus.
     */
    public HuntersCard(ERA era, CARD_TYPE cardType, boolean hasICON) {
        this.era = era;
        this.cardType = cardType;
        this.hasICON = hasICON;
    }

    /**
     * Returns whether this card carries an icon granting an immediate food bonus.
     *
     * @return {@code true} if this card has an icon, {@code false} otherwise.
     */
    public boolean getHasICON() {
        return hasICON;
    }

    /**
     * Adds this card to the player's tribe.
     * If the card has an icon, immediately grants food equal to the player's current hunter count.
     */
    @Override
    public void addCardToPlayer(Player player) {
        if (this.hasICON) {
            player.manageFoodAndPP(player.getHunterNumber());
        }
        player.addCardToTribe(this);
    }

    /**
     * @param obj the object to compare against.
     * @return true if {@code obj} is a HuntersCard with the same era, card type, and icon flag.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HuntersCard toCompare) {
            return toCompare.era == this.era && toCompare.cardType == this.cardType && toCompare.hasICON == this.hasICON;
        } else {
            return false;
        }
    }

    /**
     * @return a CardDTO snapshot of this card for network transfer.
     */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }

    /**
     * Checks whether this hunter card matches the provided DTO by comparing card type, era, and the icon flag.
     *
     * @param dto the DTO to compare against this card.
     * @return {@code true} if the DTO represents a HuntersCard with the same era and icon flag.
     */
    @Override
    public boolean matchesDTO(CardDTO dto) {
        return dto.getCardType() == CARD_TYPE.HUNTER
            && dto.getEra() == this.era
            && dto.isHasIcon() == this.hasICON;
    }
}
