package it.polimi.ingsw.am25.Model;

import it.polimi.ingsw.am25.Model.Card.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface Utilities {

     static void countOccurrence(List<Card> listToParse,List<Integer> setCards){
        int quantity=6;
        for (Card card : listToParse) {
            switch (card.getCardType()){
                case BUILDER:
                    setCards.set(0, setCards.getFirst()+1);
                    break;
                case ARTIST:
                    setCards.set(1,setCards.get(1)+1);
                    break;
                case GATHERER:
                    setCards.set(2,setCards.get(2)+1);
                    break;
                case SHAMAN:
                    setCards.set(3,setCards.get(3)+1);
                    break;
                case INVENTOR:
                    setCards.set(4,setCards.get(4)+1);
                    break;
                case HUNTER:
                    setCards.set(5,setCards.get(5)+1);
                    break;
                default:
                    System.err.println(" errore identificazione carta");

            }
        }
    }
}
