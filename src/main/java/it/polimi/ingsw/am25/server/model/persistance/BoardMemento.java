package it.polimi.ingsw.am25.server.model.persistance;

import java.util.List;

/**
 * Memento snapshot of the board state at end of round.
 * At end of round all players are on default tiles, so only their ordered list is needed.
 */
public record BoardMemento(List<String> orderedNicknamesOnDefaultTiles) {
}
