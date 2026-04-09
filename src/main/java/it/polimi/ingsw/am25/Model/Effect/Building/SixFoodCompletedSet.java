package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.UtilitiesFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Building effect that awards 5 food each time the player completes a full set of one
 * of each of the six tribe-card types across the entire game.
 * The set is tracked cumulatively: each card type must be present at least N times to
 * complete N sets, and each set is scored only once.
 */
public class SixFoodCompletedSet extends BuildingEffect {
    private boolean alreadyUsed = false;
    private  List<Integer> setCard;
    private List<Card> listOldCard;

    /**
     * Default constructor for SixFoodCompletedSet.
     */
    public SixFoodCompletedSet() {
    }

    /**
     * On the first invocation, initialises the card-type counters and snapshots the tribe.
     * On subsequent invocations, counts newly added cards and awards 5 food for each
     * complete set that can be formed from the new additions.
     *
     * @param player the player who owns this building
     */
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
