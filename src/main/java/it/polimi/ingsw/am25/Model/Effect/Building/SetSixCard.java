package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SetSixCard extends BuildingEffect{
    private final int foodToGive = 5;
    private List<Card> oldCards = new ArrayList<>();

    public SetSixCard() {
    }

    @Override
    public void applyEffect(Player player) {
        int setCompleti;
        List <Card> newCards= new ArrayList<>(player.getTribe());
        List <Integer> setCards = new ArrayList<>();
        newCards.removeAll(oldCards);
        for (int i = 0; i < 6; i++) {
            setCards.add(i,0);
        }
        for (Card card : newCards) {
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
                    System.err.println(getClass()+" errore identificazione carta");

            }

        }
        oldCards = newCards;
        newCards.clear();
        if(!setCards.contains(0)){
            setCompleti = Collections.min(setCards);
            player.managePP(6*setCompleti);
            setCards.clear();
        }

    }
}
