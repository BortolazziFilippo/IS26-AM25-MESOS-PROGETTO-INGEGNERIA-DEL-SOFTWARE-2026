package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;

import java.util.List;

/**
 * Memento of the full game state at end of round.
 * Produced by Game (Originator) and stored/restored by PersistanceLogger/PersistanceLoader (Caretaker).
 */
public record GameMemento(ERA currentEra, GAME_PHASE gamePhase, int playerNumber, String playerToPlaceNickname,
                          String playerToPlayNickname, List<PlayerMemento> players, MarketMemento market,
                          BoardMemento board) {
}
