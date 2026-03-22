package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Tile.OfferTrack;

import java.util.List;

public class Game {
    private List<Player> players;
    private Player playerHost;
    private int playerNumber;
    private OfferTrack offerTrack;
    private List<Card> deck;
    private List <Card> topCardList;
    private List<Card> bottomCardList; /*modificare UML le lettere maiuscole*/
    private ERA actualEra;
    private List<Player> placingOrder;
    private List<Card> playingOrder;

    public Game(Player playerHost, int playerNumber) {

    }

    private void createOfferTrack(int playerNumber) {
        this.offerTrack= new OfferTrack(playerNumber);
    }

    private void createDecks(int playerNumber) {
        DeckFactory deckFactory = new DeckFactory();
        this.deck = deckFactory.createDeck(playerNumber);


    }

    private void refillTopList(){
        for(int i = 0; i < (this.playerNumber+4); i++){
            this.topCardList.addFirst(this.deck.get(0));
            this.deck.remove(0);
        }
    }

    private void ShiftTopToBottomList(){
        for (Card card : this.topCardList){
            if(card.getCardType() != CARD_TYPE.BUILDING){
                this.bottomCardList.add(card);
                this.topCardList.remove(card);
            }
        }

    }

    //non sono sicuro sulla logica del metodo quindi ho messo che rimuove tutte le carte dalla lista
    //ma forse è più comodo se rimuove solo l'ultima e poi al massimo viene chiamato più volte
    //clear non cancella la lista, la svuota e basta
    private void removeFromBottomList(){
        //eccezione se lista non inizializzata ma non dovrebbe succedere se scriviamo bene il costruttore
        if (bottomCardList == null) {
            throw new IllegalStateException("La bottomCardList non è ancora stata inizializzata");
        }
        this.bottomCardList.clear();
    }

    public void manageFood(Player player, int food_amount){
        player.manageFood(food_amount);
    }

    //non ricordo più se i PP possono diventare negativi. mi sembrava di no ma se possono diventare negativi
    //allora non abbiamo gestito quel caso in managePP() di player
    public void managePrestigePoint(Player player, int PP_amount){
        player.managePP(PP_amount);
    }

    public List<Card> getCardTopList(){
        return this.topCardList;
    }

    public List<Card> getCardBottomList(){
        return this.bottomCardList;
    }

    public void selectCardFromTopList(int position, Player player) {
        //questo è per sicurezza ma non dovrebbe succedere, magari si può aggiungere anche il caso
        // in cui player vuole pescare una carta ma la lista non è null ma è vuota
        if (topCardList == null || player == null) {
            throw new IllegalArgumentException("topCardList null o player null");
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
        if (selected_card.getCardType() != CARD_TYPE.BUILDING) {
            player.addCardToTribe(selected_card);
        }
        /* qui manca l'else che dice che se è building allora aggiunge a building del player ma manca
        il metodo per aggiungere a building in player

        else {
            player.metodo_per_aggiungere_a_building(selected_card);
        }
        */

        //poi rimuove la carta scelta dalla toplist, ma remove mi sembra che cancelli proprio la posizone
        //dalla lista, quindi ad esempio da x elementi va ad x-1. se no possiamo semplicemente settare a
        //null quella posizione della lista
        this.topCardList.remove(position);

    }

    //per checkWinner() mi sa che mancano i getter che ho scritto su discord ma è semplice



}


