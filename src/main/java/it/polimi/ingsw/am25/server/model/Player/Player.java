package it.polimi.ingsw.am25.server.model.Player;

import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Enums.*;
import it.polimi.ingsw.am25.server.model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotEnoughFoodException;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.model.persistance.MementoManager;
import it.polimi.ingsw.am25.server.model.persistance.PlayerMemento;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a Mesos player. Holds the player's nickname, totem, food total,
 * prestige-point total, tribe cards, and buildings. Notifies registered
 * {@link PlayerObserver}s on every state change.
 */
public class Player implements MementoManager<PlayerMemento> {
    private static final String LOG_PREFIX = "[SERVER][PLAYER]";
    private final String nickname;
    private final Totem totem;
    private int food;
    private int prestigePoint;
    private final List<Card> tribe;
    private final List<BuildingCard> buildingCards;
    private CONNECTION_STATUS connectionStatus;
    private final List<PlayerObserver> observers = new ArrayList<>();


    /**
     * Returns the player's nickname.
     *
     * @return nickname.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Builds a player with the provided nickname and totem color.
     *
     * @param nickname player's nickname
     * @param color    totem color
     */
    public Player(String nickname, COLOR color) {
        this.nickname = nickname;
        this.food = 0;
        this.prestigePoint = 0;
        this.tribe = new ArrayList<>();
        this.buildingCards = new ArrayList<>();
        this.totem = new Totem(color);
    }

    /**
     * Builds a player and subscribes the provided virtual view as observer.
     *
     * @param nickname    player's nickname
     * @param color       totem color
     * @param virtualView observer to bind
     */
    public Player(String nickname, COLOR color, ServerVirtualView virtualView) {
        this(nickname, color);
        addObserver(virtualView);
        notifyPlayerChanged();
    }

    /**
     * Builds a player from a DTO snapshot.
     *
     * @param playerDTO player data transfer object
     */
    public Player(PlayerDTO playerDTO) {
        this.nickname = playerDTO.getNickName();
        this.food = 0;
        this.prestigePoint = 0;
        this.tribe = new ArrayList<>();
        this.buildingCards = new ArrayList<>();
        this.totem = new Totem(playerDTO.getColorTotem());
    }

    /**
     * Updates the player's food amount.
     * If food would go below zero, the deficit is converted into prestige-point loss
     * at a ratio of 2 PP per missing food, then food is clamped to zero.
     *
     * @param foodAmount delta to apply to food (positive or negative)
     */
    public void manageFoodAndPP(int foodAmount) {
        int previousFood = this.food;
        this.food += foodAmount;
        if (this.food < 0) {
            managePP(this.food * 2);
            this.food = 0;
        }
        logServerEvent(
                "Updated food for player '" + nickname + "': " + previousFood + " -> " + this.food +
                        " (delta " + foodAmount + ")"
        );
        notifyFoodChanged();
    }

    /**
     * Attempts to purchase a building card.
     *
     * @param selectedBuildingCard building to buy
     * @throws NotEnoughFoodException if the player cannot afford the card
     */
    public void tryBuyBuilding(BuildingCard selectedBuildingCard) throws NotEnoughFoodException {
        int originalCost = selectedBuildingCard.getFoodCost();
        int cost = Math.max(0, originalCost - this.getBuilderDiscount());
        if (this.food - cost < 0) {
            throw new NotEnoughFoodException();
        }
        this.food -= cost;
        selectedBuildingCard.addCardToPlayer(this);
        logServerEvent(
                "Player '" + nickname + "' bought building #" + selectedBuildingCard.getBuildingID() +
                        " (cost " + originalCost + ", discounted to " + cost + ")"
        );
        notifyFoodChanged();
    }

    /**
     * Updates player's prestige points.
     *
     * @param PPamount delta to apply (positive or negative)
     */
    public void managePP(int PPamount) {
        int previousPP = this.prestigePoint;
        this.prestigePoint += PPamount;
        logServerEvent(
                "Updated prestige points for player '" + nickname + "': " + previousPP + " -> " + this.prestigePoint +
                        " (delta " + PPamount + ")"
        );
        notifyPPChanged();
    }

    /**
     * Returns the current connection status of the player.
     *
     * @return the player's {@link CONNECTION_STATUS}
     */
    public CONNECTION_STATUS getConnection() {
        return connectionStatus;
    }

