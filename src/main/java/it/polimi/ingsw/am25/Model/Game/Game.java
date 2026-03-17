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



}
