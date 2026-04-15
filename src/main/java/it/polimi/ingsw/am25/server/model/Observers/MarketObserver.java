package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;

import java.util.List;

public interface MarketObserver {
    void onMarketChanged(
            List<Card> topCards,
            List<Card> bottomCards,
            List<BuildingCard> topBuildings,
            List<BuildingCard> bottomBuildings
    );
}