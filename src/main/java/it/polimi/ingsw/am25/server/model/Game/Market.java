package it.polimi.ingsw.am25.server.model.Game;

import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Factory.Building.BuildingFactory;
import it.polimi.ingsw.am25.server.model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.server.model.Observers.MarketObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.model.persistance.MarketMemento;
import it.polimi.ingsw.am25.server.model.persistance.MementoManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages the Mesos card and building market. Holds the top and bottom card rows,
 * the top building row, and the underlying deck. Notifies registered
 * {@link MarketObserver}s on every change (draws, refreshes, event resolutions).
 */
public class Market implements MementoManager<MarketMemento> {
    private List<Card> topCardList;
    private List<BuildingCard> topBuildingList;
    private List<Card> bottomCardList;
    private List<BuildingCard> bottomBuildingList;
    private List<Card> deck;
    private List<BuildingCard> buildingCards;
    private final GameView gameView;
    private final BoardView boardView;
    private final List<MarketObserver> observers = new ArrayList<>();
    private List<Card> extraDrawCardSnapshot = new ArrayList<>();
    private List<BuildingCard> extraDrawBuildingSnapshot = new ArrayList<>();
    private final String LOG_PREFIX="[SERVER][MARKET]";

    /**
     * Initializes the market by building the deck and buildings for the given player count,
     * then populates both the bottom and top card/building rows.
     *
     * @param gameView  read-only view of the game, used to determine the player count.
     * @param boardView read-only view of the board, passed to the building factory.
     */
    public Market(GameView gameView, BoardView boardView) {
        this.topCardList = new ArrayList<>();
        this.bottomBuildingList = new ArrayList<>();
        this.bottomCardList = new ArrayList<>();
        this.topBuildingList = new ArrayList<>();
        this.gameView = gameView;
        this.boardView = boardView;
        this.buildingCards = new BuildingFactory().createBuildingDeck(gameView.getPlayerNumber(), boardView);
        this.deck = new DeckFactory().createDeck(gameView.getPlayerNumber());
        this.organizeDeck();
        this.initializeBottomList();
        this.initializeBothTopList();
        notifyMarketChanged();
    }


    /**
     * Returns the list of building cards available in the bottom (previous-round) building row.
     *
     * @return bottom building list
     */
    public List<BuildingCard> getBottomBuildingList() {
        return bottomBuildingList;
    }

    /**
     * Returns the list of building cards available in the top (current-round) building row.
     *
     * @return top building list
     */
    public List<BuildingCard> getTopBuildingList() {
        return topBuildingList;
    }

    /**
     * Returns the list of cards (villagers and events) in the bottom (previous-round) row.
     *
     * @return bottom card list
     */
    public List<Card> getBottomCardList() {
        return bottomCardList;
    }

    /**
     * Returns the list of cards (villagers and events) in the top (current-round) row.
     *
     * @return top card list
     */
    public List<Card> getTopCardList() {
        return topCardList;
    }

    /**
     * Clears all cards from the bottom card row.
     */
    public void clearBottomCardList() {
        if (bottomCardList == null) {
            throw new IllegalStateException("bottomCardList has not been initialized yet");
        }
        this.bottomCardList.clear();
    }

    /**
     * Captures the current top card row and top building row into snapshot lists that
     * will be used by the draw-one-more mechanic. Must be called before
     * {@link #endOfRoundMarketActions()} so the snapshot reflects the round that just ended,
     * not the next one.
     */
    public void snapshotForExtraDraw() {
        extraDrawCardSnapshot = List.copyOf(topCardList);
        extraDrawBuildingSnapshot = List.copyOf(topBuildingList);
        notifyExtraDrawSnapshotReady();
    }

