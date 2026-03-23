package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Card.InventorCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class FoodOnNewCoupleInventors extends BuildingEffect {
    private List<Card> ActualCards;
    private List<Card> BeforeTurnCards;
    private List<InventorCard> PairsCards;

    public FoodOnNewCoupleInventors() {
    }

    @Override
    public void applyEffect(Player player) {
        int count = 0;
        int pairs = 0;
        List<Card> ActualCards= player.getTribe();
        List<Card> difference = new ArrayList<>(ActualCards);
        difference.removeAll(BeforeTurnCards);
        BeforeTurnCards=new ArrayList<>(ActualCards);
        for (Card card :  difference) {
            if(card.getCardType() == CARD_TYPE.INVENTOR){
                PairsCards.add((InventorCard)  card);
                pairs++;
                if(pairs == 2){
                    if((PairsCards.get(0).getInvIcon()).equals(PairsCards.get(1).getInvIcon())){
                        pairs=0;
                    }
                    else{
                        count = count - 2;
                    }
                }
                count++;
            }
        }
        if(count%2 == 0){
            count = count/2;
            player.manageFood(3*count);
        }
        else{
            System.out.println("Non ci sono coppie di carte d'inventori che abbiano la stessa icona");
        }

    }
}
