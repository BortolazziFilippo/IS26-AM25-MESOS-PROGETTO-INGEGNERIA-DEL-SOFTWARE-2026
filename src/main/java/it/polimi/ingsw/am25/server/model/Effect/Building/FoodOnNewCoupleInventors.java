package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Card.InventorCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Building effect that awards 3 food for each new pair of Inventor cards sharing the same icon
 * added to the tribe since the last time this effect was evaluated.
 */
public class FoodOnNewCoupleInventors extends BuildingEffect {
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    private List<Card> beforeTurnCards = new ArrayList<>();
    private final List<InventorCard> pairsCards = new ArrayList<>();

    /**
     * Default constructor for FoodOnNewCoupleInventors.
     */
    public FoodOnNewCoupleInventors() {
    }
    /**
     * Compares the player's current tribe with the tribe state from the previous evaluation.
     * For each new pair of Inventor cards that share the same icon, awards 3 food to the player.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        int count = 0;
        int pairs = 0;
        int position=0;
        List<Card> ActualCards= player.getTribe();
        List<Card> difference = new ArrayList<>(ActualCards);
        difference.removeAll(beforeTurnCards);
        beforeTurnCards =new ArrayList<>(ActualCards);
        for(int i=0;i<2;i++){
            pairsCards.add(i, null);
        }
        for (Card card :  difference) {
            if(card.getCardType() == CARD_TYPE.INVENTOR){
                pairsCards.set(position, (InventorCard)  card);
                pairs++;
                position++;
                if(pairs == 2){
                    if((pairsCards.get(0).getInvIcon()).equals(pairsCards.get(1).getInvIcon())){
                        count+=2;
                    }
                    for(int i=0;i<2;i++){
                        pairsCards.set(i, null);
                    }
                    pairs=0;
                    position = 0;
                }
            }
        }
        if(count>0){
            count = count/2;
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "FoodOnNewCoupleInventors: player '" + player.getNickname() + "' formed " + count +
                            " new matching Inventor pair(s), awarding " + (3 * count) + " food");
            player.manageFoodAndPP(3*count);
        }
        else{
            logServerEvent("FoodOnNewCoupleInventors: no matching Inventor card pairs found this turn for player '" + player.getNickname() + "'");
        }

    }

    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }
}
