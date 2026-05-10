package it.polimi.ingsw.am25.server.model.Observers;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Player.Totem;

import java.util.List;

/**
 * Observer interface for player-level state changes in Mesos.
 * Implemented by {@link it.polimi.ingsw.am25.server.webLayer.ServerVirtualView} to forward
 * player events (food changes, card draws, extra-draw requests) to all connected clients.
 */
public interface PlayerObserver {

    /**
     * Called once at game start to push the full initial player state to the observer.
     *
     * @param nickname      the player's nickname.
     * @param totem         the player's totem.
     * @param food          the player's starting food total.
     * @param prestigePoint the player's starting prestige-point total.
     * @param tribe         the player's initial tribe (empty at game start).
     * @param buildingCards the player's initial buildings (empty at game start).
     */
    void onPlayerChanged(String nickname, Totem totem, int food, int prestigePoint,
                         List<Card> tribe, List<BuildingCard> buildingCards);

    /**
     * Called when the player's food total changes.
     *
     * @param playerNickName the affected player's nickname.
     * @param newFood        the player's new food total.
     */
    void notifyFoodChanged(String playerNickName, int newFood);

    /**
     * Called when the player's prestige-point total changes.
     *
     * @param playerNickName the affected player's nickname.
     * @param newPP          the player's new prestige-point total.
     */
    void notifyPPChanged(String playerNickName, int newPP);

    /**
     * Called when a card is added to the player's tribe.
     *
     * @param playername the affected player's nickname.
     * @param cardAdded  the card that was added.
     */
    void notifyCardAddedToTribe(String playername, Card cardAdded);

    /**
     * Called when the player must select one extra card from the market (draw-one-more building effect).
     *
     * @param nickname the affected player's nickname.
     */
    void requestExtraDraw(String nickname);
}
