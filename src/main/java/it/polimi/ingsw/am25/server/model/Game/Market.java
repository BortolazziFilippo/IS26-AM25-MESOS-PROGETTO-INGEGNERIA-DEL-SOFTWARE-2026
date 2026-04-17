package it.polimi.ingsw.am25.server.model.Game;

import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Factory.Building.BuildingFactory;
import it.polimi.ingsw.am25.server.model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.server.model.Observers.MarketObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;

import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.util.*;
import java.util.stream.Collectors;

public class Market {
    private List<Card> topCardList;
    private List<BuildingCard> topBuildingList;
    private List<Card> bottomCardList;
    private List<BuildingCard> bottomBuildingList;
    private List<Card> deck ;
    private List<BuildingCard> buildingCards;
    private  GameView gameView;
    private final List<MarketObserver> observers = new ArrayList<>();


    /**
     * default constructor of Market
     * it initializes the deck and the building
     *
     */
    public Market(GameView gameView, BoardView boardView) {
        this.topCardList= new ArrayList<>();
        this.bottomBuildingList= new ArrayList<>();
        this.bottomCardList = new ArrayList<>();
        this.topBuildingList=new ArrayList<>();
        this.gameView=gameView;
        this.buildingCards=new BuildingFactory().createBuildingDeck(gameView.getPlayerNumber(),boardView);
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
     * this method clear the bottom list from everything
     */
    public void clearBottomCardList(){
        //guard against uninitialized list — should never happen if the constructor is correct
        //this branch is untestable because the constructor always initializes the list
        if (bottomCardList == null) {
            throw new IllegalStateException("bottomCardList has not been initialized yet");
        }
        this.bottomCardList.clear();
    }

    /**
     * this method does all the things that has to be done at the end of the round in the market area:
     * 1)if there are solves the events in the bottom list
     * 2)clear the bottom list
     * 3)shift the remaining card from the top list to the bottom list
     * 4) refill the top list
     * 5)Try refilling the topCardList
     *  5.a) if an ERA change is detected it:
     *      5.1) clears the bottomBuildingList
     *      5.2)shift the building from the top to bottom building list
     *      5.3) refill the topBuildingList
     *  5.b) if the deck is finished throws DeckFineshed7
     * @throws DeckFinishedException int the case the deck is finished it notifies the caller
     */
    public void endOfRoundMarketActions() throws DeckFinishedException{
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
            notifyTopCardRefreshed(); //here it notifies all the subscribed method of the changes
            throw new DeckFinishedException();
        }
    }

    /**
     * this method refill the top Card List,
     * @throws ChangedEraException in the case an Era change is detected it throws a ChangedEraException.
     * @throws DeckFinishedException In the case The deck is finished it throws a DeckFinishedException
     */
    private void refillTopCardList() throws ChangedEraException, DeckFinishedException{
        Card cardToAdd = null;
        //refill the top list by the right amount of cards needed
        for (int i = 0; i < UtilitiesFunction.bindCorrectNumberOfTopListCard(gameView.getPlayerNumber()); i++) {
            if(deck.isEmpty()) {
                //if the deck is empty we are at the end of the game, so we must do specific procedures
                throw new DeckFinishedException();
            }
            //Extract the first card from the deck end add it to the top card list and then remove it from the deck
            cardToAdd=deck.getFirst();
            topCardList.add(cardToAdd);
            deck.removeFirst();
        }
        if (cardToAdd!=null){
            //check if the last drawn card is from a different ERA, if so it launches a ChangedEraException notifying the caller
            if(cardToAdd.getEra()!=gameView.getCurrentEra()) {
                gameView.nextEra();
                throw new ChangedEraException();
            }
        }
    }

    /**
     * Moves all building cards of the current era from the building pool into the top building list,
     * and removes any buildings of the current era that were left in the bottom building list.
     */
    private void refillTopBuildingList(){
        this.topBuildingList.addAll(this.buildingCards.stream().filter(buildingCard -> buildingCard.getEra()==gameView.getCurrentEra()).toList());
        this.bottomBuildingList.removeIf(buildingCard -> buildingCard.getEra()==gameView.getCurrentEra());
    }

    /**
     * this method clears the bottomBuildingList
     */
    private void clearBottomBuildingList(){
        this.bottomBuildingList.clear();
    }

    /**
     * this method shift the card from the topList To the bottom list
     */
    private void shiftCardTopToBottomList(){
        bottomCardList.addAll(topCardList);
        topCardList.clear();
    }

    /**
     * this method shift the buildings from the top to the bottom list
     */
    private void shiftBuildingTopToBottom(){
        this.bottomBuildingList.addAll(this.topBuildingList);
        this.topBuildingList.clear();
    }

