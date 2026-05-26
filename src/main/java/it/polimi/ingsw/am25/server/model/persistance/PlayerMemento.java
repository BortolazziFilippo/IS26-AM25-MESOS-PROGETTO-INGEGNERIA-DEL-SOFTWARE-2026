package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

import java.util.List;

/**
 * Memento snapshot of a single player's state at end of round.
 *
 * @param nickname       the player's unique nickname.
 * @param totemColor     the color of the player's totem.
 * @param food           the player's current food supply.
 * @param prestigePoints the player's accumulated prestige points.
 * @param tribe          the list of tribe-member card DTOs in the player's hand.
 * @param buildingIDs    the IDs of buildings owned by the player.
 */
public record PlayerMemento(String nickname, COLOR totemColor, int food, int prestigePoints, List<CardDTO> tribe,
                            List<Integer> buildingIDs) {
}
