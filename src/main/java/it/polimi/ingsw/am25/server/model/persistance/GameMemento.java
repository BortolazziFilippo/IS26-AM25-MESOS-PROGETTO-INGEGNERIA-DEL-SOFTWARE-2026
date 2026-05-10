package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;

import java.util.List;

/**
 * Memento of the full game state at end of round.
 * Produced by Game (Originator) and stored/restored by PersistanceLogger/PersistanceLoader (Caretaker).
 */
public class GameMemento {
    private final ERA currentEra;
    private final GAME_PHASE gamePhase;
    private final int playerNumber;
    private final String playerToPlaceNickname;
    private final String playerToPlayNickname;
    private final List<PlayerMemento> players;
    private final MarketMemento market;
    private final BoardMemento board;

    public GameMemento(ERA currentEra, GAME_PHASE gamePhase, int playerNumber,
                       String playerToPlaceNickname, String playerToPlayNickname,
                       List<PlayerMemento> players, MarketMemento market, BoardMemento board) {
        this.currentEra = currentEra;
        this.gamePhase = gamePhase;
        this.playerNumber = playerNumber;
        this.playerToPlaceNickname = playerToPlaceNickname;
        this.playerToPlayNickname = playerToPlayNickname;
        this.players = players;
        this.market = market;
        this.board = board;
    }

    public ERA getCurrentEra() { return currentEra; }
    public GAME_PHASE getGamePhase() { return gamePhase; }
    public int getPlayerNumber() { return playerNumber; }
    public String getPlayerToPlaceNickname() { return playerToPlaceNickname; }
    public String getPlayerToPlayNickname() { return playerToPlayNickname; }
    public List<PlayerMemento> getPlayers() { return players; }
    public MarketMemento getMarket() { return market; }
    public BoardMemento getBoard() { return board; }
}
