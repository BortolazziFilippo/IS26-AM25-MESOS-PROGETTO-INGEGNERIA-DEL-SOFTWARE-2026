package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

import java.util.List;

/**
 * Memento snapshot of a single player's state at end of round.
 */
public record PlayerMemento(String nickname, COLOR totemColor, int food, int prestigePoints, List<CardDTO> tribe,
                            List<Integer> buildingIDs) {
}