    /**
     * This method is used to draw a card from the top list and add it to the player deck
     * @param position position of the card to be drawn
     * @param player player that has draw the card
     */
    public void selectCardFromTopList(int position, Player player) throws NotSelectableCardException, IndexOutOfBoundsException, EmptyMarketException {
        //safety guard — should not happen under normal conditions; consider also handling the case where the list is non-null but empty
        // in which the player wants to draw a card but the list is not null yet empty

        if (topCardList == null || player == null) {
            throw new IllegalArgumentException("topCardList is null or player is null");
        }

        if(topCardList.isEmpty() || topCardList.stream().allMatch(card -> card.getCardType()==CARD_TYPE.EVENT) ){
            throw new EmptyMarketException("No cards available top list");
        }
        if (position < 0 || position >= topCardList.size()) {
            throw new IndexOutOfBoundsException("Invalid position");
        }
        Card selected_card = this.topCardList.get(position);
        try { //if the card throw an exception it must not be removed from the list
            selected_card.addCardToPlayer(player);
        }catch (NotSelectableCardException e) {
             throw new NotSelectableCardException("Cannot Select EventCard");
        }
        //if everything went good it removes it
        this.topCardList.remove(position);
        notifyCardRemoveFromTop(position,CARD_TYPE.ARTIST);//here it notifies the changed list
    }

    /**
     * this method checks if the player has enough food to buy the house, if it can't throws exception. Otherwise add the building
     * to the player building list
     * @param position position of the card it wants to buy
     * @param player the player who wants to buy
     * @throws NotEnoughFoodException in the case the player has not enough food to buy the building
     * @throws IndexOutOfBoundsException in the case the position is not valid
     */
    public void buyBuildingTopList(int position, Player player) throws IndexOutOfBoundsException, NotEnoughFoodException,EmptyMarketException{
        if(topBuildingList.isEmpty()){
            throw new EmptyMarketException("No buildings available top list");
        }
        if( position< 0 || position>=topBuildingList.size()){
            throw new IndexOutOfBoundsException();
        }
        BuildingCard selectedBuildingCard = this.topBuildingList.get(position);
        try{ //if the player has not enough food the card cannot be buyed
            player.tryBuyBuilding(selectedBuildingCard);
        }catch (NotEnoughFoodException exception){
            throw new NotEnoughFoodException(player.getNickname()+" has not enough food");
        }
        this.topBuildingList.remove(position);
        notifyCardRemoveFromTop(position,CARD_TYPE.BUILDING);//here it notifies the changed list
    }

    /**
     * This method is used to draw a card from the bottom list and add it to the player deck
     * @param position position of the card to be drawn
     * @param player player that has draw the card
     */
    public void selectCardFromBottomList(int position, Player player) throws NotSelectableCardException, IndexOutOfBoundsException,EmptyMarketException{
        // null-guard first, before any method is called on bottomCardList
        if (bottomCardList == null || player == null) {
            throw new IllegalArgumentException("bottomCardList is null or player is null");
        }
        if(bottomCardList.isEmpty() || bottomCardList.stream().allMatch(card -> card.getCardType()==CARD_TYPE.EVENT) ){
            throw new EmptyMarketException("No Card Available bottom list");
        }

        if (position < 0 || position >= bottomCardList.size()) {
            throw new IndexOutOfBoundsException("Invalid position");
        }

        //selects the card at the given position and stores it in selected_card, same as in the method above
        Card selected_card = this.bottomCardList.get(position);
        try{
            selected_card.addCardToPlayer(player);
        }catch (NotSelectableCardException e) {
            throw new NotSelectableCardException("Cannot select EventCard");
        }
        this.bottomCardList.remove(position);

        notifyCardRemovedFromBottom(position,CARD_TYPE.ARTIST);
    }

    /**
     * this method checks if the player has enough food to buy the house, if it can't throw exception. Otherwise, add the building
     * to the player building list
     * @param position position of the card it wants to buy
     * @param player the player who wants to buy
     * @throws NotEnoughFoodException in the case the player has not enough food to buy the building
     * @throws IndexOutOfBoundsException in the case the position is not valid
     */
    public void buyBuildingBottomList(int position, Player player) throws NotEnoughFoodException, IndexOutOfBoundsException,EmptyMarketException{
        if(bottomBuildingList.isEmpty()){
            throw new EmptyMarketException("No buildings available bottom list");
        }
        if( position< 0 || position>=bottomBuildingList.size()){
            throw new IndexOutOfBoundsException();
        }
        BuildingCard selectedBuildingCard = this.bottomBuildingList.get(position);
        try{
            player.tryBuyBuilding(selectedBuildingCard);
        }catch (NotEnoughFoodException exception){
            throw new NotEnoughFoodException(player.getNickname()+" has not enough food");
        }
        this.bottomBuildingList.remove(position);

        notifyCardRemovedFromBottom(position,CARD_TYPE.BUILDING);
    }
    //the logic does not enforce that this is called at end-of-turn; it is assumed to only be called then,
    //but even if called mid-turn it is safe since it only returns a bool and does not actually resolve events

    /**
     * this method is used to subscribe an observer to the class
     * @param observerToAdd observer to subscribe
     */
    public void addObserver(MarketObserver observerToAdd){
        if(observerToAdd!=null && !observers.contains(observerToAdd)){
            observers.add(observerToAdd);
        }
    }

