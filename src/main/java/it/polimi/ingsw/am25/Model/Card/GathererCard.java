package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;

public class GathererCard extends Card{
    /**
     * Default GathereCard constructor
     * @param era Card ERA
     * @param cardType Card type
     */
    public GathererCard(ERA era, CARD_TYPE cardType) {
        this.cardType=cardType;
        this.era=era;
    }
}
