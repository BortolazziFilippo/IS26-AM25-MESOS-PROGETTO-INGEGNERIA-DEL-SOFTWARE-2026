package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

public class DoublePPOnShamanEvent extends BuildingEffect{
    private boolean flag = false;
    private int prevPP;

    public DoublePPOnShamanEvent() {
    }

    @Override
    public void applyEffect(Player player) {
        if(!flag){
            prevPP = player.getPrestigePoint();
            flag = true;
        }else{
            if(player.getPrestigePoint()>prevPP){
                player.managePP(player.getPrestigePoint()-prevPP);
                flag = false;
            }
        }
    }
}