    /**
     * this method unsubscribe an observer
     * @param observerToRemove observer to unsubscribe
     */
    public void removeObserver(MarketObserver observerToRemove){
        observers.remove(observerToRemove);
    }

    /**
     * this method is used to update all the subscribed observer
     */
    public void notifyMarketChanged(){
        List<Card> topCardsSnapshot = List.copyOf(topCardList);
        List<Card> bottomCardsSnapshot = List.copyOf(bottomCardList);
        List<BuildingCard> topBuildingsSnapshot = List.copyOf(topBuildingList);
        List<BuildingCard> bottomBuildingsSnapshot = List.copyOf(bottomBuildingList);

        for(MarketObserver observer: observers){
            observer.onMarketChanged(
                    topCardsSnapshot,
                    bottomCardsSnapshot,
                    topBuildingsSnapshot,
                    bottomBuildingsSnapshot
            );
        }
    }

    /**
     * at the end of the round a new top set card is created
     */
    private void notifyTopCardRefreshed(){
        List<Card> topCardsSnapshot = List.copyOf(topCardList);
        for(MarketObserver observer: observers){
           observer.onTopCardRefreshed(topCardsSnapshot);
        }
    }
    private void notifyTopBuildingRefreshed(){
        List<BuildingCard> topBuildingSnapshot = List.copyOf(topBuildingList);
        for(MarketObserver observer: observers){
            observer.onTopBuildingRefreshed(topBuildingSnapshot);
        }
    }

    private void notifyCardRemoveFromTop(int position, CARD_TYPE card){
        for(MarketObserver observer: observers){
            observer.onCardRemovedFromTop(position, card);
        }
    }

    private void notifyCardRemovedFromBottom(int position, CARD_TYPE card){
        for(MarketObserver observer: observers){
            observer.onCardRemovedFromBottom(position, card);
        }
    }

    /**
     * this method check if there are event in the bottom list
     * @return returns true if a event is found in bottom list
     */
    //this method may no longer be needed
    @Deprecated
    private boolean checkEventsPresence(){
        //standard null-guard exception
        if (bottomCardList == null) {
            throw new IllegalStateException("bottomCardList is null");
        }
        //iterates through the bottom list looking for event cards
        for (Card card : bottomCardList) {
            if (card.getCardType() == CARD_TYPE.EVENT) {
                return true;
            }
        }
        //no events found, return false
        return false;
    }

//    public void solveFinalEvents(){
//        for (Card card : topCardList) {
//            if (card.getCardType() == CARD_TYPE.EVENT) {
//                bottomCardList.add(card);
//                topCardList.remove(card);
//            }
//        }
//        solveEvents();
//    }
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
    private void solveEvents(){
        List<EventCard> eventToBeSolved= this.bottomCardList.stream().filter(card -> card.getCardType()==CARD_TYPE.EVENT).map(EventCard.class::cast).collect(Collectors.toCollection(ArrayList::new));
        if(!eventToBeSolved.isEmpty()){
            eventToBeSolved.sort(Comparator.comparing(EventCard::getEventType).thenComparing(EventCard::getEra));
            eventToBeSolved.forEach(eventCard -> eventCard.applyEventEffect(gameView.getPlayerList()));
        }
    }

    /**
     * this method first shuffles the deck then order it by era and at the end appends the two final events
     */
    private  void organizeDeck(){
        EventCard finalShamanEventCard=  (EventCard) this.deck.getLast();
        this.deck.removeLast();
        EventCard finalSustenanceEventCard= (EventCard) this.deck.getLast();
        this.deck.removeLast();
        Collections.shuffle(deck);
        deck.sort(Comparator.comparing(Card::getEra));
        deck.addLast(finalSustenanceEventCard);
        deck.addLast(finalShamanEventCard);
    }

    /**
     * this method initialize the bottom list with the right amount o cards
     */
    private void initializeBottomList(){
        int numberOfCard= UtilitiesFunction.bindCorrectNumberOfBottomListCard(gameView.getPlayerNumber());
        Card cardToAdd;
        for (int i = 0; i < numberOfCard; i++) {
            cardToAdd=deck.getFirst();
            if(cardToAdd.getCardType()==CARD_TYPE.EVENT){
                this.topCardList.add(cardToAdd);
                i--;
            }else{
                this.bottomCardList.add(cardToAdd);
            }
            deck.removeFirst();
        }
    }

    /**
     * this method initialize the top card list and the top building list
     */
    private void initializeBothTopList(){
        int numberOfCard=UtilitiesFunction.bindCorrectNumberOfTopListCard(gameView.getPlayerNumber());
        numberOfCard=numberOfCard-this.topCardList.size();
        for (int i = 0; i < numberOfCard; i++) {
            this.topCardList.add(deck.getFirst());
            deck.removeFirst();
        }
        this.topBuildingList.addAll(buildingCards.stream().filter(buildingCard -> buildingCard.getEra()==ERA.ERA_I).toList());
        buildingCards.removeIf(buildingCard -> buildingCard.getEra()==ERA.ERA_I);
    }



}
