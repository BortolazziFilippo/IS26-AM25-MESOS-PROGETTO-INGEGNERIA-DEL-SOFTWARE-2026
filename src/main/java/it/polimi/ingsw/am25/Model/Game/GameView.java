package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public interface GameView {

    int getPlayerNumber();

    List<Player> getPlayerList();
}
