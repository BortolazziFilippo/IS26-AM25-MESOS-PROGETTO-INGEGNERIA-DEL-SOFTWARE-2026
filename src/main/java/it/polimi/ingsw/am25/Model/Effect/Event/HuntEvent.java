package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class HuntEvent extends EventEffect {
    private int food;
    private int PPtoMultiply;

    public HuntEvent(int food, int PPtoMultiply){
        this.food = food;
        this.PPtoMultiply = PPtoMultiply;
    }

    @Override
    public void solveEvent(List<Player> playersList) {

    }
}