    /**
     * Sets the connection status of the player.
     *
     * @param connectionStatus the new {@link CONNECTION_STATUS}
     */
    public void setConnection(CONNECTION_STATUS connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    /**
     * Adds a non-building card to the player's tribe.
     *
     * @param card card to add
     */
    public void addCardToTribe(Card card) {
        this.tribe.add(card);
        logServerEvent("Added " + formatCardForLog(card) + " to player '" + nickname + "'");
        notifyCardAdded(card);
    }

    /**
     * Adds a building card to the player's building area.
     *
     * @param buildingCard building card to add
     */
    public void addBuilding(BuildingCard buildingCard) {
        this.buildingCards.add(buildingCard);
        logServerEvent("Added building #" + buildingCard.getBuildingID() + " to player '" + nickname + "'");
        notifyCardAdded(buildingCard);
    }

    /**
     * Returns the player's current food amount.
     *
     * @return food amount
     */
    public int getFood() {
        return food;
    }

    /**
     * Returns the player's current prestige-point total.
     *
     * @return prestige points
     */
    public int getPrestigePoint() {
        return prestigePoint;
    }

    /**
     * Returns the number of distinct inventor icons in the player's tribe.
     *
     * @return count of unique {@link INV_ICON} values
     */
    public int getNumberOfDifferentInventorIcon() {
        return (int) tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.INVENTOR)
                .map(InventorCard.class::cast)
                .map(InventorCard::getInvIcon)
                .distinct()
                .count();
    }

