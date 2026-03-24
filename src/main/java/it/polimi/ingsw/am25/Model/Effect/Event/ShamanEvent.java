package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class ShamanEvent extends EventEffect{
    private final int PPToMost;
    private final int PPToLeast;

    public ShamanEvent(int PPToMost, int PPToLeast){
        this.PPToMost = PPToMost;
        this.PPToLeast = PPToLeast;
    }

    @Override
    public void solveEvent(List<Player> playersList) {

    }
}
