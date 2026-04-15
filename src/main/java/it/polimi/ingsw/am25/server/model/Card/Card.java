package it.polimi.ingsw.am25.server.model.Card;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotSelectableCardException;

public abstract class Card {
    protected ERA era;
    protected CARD_TYPE cardType;

    public ERA getEra() {
        return era;
    }
    public CARD_TYPE getCardType(){
        return cardType;
    }
    public abstract void addCardToPlayer(Player player) throws NotSelectableCardException;
}
