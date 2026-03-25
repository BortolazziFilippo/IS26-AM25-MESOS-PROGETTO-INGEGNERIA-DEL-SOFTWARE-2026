package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.UtilitiesFunction;

import java.util.ArrayList;
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
        int quantity=6;
        List <Card> newCards= new ArrayList<>(player.getTribe());
        List <Integer> setCards = new ArrayList<>(Collections.nCopies(quantity,0));
        newCards.removeAll(oldCards);
        UtilitiesFunction.countOccurrence(newCards,setCards);
        oldCards = newCards;
        newCards.clear();
        if(!setCards.contains(0)){
            setCompleti = Collections.min(setCards);
            player.managePP(6*setCompleti);
            setCards.clear();
        }

    }
}