    /**
     * Draws a tribe card from the end-of-round snapshot (not the current top row) and adds it
     * to the player's tribe.
     */
    public void selectExtraCardFromSnapshot(int position, Player player)
            throws NotSelectableCardException, IndexOutOfBoundsException, EmptyMarketException {
        if (extraDrawCardSnapshot == null || player == null) {
            throw new IllegalArgumentException("extraDrawCardSnapshot is null or player is null");
        }
        if (extraDrawCardSnapshot.isEmpty()
                || extraDrawCardSnapshot.stream().allMatch(c -> c.getCardType() == CARD_TYPE.EVENT)) {
            throw new EmptyMarketException("No cards available in extra draw snapshot");
        }
        if (position < 0 || position >= extraDrawCardSnapshot.size()) {
            throw new IndexOutOfBoundsException("Invalid position");
        }
        Card selected = extraDrawCardSnapshot.get(position);
        try {
            selected.addCardToPlayer(player);
        } catch (NotSelectableCardException e) {
            throw new NotSelectableCardException("Cannot select EventCard");
        }
        extraDrawCardSnapshot = new ArrayList<>(extraDrawCardSnapshot);
        extraDrawCardSnapshot.remove(position);
        // After endOfRoundMarketActions() the snapshot cards have shifted to bottomCardList.
        // Find the card by reference and remove it from whichever live list it is in now.
        int bottomPos = bottomCardList.indexOf(selected);
        if (bottomPos >= 0) {
            bottomCardList.remove(bottomPos);
            notifyCardRemovedFromBottom(bottomPos, CARD_TYPE.ARTIST);
        } else {
            // endOfRoundMarketActions not yet executed (edge case): card still in top list
            int topPos = topCardList.indexOf(selected);
            if (topPos >= 0) {
                topCardList.remove(topPos);
                notifyCardRemoveFromTop(topPos, CARD_TYPE.ARTIST);
            }
        }
    }

    /**
     * Buys a building from the end-of-round snapshot (not the current top row) and adds it
     * to the player's buildings.
     */
    public void buyExtraBuildingFromSnapshot(int position, Player player)
            throws NotEnoughFoodException, IndexOutOfBoundsException, EmptyMarketException {
        if (extraDrawBuildingSnapshot.isEmpty()) {
            throw new EmptyMarketException("No buildings available in extra draw snapshot");
        }
        if (position < 0 || position >= extraDrawBuildingSnapshot.size()) {
            throw new IndexOutOfBoundsException("Invalid position");
        }
        BuildingCard selected = extraDrawBuildingSnapshot.get(position);
        try {
            player.tryBuyBuilding(selected);
        } catch (NotEnoughFoodException e) {
            throw new NotEnoughFoodException(player.getNickname() + " has not enough food");
        }
        extraDrawBuildingSnapshot = new ArrayList<>(extraDrawBuildingSnapshot);
        extraDrawBuildingSnapshot.remove(position);
        // Buildings stay in topBuildingList unless an era change happened during endOfRoundMarketActions,
        // in which case they shifted to bottomBuildingList. Search by reference in both.
        int topPos = topBuildingList.indexOf(selected);
        if (topPos >= 0) {
            topBuildingList.remove(topPos);
            notifyCardRemoveFromTop(topPos, CARD_TYPE.BUILDING);
        } else {
            int bottomPos = bottomBuildingList.indexOf(selected);
            if (bottomPos >= 0) {
                bottomBuildingList.remove(bottomPos);
                notifyCardRemovedFromBottom(bottomPos, CARD_TYPE.BUILDING);
            }
        }
    }


    /**
     * this method does all the things that has to be done at the end of the round in the market area:
     * 1)if there are solves the events in the bottom list
     * 2)clear the bottom list
     * 3)shift the remaining card from the top list to the bottom list
     * 4) refill the top list
     * 5)Try refilling the topCardList
     * 5.a) if an ERA change is detected it:
     * 5.1) clears the bottomBuildingList
     * 5.2)shift the building from the top to bottom building list
     * 5.3) refill the topBuildingList
     * 5.b) if the deck is finished throws DeckFineshed7
     *
     * @throws DeckFinishedException int the case the deck is finished it notifies the caller
     */
    public void endOfRoundMarketActions() throws DeckFinishedException {
        this.solveEvents();
        this.clearBottomCardList();
        this.shiftCardTopToBottomList();
        try {
            this.refillTopCardList();
            notifyTopCardRefreshed();
        } catch (ChangedEraException e) {
            notifyTopCardRefreshed();
            this.clearBottomBuildingList();
            this.shiftBuildingTopToBottom();
            this.refillTopBuildingList();
            notifyTopBuildingRefreshed();

        } catch (DeckFinishedException e) {
            notifyTopCardRefreshed();
            throw new DeckFinishedException();
        }
    }

