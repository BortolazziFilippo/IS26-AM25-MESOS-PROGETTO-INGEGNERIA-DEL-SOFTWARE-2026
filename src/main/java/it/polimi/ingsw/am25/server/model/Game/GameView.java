package it.polimi.ingsw.am25.server.model.Game;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

public interface GameView {

    int getPlayerNumber();

    List<Player> getPlayerList();

    ERA getCurrentEra();
    void nextEra();
}
