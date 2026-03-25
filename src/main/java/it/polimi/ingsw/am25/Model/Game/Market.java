package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.TurnOrderView;
import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Card.EventCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Factory.Building.BuildingFactory;
import it.polimi.ingsw.am25.Model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.NotEnoughFoodException;
import it.polimi.ingsw.am25.Model.Utilities.UtilitiesFunction;

import java.util.*;

public class Market {
    private List<Card> topCardList;
    private List<BuildingCard> topBuildingList;
    private List<Card> bottomCardList;
    private List<BuildingCard> bottomBuildingList;
    private List<Card> deck ;
    private List<BuildingCard> buildingCards;
    private final GameView gameView;

    /**
     * default constructo of Market
     *
     */
    public Market(GameView gameView) {
        this.topCardList= new ArrayList<>();
        this.bottomBuildingList= new ArrayList<>();
        this.bottomCardList = new ArrayList<>();
        this.topBuildingList=new ArrayList<>();
        this.gameView=gameView;
        this.buildingCards=new BuildingFactory().createBuildingDeck(gameView.getPlayerNumber());
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

    public void clearBottomBuildingList(){
        this.bottomBuildingList.clear();
    }
    /**
     * this method shift the card from the topList To the bottom list
     */
    public void shiftCardTopToBottomList(){
        bottomCardList.addAll(topCardList);
        topCardList.clear();
    }

    public void shiftBuildingTopToBottom(){
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

        //a questo punto se la carta è un edificio la mette in building, se no in tribe
        // da vedere bene come funziona quando uno vuole prendere edificio, se deve pagare cibo o no
        // nel caso va aggiunto. inotlre nella lista sopra non ci sono carte evento giisto? quindi non
        //c'è la possibiità di pescare un evento? se c'è va aggiunto in un altro else che se il giocatore
        // prova a pescare un evento anzichè una carta normale lancia eccezione. stessa cosa quando
        //facciamo il metodo per pescare dalla bottomlist.

        selected_card.addCardToPlayer(player);

        //poi rimuove la carta scelta dalla toplist, ma remove mi sembra che cancelli proprio la posizone
        //dalla lista, quindi ad esempio da x elementi va ad x-1. se no possiamo semplicemente settare a
        //null quella posizione della lista
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
    }
    //nella logica non ho messo che deve verificare che siamo a fine turno quindi ho dato per scontato
    //che è un metodo che viene chiamato solo a fine turno, ma in realtà anche se venisse chiamato a metà turno
    //tanto ritorna solo un bool, non esegue effettivamente gli eventi

    /**
     * this method check if there are event in the bottom list
     * @return returns true if a event is found in bottom list
     */
    public boolean checkEventsPresence(){
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