    /**
     * Refills the top card row from the deck, one card at a time.
     *
     * @throws ChangedEraException   if the last drawn card belongs to a new era.
     * @throws DeckFinishedException if the deck is exhausted before the row is full.
     */
    private void refillTopCardList() throws ChangedEraException, DeckFinishedException {
        Card cardToAdd = null;
        //refill the top list by the right amount of cards needed
        for (int i = 0; i < UtilitiesFunction.bindCorrectNumberOfTopListCard(gameView.getPlayerNumber()); i++) {
            if (deck.isEmpty()) {
                //if the deck is empty we are at the end of the game, so we must do specific procedures
                throw new DeckFinishedException();
            }
            //Extract the first card from the deck end add it to the top card list and then remove it from the deck
            cardToAdd = deck.get(0);
            topCardList.add(cardToAdd);
            deck.remove(0);
        }
        if (cardToAdd != null) {
            //check if the last drawn card is from a different ERA, if so it launches a ChangedEraException notifying the caller
            if (cardToAdd.getEra() != gameView.getCurrentEra()) {
                gameView.nextEra();
                throw new ChangedEraException();
            }
        }
    }

    /**
     * Moves all building cards of the current era from the building pool into the top building list,
     * and removes any buildings of the current era that were left in the bottom building list.
     */
    private void refillTopBuildingList() {
        this.topBuildingList.addAll(this.buildingCards.stream().filter(buildingCard -> buildingCard.getEra() == gameView.getCurrentEra()).toList());
        this.bottomBuildingList.removeIf(buildingCard -> buildingCard.getEra() == gameView.getCurrentEra());
    }

    /**
     * Clears all buildings from the bottom building row.
     */
    private void clearBottomBuildingList() {
        this.bottomBuildingList.clear();
    }

    /**
     * Moves all cards from the top row to the bottom row and clears the top row.
     */
    private void shiftCardTopToBottomList() {
        bottomCardList.addAll(topCardList);
        topCardList.clear();
    }

    /**
     * Moves all buildings from the top row to the bottom row and clears the top row.
     */
    private void shiftBuildingTopToBottom() {
        this.bottomBuildingList.addAll(this.topBuildingList);
        this.topBuildingList.clear();
    }

    /**
     * Draws a card from the top row and adds it to the player's tribe.
     *
     * @param position the index of the card to draw.
     * @param player   the player drawing the card.
     * @throws NotSelectableCardException if the card at the given position is an event card.
     * @throws EmptyMarketException       if the top row contains no selectable cards.
     * @throws IndexOutOfBoundsException  if {@code position} is out of range.
     */
    public void selectCardFromTopList(int position, Player player) throws NotSelectableCardException, IndexOutOfBoundsException, EmptyMarketException {
        if (topCardList == null || player == null) {
            throw new IllegalArgumentException("topCardList is null or player is null");
        }

        if (topCardList.isEmpty() || topCardList.stream().allMatch(card -> card.getCardType() == CARD_TYPE.EVENT)) {
            throw new EmptyMarketException("No cards available top list");
        }
        if (position < 0 || position >= topCardList.size()) {
            throw new IndexOutOfBoundsException("Invalid position");
        }
        Card selectedCard = this.topCardList.get(position);
        try {
            selectedCard.addCardToPlayer(player);
        } catch (NotSelectableCardException e) {
            throw new NotSelectableCardException("Cannot Select EventCard");
        }
        this.topCardList.remove(position);
        notifyCardRemoveFromTop(position, CARD_TYPE.ARTIST);
    }

