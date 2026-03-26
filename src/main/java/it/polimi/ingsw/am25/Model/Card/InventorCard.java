package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.INV_ICON;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.Objects;

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

    public INV_ICON getInvIcon() {
        return invIcon;
    }
    @Override
    public void addCardToPlayer(Player player) {
        player.addCardToTribe(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InventorCard that)) return false;
        return invIcon == that.invIcon && this.cardType == that.cardType && this.era == that.era;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(invIcon);
    }
}
