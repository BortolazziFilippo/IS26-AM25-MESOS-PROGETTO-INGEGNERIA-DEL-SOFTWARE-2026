package it.polimi.ingsw.am25.server.model.Effect.Event;

import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

public interface EventEffectInterface {

    void solveEvent(List<Player> playersList);
}