    /**
     * Returns the total number of Shaman stars across all Shaman cards in the tribe.
     *
     * @return the sum of star values for all Shaman cards.
     */
    public int getShamanStarTotal() {
        return tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.SHAMAN)
                .mapToInt(card -> ((ShamanCard) card).getStarNumber())
                .sum();
    }

    /**
     * Returns the total food discount granted by all Builder cards in the tribe.
     *
     * @return the sum of food discounts across all Builder cards.
     */
    public int getBuilderDiscount() {
        return this.tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.BUILDER)
                .map(BuilderCard.class::cast)
                .mapToInt(BuilderCard::getFoodDiscount)
                .sum();
    }

    /**
     * Returns the food penalty reduction granted by Gatherer cards during a sustenance event (3 per Gatherer).
     *
     * @return the total food discount from Gatherer cards.
     */
    public int getGatherDiscount() {
        return (int) this.tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.GATHERER)
                .count() * 3;
    }

    /**
     * Returns the number of Hunter cards in the player's tribe.
     *
     * @return the hunter count.
     */
    public int getHunterNumber() {
        return (int) tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.HUNTER)
                .count();
    }

    /**
     * Returns the number of Artist cards in the player's tribe.
     *
     * @return the artist count.
     */
    public int getArtistNumber() {
        return (int) this.tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.ARTIST)
                .count();
    }

    /**
     * Returns the total number of cards in the player's tribe.
     *
     * @return the tribe size.
     */
    public int getNumberOfCard() {
        return this.tribe.size();
    }

    /**
     * Triggers all end-round building effects owned by this player.
     */
    public void triggerEndRoundBuilding() {
        this.buildingCards.stream()
                .filter(buildingCard -> buildingCard.getApplyOn() == EVENT_TYPE.END_ROUND)
                .forEach(buildingCard -> buildingCard.applyBuildingEffect(this));
        notifyPlayerChanged();
    }

    /**
     * Triggers all end-game building effects owned by this player.
     */
    public void triggerEndGameBuilding() {
        this.buildingCards
                .stream()
                .filter(buildingCard -> buildingCard.getApplyOn() == EVENT_TYPE.END_GAME)
                .forEach(buildingCard -> buildingCard.applyBuildingEffect(this));
        notifyPlayerChanged();
    }

    /**
     * Returns all cards in the player's tribe.
     *
     * @return the tribe card list.
     */
    public List<Card> getTribe() {
        return tribe;
    }

    /**
     * Returns the player's totem.
     *
     * @return totem.
     */
    public Totem getTotem() {
        return totem;
    }

    /**
     * Returns the list of building cards the player owns.
     *
     * @return building card list
     */

    public List<BuildingCard> getBuildingCards() {
        return buildingCards;
    }

    /**
     * Subscribes an observer.
     *
     * @param observerToAdd observer to subscribe
     */
    public void addObserver(PlayerObserver observerToAdd) {
        if (observerToAdd != null && !observers.contains(observerToAdd)) {
            observers.add(observerToAdd);
        }
    }

    /**
     * Returns the list of subscribed observers.
     *
     * @return observer list.
     */
    public List<PlayerObserver> getObservers() {
        return this.observers;
    }

    /**
     * Unsubscribes an observer.
     *
     * @param observerToRemove observer to unsubscribe
     */
    public void removeObserver(PlayerObserver observerToRemove) {
        observers.remove(observerToRemove);
    }

    private void notify(Consumer<PlayerObserver> action) {
        for (PlayerObserver observer : observers) {
            action.accept(observer);
        }
    }

    private void notifyPlayerChanged() {
        List<Card> tribeSnapshot = List.copyOf(this.tribe);
        List<BuildingCard> buildingSnapshot = List.copyOf(this.buildingCards);
        notify(o -> o.onPlayerChanged(this.nickname, this.totem, this.food, this.prestigePoint, tribeSnapshot, buildingSnapshot));
    }

    private void notifyFoodChanged() {
        notify(o -> o.notifyFoodChanged(this.nickname, food));
    }

    private void notifyPPChanged() {
        notify(o -> o.notifyPPChanged(this.nickname, prestigePoint));
    }

    private void notifyCardAdded(Card cardAdded) {
        notify(o -> o.notifyCardAddedToTribe(this.nickname, cardAdded));
    }

    /**
     * Re-fires notifyCardAddedToTribe for every card currently in this player's tribe.
     * Used when resuming a saved game so observers that registered after the cards were
     * originally drawn can still receive the full tribe state.
     */
    public void notifyCurrentTribe() {
        for (Card card : tribe) {
            notifyCardAdded(card);
        }
        for (BuildingCard building : buildingCards) {
            notifyCardAdded(building);
        }
    }

    private String formatCardForLog(Card card) {
        if (card instanceof BuildingCard buildingCard) {
            return "building #" + buildingCard.getBuildingID();
        }
        return card.getCardType() + " card (" + card.getEra() + ")";
    }

    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }

    /**
     * Two players are equal if they share the same nickname and totem.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Player player)) return false;
        return Objects.equals(nickname, player.nickname) && totem.equals(player.totem);
    }

    /**
     * Requests an extra card draw on behalf of this player (triggered by a building effect).
     */
    public void requestExtraDraw() {
        notify(observer -> observer.requestExtraDraw(this.nickname));
    }

    /**
     * Computes the end-game prestige points earned from tribe cards:
     * artists (every 2 = 10 PP), builders (fixed PP per card), and inventors
     * (count × number of distinct inventor icons).
     *
     * @return total end-game prestige points from tribe cards.
     */
    public int checkpoints() {
        int artistPoints = (getArtistNumber() / 2) * 10;

        int builderPoints = this.tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.BUILDER)
                .mapToInt(card -> ((BuilderCard) card).getFinalPrestigePoint())
                .sum();

        long inventorCount = this.tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.INVENTOR)
                .count();
        int inventorPoints = (int) (inventorCount * getNumberOfDifferentInventorIcon());

        return artistPoints + builderPoints + inventorPoints;
    }

    /**
     * Creates a memento capturing the current player state: nickname, totem colour,
     * food amount, prestige points, tribe serialised as DTOs, and owned building IDs.
     *
     * @return a {@link PlayerMemento} representing the player snapshot.
     */
    @Override
    public PlayerMemento createMemento() {
        logServerEvent("Creating memento for player '" + nickname + "' (food=" + food + ", pp=" + prestigePoint + ", tribe=" + tribe.size() + ", buildings=" + buildingCards.size() + ")");
        return new PlayerMemento(
                this.nickname,
                this.totem.color(),
                this.food,
                this.prestigePoint,
                this.tribe.stream().map(Card::toDTO).toList(),
                this.buildingCards.stream().map(BuildingCard::getBuildingID).toList()
        );
    }

    /**
     * Restores the player state from the provided memento: food, prestige points, and tribe.
     * Buildings are restored separately via {@link #restoreBuildings(List)}.
     *
     * @param memento the {@link PlayerMemento} from which to restore the player state.
     */
    @Override
    public void restoreMemento(PlayerMemento memento) {
        logServerEvent("Restoring memento for player '" + nickname + "' (food=" + memento.food() + ", pp=" + memento.prestigePoints() + ", tribe=" + memento.tribe().size() + ")");
        this.food = memento.food();
        this.prestigePoint = memento.prestigePoints();
        this.tribe.clear();
        this.tribe.addAll(new DeckFactory().loadDeck(memento.tribe()));
    }

    /**
     * Restores the player's building cards. Called by Game.restoreMemento after
     * building cards have been recreated via BuildingFactory.
     *
     * @param buildings the fully initialised building cards to assign to this player.
     */
    public void restoreBuildings(List<BuildingCard> buildings) {
        this.buildingCards.clear();
        this.buildingCards.addAll(buildings);
    }
}
