package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.BoardView;
import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Card.EventCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Factory.Building.BuildingFactory;
import it.polimi.ingsw.am25.Model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.*;
import it.polimi.ingsw.am25.Model.Utilities.Exception.ChangedEraException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.DeckFinishedException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.NotEnoughFoodException;

import java.util.*;
import java.util.stream.Collectors;

public class Market {
    private List<Card> topCardList;
    private List<BuildingCard> topBuildingList;
    private List<Card> bottomCardList;
    private List<BuildingCard> bottomBuildingList;
    private List<Card> deck ;
    private List<BuildingCard> buildingCards;
    private final GameView gameView;



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
    }

    public List<BuildingCard> getBottomBuildingList() {
        return bottomBuildingList;
    }

    public List<BuildingCard> getTopBuildingList() {
        return topBuildingList;
    }

    public List<Card> getBottomCardList() {
        return bottomCardList;
    }

    public List<Card> getTopCardList() {
        return topCardList;
    }

    /**
     * this method clear the bottom list from everything
     */
    public void clearBottomCardList(){
        //eccezione se lista non inizializzata ma non dovrebbe succedere se scriviamo bene il costruttore
        if (bottomCardList == null) {
            throw new IllegalStateException("La bottomCardList non è ancora stata inizializzata");
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
        } catch (ChangedEraException e) {
            this.clearBottomBuildingList();
            this.shiftBuildingTopToBottom();
            this.refillTopBuildingList();
        } catch (DeckFinishedException e) {
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
    public void selectCardFromTopList(int position, Player player) {
        //questo è per sicurezza ma non dovrebbe succedere, magari si può aggiungere anche il caso
        // in cui player vuole pescare una carta ma la lista non è null ma è vuota

        if (topCardList == null || player == null) {
            throw new IllegalArgumentException("topCardList null o player null");
        }
        if (topCardList.isEmpty()) {
            throw new IllegalStateException("topCardList è vuota");
        }
        if (position < 0 || position >= topCardList.size()) {
            throw new IndexOutOfBoundsException("Posizione non valida");
        }
        Card selected_card = this.topCardList.get(position);
        selected_card.addCardToPlayer(player);
        this.topCardList.remove(position);
    }

    /**
     * this method checks if the player has enough food to buy the house, if it can't throws exception. Otherwise add the building
     * to the player building list
     * @param position position of the card it wants to buy
     * @param player the player who wants to buy
     */
    public void buyBuildingTopList(int position, Player player){
        BuildingCard selectedBuildingCard = this.topBuildingList.get(position);
        try{
            player.tryBuyBuilding(selectedBuildingCard);
        }catch (NotEnoughFoodException exception){
            throw new NotEnoughFoodException(player.getNickname()+" has not enough food");
        }
        this.topBuildingList.remove(position);
    }



    /**
     * This method is used to draw a card from the bottom list and add it to the player deck
     * @param position position of the card to be drawn
     * @param player player that has draw the card
     */
    public void selectCardFromBottomList(int position, Player player){
        //solite eccezioni come in selectedCardFromTopList()

        if (bottomCardList == null || player == null) {
            throw new IllegalArgumentException("bottomCardList null o player null");
        }

        if (bottomCardList.isEmpty()) {
            throw new IllegalStateException("bottomCardList è vuota");
        }

        if (position < 0 || position >= bottomCardList.size()) {
            throw new IndexOutOfBoundsException("Posizione non valida");
        }

        //seleziona carta da position e mette in selected_card come metodo sopra
        Card selected_card = this.bottomCardList.get(position);

        selected_card.addCardToPlayer(player);
        this.bottomCardList.remove(position);
    }

    /**
     * this method checks if the player has enough food to buy the house, if it can't throw exception. Otherwise, add the building
     * to the player building list
     * @param position position of the card it wants to buy
     * @param player the player who wants to buy
     */
    public void buyBuildingBottomList(int position, Player player) throws NotEnoughFoodException {

        BuildingCard selectedBuildingCard = this.bottomBuildingList.get(position);
        try{
            player.tryBuyBuilding(selectedBuildingCard);
        }catch (NotEnoughFoodException exception){
            throw new NotEnoughFoodException(player.getNickname()+" has not enough food");
        }
        this.bottomCardList.remove(position);
    }
    //nella logica non ho messo che deve verificare che siamo a fine turno quindi ho dato per scontato
    //che è un metodo che viene chiamato solo a fine turno, ma in realtà anche se venisse chiamato a metà turno
    //tanto ritorna solo un bool, non esegue effettivamente gli eventi

    /**
     * this method check if there are event in the bottom list
     * @return returns true if a event is found in bottom list
     */
    //questo metodo potrebbe no servire piu
    private boolean checkEventsPresence(){
        //solita eccezione anche qui
        if (bottomCardList == null) {
            throw new IllegalStateException("bottomCardList è null");
        }
        //scorre la lista sotto e cerca event cards
        for (Card card : bottomCardList) {
            if (card.getCardType() == CARD_TYPE.EVENT) {
                return true;
            }
        }
        //se non trova più eventi allora false
        return false;
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
