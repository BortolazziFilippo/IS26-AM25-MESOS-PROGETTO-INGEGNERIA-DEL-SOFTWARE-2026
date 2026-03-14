package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class ArtistEvent extends EventEffect{
    private int artistNeeded;
    private int PPLost;
    private int PPtoMultiply;

    public ArtistEvent(int artistNeeded, int PPLost, int PPtoMultiply){
        this.artistNeeded = artistNeeded;
        this.PPLost = PPLost;
        this.PPtoMultiply = PPtoMultiply;
    }

    @Override
    public void solveEvent(List<Player> playersList) {

    }
}
