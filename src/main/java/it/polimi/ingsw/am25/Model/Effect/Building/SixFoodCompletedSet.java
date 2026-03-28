package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.UtilitiesFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//1
//2
//3
//4
//5
//6
public class SixFoodCompletedSet extends BuildingEffect {
    private boolean alreadyUsed = false;
    private  List<Integer> setCard;
    private List<Card> listOldCard;

    public SixFoodCompletedSet() {
    }

    @Override
    public void applyEffect(Player player) {

        if(!alreadyUsed){
            int quantity=6;
            setCard = new ArrayList<>(Collections.nCopies(quantity,0));
            listOldCard=new ArrayList<>(player.getTribe());
            this.alreadyUsed=true;
        }else{
            List<Card> listOfNewCard = player.getTribe();
            List<Card> difference = new ArrayList<>(listOfNewCard);
            difference.removeIf(card ->
                    listOldCard.stream().anyMatch(oldCard -> oldCard == card)
            );
            listOldCard=new ArrayList<>(listOfNewCard);

            UtilitiesFunction.countOccurrence(difference, setCard);

            while (!setCard.contains(0)){
                player.manageFoodAndPP(5);
                setCard.replaceAll(integer -> integer-1);
            }
        }
    }
}
