package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;

import java.util.List;

public interface MarketObserver {
    void onMarketChanged(
            List<Card> topCards,
            List<Card> bottomCards,
            List<BuildingCard> topBuildings,
            List<BuildingCard> bottomBuildings
    );
    void onTopCardRefreshed(
            List<Card> topCards
            //bottom cards==topcards
    );

    void onTopBuildingRefreshed(
            List<BuildingCard> topCards
    );
    void onCardRemovedFromTop(
      int position,
      CARD_TYPE cardType
    );

    void onCardRemovedFromBottom(
            int position,
            CARD_TYPE cardType
    );


}