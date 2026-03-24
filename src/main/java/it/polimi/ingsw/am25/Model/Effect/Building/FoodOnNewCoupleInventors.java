package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Card.InventorCard;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class FoodOnNewCoupleInventors extends BuildingEffect {
    private List<Card> ActualCards = new ArrayList<>();
    private List<Card> BeforeTurnCards = new ArrayList<>();
    private List<InventorCard> PairsCards = new ArrayList<>();

    public FoodOnNewCoupleInventors() {
    }

    @Override
    public void applyEffect(Player player) {
        int count = 0;
        int pairs = 0;
        int position=0;
        List<Card> ActualCards= player.getTribe();
        List<Card> difference = new ArrayList<>(ActualCards);
        difference.removeAll(BeforeTurnCards);
        BeforeTurnCards=new ArrayList<>(ActualCards);
        for(int i=0;i<2;i++){
            PairsCards.add(i, null);
        }
        for (Card card :  difference) {
            if(card.getCardType() == CARD_TYPE.INVENTOR){
                PairsCards.set(position, (InventorCard)  card);
                pairs++;
                position++;
                if(pairs == 2){
                    if((PairsCards.get(0).getInvIcon()).equals(PairsCards.get(1).getInvIcon())){
                        count+=2;
                    }
                    for(int i=0;i<2;i++){
                        PairsCards.set(i, null);
                    }
                    pairs=0;
                    position = 0;
                }
            }
        }
        if(count>0){
            count = count/2;
            player.manageFood(3*count);
        }
        else{
            System.out.println("Non ci sono coppie di carte d'inventori che abbiano la stessa icona");
        }

    }
}