    /**
     * Purchases a building from the top row and adds it to the player's buildings.
     *
     * @param position the index of the building to buy.
     * @param player   the player buying the building.
     * @throws NotEnoughFoodException    if the player cannot afford the building.
     * @throws EmptyMarketException      if the top building row is empty.
     * @throws IndexOutOfBoundsException if {@code position} is out of range.
     */
    public void buyBuildingTopList(int position, Player player) throws IndexOutOfBoundsException, NotEnoughFoodException, EmptyMarketException {
        if (topBuildingList.isEmpty()) {
            throw new EmptyMarketException("No buildings available top list");
        }
        if (position < 0 || position >= topBuildingList.size()) {
            throw new IndexOutOfBoundsException();
        }
        BuildingCard selectedBuildingCard = this.topBuildingList.get(position);
        try {
            player.tryBuyBuilding(selectedBuildingCard);
        } catch (NotEnoughFoodException exception) {
            throw new NotEnoughFoodException(player.getNickname() + " has not enough food");
        }
        this.topBuildingList.remove(position);
        notifyCardRemoveFromTop(position, CARD_TYPE.BUILDING);
    }

    /**
     * Draws a card from the bottom row and adds it to the player's tribe.
     *
     * @param position the index of the card to draw.
     * @param player   the player drawing the card.
     * @throws NotSelectableCardException if the card at the given position is an event card.
     * @throws EmptyMarketException       if the bottom row contains no selectable cards.
     * @throws IndexOutOfBoundsException  if {@code position} is out of range.
     */
    public void selectCardFromBottomList(int position, Player player) throws NotSelectableCardException, IndexOutOfBoundsException, EmptyMarketException {
        if (bottomCardList == null || player == null) {
            throw new IllegalArgumentException("bottomCardList is null or player is null");
        }
        if (bottomCardList.isEmpty() || bottomCardList.stream().allMatch(card -> card.getCardType() == CARD_TYPE.EVENT)) {
            throw new EmptyMarketException("No Card Available bottom list");
        }

        if (position < 0 || position >= bottomCardList.size()) {
            throw new IndexOutOfBoundsException("Invalid position");
        }

        Card selectedCard = this.bottomCardList.get(position);
        try {
            selectedCard.addCardToPlayer(player);
        } catch (NotSelectableCardException e) {
            throw new NotSelectableCardException("Cannot select EventCard");
        }
        this.bottomCardList.remove(position);

        notifyCardRemovedFromBottom(position, CARD_TYPE.ARTIST);
    }

    /**
     * Purchases a building from the bottom row and adds it to the player's buildings.
     *
     * @param position the index of the building to buy.
     * @param player   the player buying the building.
     * @throws NotEnoughFoodException    if the player cannot afford the building.
     * @throws EmptyMarketException      if the bottom building row is empty.
     * @throws IndexOutOfBoundsException if {@code position} is out of range.
     */
    public void buyBuildingBottomList(int position, Player player) throws NotEnoughFoodException, IndexOutOfBoundsException, EmptyMarketException {
        if (bottomBuildingList.isEmpty()) {
            throw new EmptyMarketException("No buildings available bottom list");
        }
        if (position < 0 || position >= bottomBuildingList.size()) {
            throw new IndexOutOfBoundsException();
        }
        BuildingCard selectedBuildingCard = this.bottomBuildingList.get(position);
        try {
            player.tryBuyBuilding(selectedBuildingCard);
        } catch (NotEnoughFoodException exception) {
            throw new NotEnoughFoodException(player.getNickname() + " has not enough food");
        }
        this.bottomBuildingList.remove(position);

        notifyCardRemovedFromBottom(position, CARD_TYPE.BUILDING);
    }
    //the logic does not enforce that this is called at end-of-turn; it is assumed to only be called then,
    //but even if called mid-turn it is safe since it only returns a bool and does not actually resolve events

    /**
     * Subscribes an observer to receive market change notifications.
     *
     * @param observerToAdd the observer to subscribe.
     */
    public void addObserver(MarketObserver observerToAdd) {
        if (observerToAdd != null && !observers.contains(observerToAdd)) {
            observers.add(observerToAdd);
        }
    }

