package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Card.BuildingCard;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Factory.Building.BuildingFactory;
import it.polimi.ingsw.am25.Model.Factory.Deck.DeckFactory;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Tile.DefaultTile;
import it.polimi.ingsw.am25.Model.Tile.OfferTile;
import it.polimi.ingsw.am25.Model.Tile.OfferTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Game {
    private final List<Player> players;
    private final Player playerHost;
    private final int playerNumber;
    private OfferTrack offerTrack;
    private List<Card> deck;
    private final List <Card> topCardList;
    private final List<Card> bottomCardList; /*modificare UML le lettere maiuscole*/
    private final ERA actualEra;
    private final List<Player> placingOrder;
    private final List<Player> playingOrder;
    private List<BuildingCard> buildings;

    /**
     * default constructor of game, this method when called manage to create the Deck anc the building By launching the factories
     *
     * @param playerHost player who created the game
     * @param playerNumber number of player
     */

    public Game(Player playerHost, int playerNumber) {
        //solita eccezione per gli argoementi passati
        if (playerHost == null) {
            throw new IllegalArgumentException("playerHost nullo");
        }

        this.playerHost = playerHost;
        this.playerNumber = playerNumber;

        this.players = new ArrayList<>();
        this.players.add(playerHost);

        this.actualEra = ERA.ERA_I;

        this.topCardList = new ArrayList<>();
        this.bottomCardList = new ArrayList<>();

        this.createDecks(playerNumber);
        //crea deck, separa eventi finali (shamano era 3 e sostentamento era 3)
        // fa shuffle, sort per era, poi prima riempie fila sotto SENZA eventi

        //divide deck in carte normali e carte eventi
        List<Card> eventCards = new ArrayList<>();
        List<Card> normalCards = new ArrayList<>();

        //scorre il deck e popola le due liste dichiarate qui sopra
        for (Card c : this.deck) {
            if (c.getCardType() == CARD_TYPE.EVENT) {
                eventCards.add(c);
            } else {
                normalCards.add(c);
            }
        }

        //fa due variabili temporanee per salvare evento finale shamano era 3 e sostentamento era 3
        /*
        for (Card c : eventCards) {
            if (c.getCardType() == CARD_TYPE.EVENT && ) {

            }
        }
        */

        //poi una volta salvate le due carte evento dobbiaomo fare un remove per toglierle prima dello
        //shuffle

        //qui fa lo shuffle del mazzo normale, mazzo eventi non serve fare shuffle
        Collections.shuffle(normalCards);

        //ora deve fare sorting per era (anche di mazzo eventi?)
        normalCards.sort(Comparator.comparing(Card::getEra));

        //ora popola liste sotto e sopra


        //edifici sono già ordinati in base ad era, vanno solo messi in fondo alla lista in alto dopo
        //che l'abbiamo popolata con le carte
        this.createBuildings(playerNumber);

        this.createOfferTrack(playerNumber);



        this.placingOrder = new ArrayList<>();
        this.playingOrder = new ArrayList<>();

    }

    private void createOfferTrack(int playerNumber) {
        this.offerTrack= new OfferTrack(playerNumber);
    }

    private void createDecks(int playerNumber) {
        DeckFactory deckFactory = new DeckFactory();
        this.deck = deckFactory.createDeck(playerNumber);
    }

    private void createBuildings(int playerNumber) {
        BuildingFactory buildingFactory = new BuildingFactory();
        this.buildings = buildingFactory.createBuildingDeck(playerNumber);
    }

    //aggiungere qui che se aggiunge una carta con era diversa dall'era della prima carta aggiunta allora
    //chaima il metoto sotto che aggiorna l'era
    private void refillTopList(){
        for(int i = 0; i < (this.playerNumber+4); i++){
            this.topCardList.addFirst(this.deck.getFirst());
            this.deck.removeFirst();
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

    private void removeFromBottomList(){
        //eccezione se lista non inizializzata ma non dovrebbe succedere se scriviamo bene il costruttore
        if (bottomCardList == null) {
            throw new IllegalStateException("La bottomCardList non è ancora stata inizializzata");
        }
        this.bottomCardList.clear();
    }

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
     * method used for managing the player food
     * @param player player to manage food
     * @param food_amount amount of food to manage (both positive or negative)
     */
    public void manageFood(Player player, int food_amount){
        player.manageFood(food_amount);
    }

    public void managePrestigePoint(Player player, int PP_amount){
        player.managePP(PP_amount);
    }

    public List<Card> getCardTopList(){
        return this.topCardList;
    }

    public List<Card> getCardBottomList(){
        return this.bottomCardList;
    }

    /**
     * This method is used to draw a card from the top list and add it to the player deck
     * @param position position of the card to be drawn
     * @param player player that has draw the card
     */

    //1) da aggiungere che se prende carta edificio deve pagare cibo ma bisogna fare metodo per capire
    //se può pagare
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

        if (selected_card.getCardType() == CARD_TYPE.BUILDING) {
            //fa cast perchè selected_card è di tipo Card ma il metodo prende BuildingCard
            player.addBuilding((BuildingCard) selected_card);
        } else if (selected_card.getCardType() == CARD_TYPE.EVENT) {
            throw new IllegalStateException("player ha provato a pescare una event card");
        } else {
            player.addCardToTribe(selected_card);
        }

        this.topCardList.remove(position);

    }
    /**
     * This method is used to draw a card from the bottom list and add it to the player deck
     * @param position position of the card to be drawn
     * @param player player that has draw the card
     */
    //1) da aggiungere che se prende carta edificio deve pagare cibo ma bisogna fare metodo per capire
    //se può pagare
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

        //qui si aggiunge la possibilità che la posizione passata come agromento sia corrispondente
        //ad una event card che non può essere pescata, quindi aggiugniamo eccezione
        if (selected_card.getCardType() == CARD_TYPE.EVENT){
            throw new IllegalStateException("la position passata corrisponde a event card, che non si pesca");
        }

        if (selected_card.getCardType() == CARD_TYPE.BUILDING) {
            //di nuovo qui serve casting come nel metodo sopra
            player.addBuilding((BuildingCard) selected_card);
        } else {
            player.addCardToTribe(selected_card);
        }

        this.bottomCardList.remove(position);

    }

    /**
     * method used to place a player on a OfferTile
     * @param player player to place
     * @param position position on offertile
     */
    //da ricordarsi di aggiungere parte che rimuove player totem da default tile
    public void placeOnTile(Player player, int position){
        //solita eccezione
        if (player == null) {
            throw new IllegalArgumentException("player passato è null");
        }

        //prendiamo tutte le tiles di offertrack
        List<OfferTile> tiles = this.offerTrack.getAvailableOfferTiles();

        //anche qui solita eccezione che si triggera solo se il costruttore fa qualche errore
        if (tiles == null) {
            throw new IllegalStateException("abbiamo lista delle offertiles che è null, err costruttore");
        }

        // eccezione se position passata in argomento non va bene
        if (position < 0 || position >= tiles.size()) {
            throw new IndexOutOfBoundsException("position non valida");
        }

        //ora prende direttamente la tile scelta
        OfferTile selectedTile = tiles.get(position);

        //ora eccezione se tile già presa
        if (selectedTile.isOccupied()) {
            throw new IllegalStateException("tile già occupata");
        }

        //altrimenti posiziona pedina del player (DA MODIFICARE UML che abbiamo scritto "placePlayer()"
        //anzichè setPlayerOn
        selectedTile.setPalyerOn(player);

    }

    //1)aggiunto getter di defaultTile su offertrack che è privato, da ricordarsi di aggiungerlo pure su UML
    //2) c'è un problema, dobbiamo decidere come implemnetrae la parte di ordine in cui i giocatori mettono
    // giù la pedina, serve anche per placeOnTile() qui sopra
    public void returnDefaultTile(Player player){
        //eccezione se arg sbagliato
        if (player == null) {
            throw new IllegalArgumentException("player null");
        }

        //prende defaultTile
        DefaultTile defaultTile = this.offerTrack.getDefaultTile();

        //prende lista dei player nell'ordine ma è qui il problema di cui dicevo sopra
        //poi UMl dice che getPlayerPosition ritorna array ma nel codice abbiamo messo Lits, quidni va
        //aggiornato UML
        List<Player> positions = defaultTile.getPlayerPosition();

        //se lista presa è null eccezione, ma dipende da come abbiamo risolto problema sopra
        if (positions == null) {
            throw new IllegalStateException("lista delle posizioni è null");
        }

        //scorre lista posizioni e al primo slot vuoto (null) mette player (da alto verso basso)
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i) == null) {
                defaultTile.insertPlayer(player, i);
                return;
            }
        }

        //se non ha trovato posizioni vuote errore
        throw new IllegalStateException("defaultTile già piena");

    }

    public Player checkWinner(){
        //questa è solo per completezza ma se il costruttore funziona non dovrebbe mai verificarsi
        if (this.players == null || this.players.isEmpty()) {
            throw new IllegalStateException("Nessun giocatore presente, errore nle costruttore");
        }

        //prende il primo player della lista, come se fosse una variabile tmp prima di fare il for
        Player winner = this.players.get(0);

        //scorre la lista e aggiorna man mano e fa spareggio con il food se i PP sono uguali
        for (Player p : this.players) {
            if (p.getPrestigePoint() > winner.getPrestigePoint()) {
                winner = p;
            } else if (p.getPrestigePoint() == winner.getPrestigePoint()) {
                if (p.getFood() > winner.getFood()) {
                    winner = p;
                }
            }
        }

        return winner;


    }

    //aggiungere metodo che cambia era

}


