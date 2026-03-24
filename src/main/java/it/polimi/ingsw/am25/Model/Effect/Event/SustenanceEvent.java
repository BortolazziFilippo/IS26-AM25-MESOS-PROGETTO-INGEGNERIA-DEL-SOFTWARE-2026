package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

import static java.lang.Math.abs;

public class SustenanceEvent extends EventEffect {
    private final int foodPerCharcater;
    private final int PPLost;

    public SustenanceEvent(int foodPerCharcater, int PPLost) {
        this.foodPerCharcater = foodPerCharcater;
        this.PPLost = PPLost;
    }

    @Override
    public void solveEvent(List<Player> playersList) {
        int numberOfVillager=0;
        int foodToSubtract=0;
        int foodDiscount=0;
        int startingFood=0;
        for (Player player : playersList) {
            startingFood=player.getFood();
            numberOfVillager=-(int)player.getTribe().size()*foodPerCharcater;
            foodDiscount=(int)player.getTribe().stream().filter(card -> card.getCardType()== CARD_TYPE.GATHERER).count()*3;
            foodToSubtract=numberOfVillager+foodDiscount;

            //checking if foodToSubtract is greater than zero, gatherer cannot give food
            if(foodToSubtract>0){
                foodToSubtract=0;
            }
            //by doing this I apply the effect giving the food to the player, then i subtract the food.
            player.getBuildingCards().stream().filter(buildingCard -> buildingCard.getApplyOn()== EVENT_TYPE.SUSTENANCE).forEach(buildingCard -> buildingCard.applyBuildingEffect(player));
            //if the player Food - foodToSubtract is less than zero, then I have to set the food to zero and subtract the right amount of pp
            if(player.getFood()-foodToSubtract<0){
                int amountToZero= player.getFood()-foodToSubtract;
                player.manageFood(foodToSubtract+abs(amountToZero));
                player.managePP(amountToZero*PPLost);
            }else{
                player.manageFood(foodToSubtract);
            }
            if(player.getFood()>startingFood){
                player.manageFood(-(player.getFood()-startingFood));
            }
        }
    }
}