    /**
     * Unsubscribes an observer from market change notifications.
     *
     * @param observerToRemove the observer to remove.
     */
    public void removeObserver(MarketObserver observerToRemove) {
        observers.remove(observerToRemove);
    }

    private void notify(Consumer<MarketObserver> action) {
        for (MarketObserver observer : observers) {
            action.accept(observer);
        }
    }

    /**
     * this method is used to update all the subscribed observer
     */
    public void notifyMarketChanged() {
        List<Card> topCardsSnapshot = List.copyOf(topCardList);
        List<Card> bottomCardsSnapshot = List.copyOf(bottomCardList);
        List<BuildingCard> topBuildingsSnapshot = List.copyOf(topBuildingList);
        List<BuildingCard> bottomBuildingsSnapshot = List.copyOf(bottomBuildingList);
        notify(observer -> observer.onMarketChanged(
                topCardsSnapshot,
                bottomCardsSnapshot,
                topBuildingsSnapshot,
                bottomBuildingsSnapshot
        ));
    }

    /**
     * at the end of the round a new top set card is created
     */
    private void notifyTopCardRefreshed() {
        List<Card> topCardsSnapshot = List.copyOf(topCardList);
        notify(observer -> observer.onTopCardRefreshed(topCardsSnapshot));
    }

    /**
     * Executes notify top building refreshed.
     */
    private void notifyTopBuildingRefreshed() {
        List<BuildingCard> topBuildingSnapshot = List.copyOf(topBuildingList);
        notify(observer -> observer.onTopBuildingRefreshed(topBuildingSnapshot));
    }

    /**
     * Executes notify card remove from top.
     *
     * @param position parameter position.
     * @param card     parameter card.
     */
    private void notifyCardRemoveFromTop(int position, CARD_TYPE card) {
        notify(observer -> observer.onCardRemovedFromTop(position, card));
    }

    /**
     * Executes notify card removed from bottom.
     *
     * @param position parameter position.
     * @param card     parameter card.
     */
    private void notifyCardRemovedFromBottom(int position, CARD_TYPE card) {
        notify(observer -> observer.onCardRemovedFromBottom(position, card));
    }


