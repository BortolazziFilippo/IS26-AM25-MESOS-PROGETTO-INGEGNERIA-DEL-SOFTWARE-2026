package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;

import java.util.List;

/**
 * Memento of the full game state at end of round.
 * Produced by Game (Originator) and stored/restored by PersistanceLogger/PersistanceLoader (Caretaker).
 *
 * @param currentEra             the era the game is currently in.
 * @param gamePhase              the current phase of the game.
 * @param playerNumber           the total number of players in this game.
 * @param playerToPlaceNickname  the nickname of the player who must place their totem next.
 * @param playerToPlayNickname   the nickname of the player whose turn it is to play.
 * @param players                the list of player state snapshots.
 * @param market                 the market state snapshot.
 * @param board                  the board state snapshot.
 */
public record GameMemento(ERA currentEra, GAME_PHASE gamePhase, int playerNumber, String playerToPlaceNickname,
                          String playerToPlayNickname, List<PlayerMemento> players, MarketMemento market,
                          BoardMemento board) {
}
