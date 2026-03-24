package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;
//1
//2
//3
//4
//5
//6
public class SixFoodCompletedSet extends BuildingEffect {
    private boolean alreadyUsed = false;
    private final List<Integer> setCard = new ArrayList<>();
    private List<Card> listOldCard;

    public SixFoodCompletedSet() {
    }

    @Override
    public void applyEffect(Player player) {

        if(!alreadyUsed){
            for (int i = 0; i < 6; i++) {
                setCard.add(i,0);
            }

            listOldCard=new ArrayList<>(player.getTribe());
            this.alreadyUsed=true;
        }else{
            List<Card> listOfNewCard = player.getTribe();
            List<Card> difference = new ArrayList<>(listOfNewCard);
            difference.removeAll(listOldCard);
            listOldCard=new ArrayList<>(listOfNewCard);
            for (Card card: difference){
                switch (card.getCardType()){
                    case BUILDER:
                        setCard.set(0,setCard.getFirst()+1);
                        break;
                    case ARTIST:
                        setCard.set(1,setCard.get(1)+1);
                        break;
                    case GATHERER:
                        setCard.set(2,setCard.get(2)+1);
                        break;
                    case SHAMAN:
                        setCard.set(3,setCard.get(3)+1);
                        break;
                    case INVENTOR:
                        setCard.set(4,setCard.get(4)+1);
                        break;
                    case HUNTER:
                        setCard.set(5,setCard.get(5)+1);
                        break;
                    default:
                        System.err.println(getClass()+" errore identificazione carta");
                }
            }

            if(!setCard.contains(0)){
                player.manageFood(5);
                setCard.replaceAll(integer -> integer-1);
            }
        }
    }
}
