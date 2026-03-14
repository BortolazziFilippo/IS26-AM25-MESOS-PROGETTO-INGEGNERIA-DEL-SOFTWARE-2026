package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class SustenanceEvent extends EventEffect{
    private int foodPerCharcater;
    private int PPLost;

    public SustenanceEvent(int foodPerCharcater, int PPLost){
        this.foodPerCharcater = foodPerCharcater;
        this.PPLost = PPLost;
    }

    @Override
    public void solveEvent(List<Player> playersList) {

    }
}
