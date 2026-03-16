package it.polimi.ingsw.am25.Model.Card;

import it.polimi.ingsw.am25.Model.Effect.Event.EventEffect;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class EventCard extends Card
{
    private int eventID;
    private EVENT_TYPE eventType;
    private EventEffect eventEffect;

    public EventCard(ERA era, CARD_TYPE cardType, int eventID, EVENT_TYPE eventType, EventEffect eventEffect) {
        this.cardType=cardType;
        this.era = era;
        this.eventID = eventID;
        this.eventType = eventType;
        this.eventEffect = eventEffect;
    }
    public void applyEventEffect(List<Player> PlayersList)
    {
        this.eventEffect.solveEvent(PlayersList);
    }
}
