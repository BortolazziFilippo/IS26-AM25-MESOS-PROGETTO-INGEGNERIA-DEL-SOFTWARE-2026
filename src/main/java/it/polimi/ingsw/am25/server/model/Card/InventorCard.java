package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

public class InventorCard extends Card{
    private final INV_ICON invIcon;

    /**
     * Default constructor of inventorCard
     * @param invIcon Type of icon the inventorCard has
     * @param era Card ERA
     * @param cardType Card type
     */
    public InventorCard( ERA era, CARD_TYPE cardType,INV_ICON invIcon){
        this.invIcon=invIcon;
        this.cardType=cardType;
        this.era=era;
    }

    /**
     * Returns inv icon.
     * @return the result of the operation.
     */
    public INV_ICON getInvIcon() {
        return invIcon;
    }
    /**
     * Executes add card to player.
     * @param player parameter player.
     */
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    /**
     * Executes equals.
     * @param o parameter o.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InventorCard that)) return false;
        return invIcon == that.invIcon && this.cardType == that.cardType && this.era == that.era;
    }
    /**
     * Executes to dto.
     * @return the result of the operation.
     */
    @Override
    public CardDTO toDTO() {
        return new CardDTO(this);
    }

}
