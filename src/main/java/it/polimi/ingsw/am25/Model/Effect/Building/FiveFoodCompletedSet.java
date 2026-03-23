package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;
//1
//2
//3
//4
//5
//6
public class FiveFoodCompletedSet extends BuildingEffect {
    private boolean alreadyUsed = false;
    private List<Boolean> setCard = new ArrayList<>();
    private List<Card> listOldCard;
    private List<Card> listOfNewCard;
    private List<Card> difference;
    public FiveFoodCompletedSet() {
    }

    @Override
    public void applyEffect(Player player) {

        if(!alreadyUsed){
            for (int i = 0; i < 6; i++) {
                setCard.add(i,false);
            }

            listOldCard=new ArrayList<>(player.getTribe());
            this.alreadyUsed=true;
            return;
        }else{
            listOfNewCard=player.getTribe();
            difference=new ArrayList<>(listOfNewCard);
            difference.removeAll(listOldCard);
            listOldCard=new ArrayList<>(listOfNewCard);
            for (Card card:difference){
                switch (card.getCardType()){
                    case BUILDER:
                        setCard.set(0,true);
                        break;
                    case ARTIST:
                        setCard.set(1,true);
                        break;
                    case GATHERER:
                        setCard.set(2,true);
                        break;
                    case SHAMAN:
                        setCard.set(3,true);
                        break;
                    case INVENTOR:
                        setCard.set(4,true);
                        break;
                    case HUNTER:
                        setCard.set(5,true);
                        break;
                    default:
                        System.err.println(getClass()+" errore identificazione carta");
                }
            }
            if(setCard.contains(false)){
                return;
            }else {
                player.manageFood(+5);
                setCard.replaceAll(element->false);
            }
        }
    }
}
