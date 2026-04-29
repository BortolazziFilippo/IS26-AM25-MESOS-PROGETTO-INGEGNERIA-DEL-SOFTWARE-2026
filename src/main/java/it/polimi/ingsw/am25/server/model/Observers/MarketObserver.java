package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

import java.util.List;

/**
 * Observer interface for market-level state changes in Mesos.
 * Implemented by {@link it.polimi.ingsw.am25.server.webLayer.ServerVirtualView} to forward
 * market events (card draws, row refreshes) to all connected clients.
 */
public interface MarketObserver {
    /**
     * Called once at game start to push the full initial market state to the observer.
     * @param topCards the initial top card row.
     * @param bottomCards the initial bottom card row.
     * @param topBuildings the initial top building row.
     * @param bottomBuildings the initial bottom building row.
     */
    void onMarketChanged(List<Card> topCards, List<Card> bottomCards,
                         List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings);

    /**
     * Called when the top card row is refreshed (old top becomes new bottom, new top is dealt).
     * @param topCards the new top card row.
     */
    void onTopCardRefreshed(List<Card> topCards);

    /**
     * Called when the top building row is refreshed.
     * @param topCards the new top building row.
     */
    void onTopBuildingRefreshed(List<BuildingCard> topCards);

    void eventSolved(int eventID, EVENT_TYPE eventType);

    /**
     * Called when a card is removed from the top market row.
     * @param position zero-based index of the removed card.
     * @param cardType the type of card that was removed.
     */
    void onCardRemovedFromTop(int position, CARD_TYPE cardType);

    /**
     * Called when a card is removed from the bottom market row.
     * @param position zero-based index of the removed card.
     * @param cardType the type of card that was removed.
     */
    void onCardRemovedFromBottom(int position, CARD_TYPE cardType);
}