    /**
     * Resolves the final events of the game.
     * This method safely moves all Event cards from the top row to the bottom row
     * using batch operations to prevent ConcurrentModificationExceptions,
     * and then triggers their resolution.
     */
    public void solveFinalEvents() {
        // 1. Identify and collect all Event cards from the top row.
        // Using streams prevents modifying the list while iterating over it.
        List<Card> eventsToMove = topCardList.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.EVENT)
                .toList();
        // 2. Add all the collected Event cards to the bottom row in a single batch operation.
        bottomCardList.addAll(eventsToMove);
        // 3. Safely remove all transferred Event cards from the top row in a single batch operation.
        topCardList.removeAll(eventsToMove);
        // 4. Proceed to resolve the events now that they are safely positioned.
        solveEvents();
    }

    /**
     * this method, if there are Events in the bottomCardList, it solves them.
     * First it order them by event Type (Sustenance are the last events to be solved), in case of two events from two different ERAS
     * the oldest one(the one with the "least ERA") must be done first
     */
    private void solveEvents() {
        List<EventCard> eventToBeSolved = this.bottomCardList.stream().filter(card -> card.getCardType() == CARD_TYPE.EVENT).map(EventCard.class::cast).collect(Collectors.toCollection(ArrayList::new));
        if (!eventToBeSolved.isEmpty()) {
            eventToBeSolved.sort(Comparator.comparing(EventCard::getEventType).thenComparing(EventCard::getEra));
            eventToBeSolved.forEach(eventCard -> {
                eventCard.applyEventEffect(gameView.getPlayerList());
                notifyResolvedEvent(eventCard.getEventID(), eventCard.getEventType());
            });
        }

    }

    /**
     * this method first shuffles the deck then order it by era and at the end appends the two final events
     */
    private void organizeDeck() {
        EventCard finalShamanEventCard = (EventCard) this.deck.getLast();
        this.deck.removeLast();
        EventCard finalSustenanceEventCard = (EventCard) this.deck.getLast();
        this.deck.removeLast();
        Collections.shuffle(deck);
        deck.sort(Comparator.comparing(Card::getEra));
        deck.addLast(finalSustenanceEventCard);
        deck.addLast(finalShamanEventCard);
    }

    /**
     * this method initialize the bottom list with the right amount o cards
     */
    private void initializeBottomList() {
        int numberOfCard = UtilitiesFunction.bindCorrectNumberOfBottomListCard(gameView.getPlayerNumber());
        Card cardToAdd;
        for (int i = 0; i < numberOfCard; i++) {
            cardToAdd = deck.getFirst();
            if (cardToAdd.getCardType() == CARD_TYPE.EVENT) {
                this.topCardList.add(cardToAdd);
                i--;
            } else {
                this.bottomCardList.add(cardToAdd);
            }
            deck.removeFirst();
        }
    }

    /**
     * this method initialize the top card list and the top building list
     */
    private void initializeBothTopList() {
        int numberOfCard = UtilitiesFunction.bindCorrectNumberOfTopListCard(gameView.getPlayerNumber());
        numberOfCard = numberOfCard - this.topCardList.size();
        for (int i = 0; i < numberOfCard; i++) {
            this.topCardList.add(deck.getFirst());
            deck.removeFirst();
        }
        this.topBuildingList.addAll(buildingCards.stream().filter(buildingCard -> buildingCard.getEra() == ERA.ERA_I).toList());
        buildingCards.removeIf(buildingCard -> buildingCard.getEra() == ERA.ERA_I);
    }

    private void notifyResolvedEvent(int eventID, EVENT_TYPE eventType) {
        for (MarketObserver observer : observers) {
            observer.eventSolved(eventID, eventType);
        }
    }

    private void notifyExtraDrawSnapshotReady() {
        List<Card> cardSnap = List.copyOf(extraDrawCardSnapshot);
        List<BuildingCard> buildingSnap = List.copyOf(extraDrawBuildingSnapshot);
        notify(observer -> observer.onExtraDrawSnapshotReady(cardSnap, buildingSnap));
    }

    /**
     * Creates a memento capturing the current market state: top and bottom rows of cards
     * and buildings, the remaining deck, and the pool of buildings not yet visible.
     *
     * @return a {@link MarketMemento} containing snapshots of the market lists.
     */
    @Override
    public MarketMemento createMemento() {
        UtilitiesFunction.logInfo(LOG_PREFIX, "Creating market memento (deck=" + deck.size() + ", top=" + topCardList.size() + ", bottom=" + bottomCardList.size() + ")");
        return new MarketMemento(
                this.topCardList.stream().map(Card::toDTO).toList(),
                this.bottomCardList.stream().map(Card::toDTO).toList(),
                this.deck.stream().map(Card::toDTO).toList(),
                this.topBuildingList.stream().map(BuildingCard::getBuildingID).toList(),
                this.bottomBuildingList.stream().map(BuildingCard::getBuildingID).toList(),
                this.buildingCards.stream().map(BuildingCard::getBuildingID).toList()

        );
    }

    /**
     * Restores the market state from the given memento, rebuilding all card and building lists
     * via the {@link it.polimi.ingsw.am25.server.model.Factory.Deck.DeckFactory}.
     *
     * @param memento the {@link MarketMemento} from which to restore the market state.
     */
    @Override
    public void restoreMemento(MarketMemento memento) {
        UtilitiesFunction.logInfo(LOG_PREFIX, "Restoring market memento");
        DeckFactory deckFactory = new DeckFactory();
        this.topCardList = deckFactory.loadDeck(memento.topCards());
        this.bottomCardList = deckFactory.loadDeck(memento.bottomCards());
        this.deck = deckFactory.loadDeck(memento.deck());
        this.topBuildingList = deckFactory.loadBuidlingDeck(memento.topBuildingIDs(), this.boardView);
        this.bottomBuildingList = deckFactory.loadBuidlingDeck(memento.bottomBuildingIDs(), this.boardView);
        this.buildingCards = deckFactory.loadBuidlingDeck(memento.buildingPoolIDs(), this.boardView);
    }
}
