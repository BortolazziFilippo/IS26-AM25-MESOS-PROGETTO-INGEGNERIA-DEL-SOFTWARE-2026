package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

public class NoPPLostOnShaman extends BuildingEffect{
    private int prevPP;
    private boolean flag = false;

    public NoPPLostOnShaman() {

    }

    @Override
    public void applyEffect(Player player) {
        if(!flag){
            prevPP = player.getPrestigePoint();
            flag = true;
        }else{
            if(player.getPrestigePoint()<prevPP){
                player.managePP(prevPP - player.getPrestigePoint());
                flag = false;
            }
        }
    }
}
