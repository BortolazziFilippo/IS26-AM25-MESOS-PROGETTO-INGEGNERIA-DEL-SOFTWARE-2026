package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Data-transfer object for the overall game state, carrying the current era, phase,
 * and which players are next to place and play.
 */
public class GameDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /** Ordered list of winning players; {@code null} while the game is ongoing. */
    private List<PlayerDTO> winners;
    /** The era the game is currently in. */
    private ERA currentEra;
    /** The current phase of the game. */
    private GAME_PHASE currentGamePhase;
    /** Nickname of the player who must place their totem next. */
    private String playerToPlace;
    /** Nickname of the player whose turn it is to draw/play. */
    private String playerToPlay;

    /**
     * Creates a GameDTO with the given state fields. Winners are initially {@code null}.
     *
     * @param currentEra       the era the game is currently in.
     * @param currentGamePhase the current phase of the game.
     * @param playerToPlace    nickname of the player who must place their totem.
     * @param playerToPlay     nickname of the player whose turn it is to play.
     */
    public GameDTO(ERA currentEra, GAME_PHASE currentGamePhase, String playerToPlace, String playerToPlay) {
        this.currentEra = currentEra;
        this.winners = null;
        this.currentGamePhase = currentGamePhase;
        this.playerToPlace = playerToPlace;
        this.playerToPlay = playerToPlay;
    }

    /**
     * Returns the ordered list of winners once the game ends.
     *
     * @return the ordered list of winners, or {@code null} if the game is still ongoing.
     */
    public List<PlayerDTO> getWinners() {
        return winners;
    }

    /**
     * Sets the ordered list of winners at end of game.
     *
     * @param winners the ordered list of winners to set.
     */
    public void setWinners(List<PlayerDTO> winners) {
        this.winners = winners;
    }

    /**
     * Returns the era the game is currently in.
     *
     * @return the current era.
     */
    public ERA getCurrentEra() {
        return currentEra;
    }

    /**
     * Sets the current era.
     *
     * @param currentEra the new current era.
     */
    public void setCurrentEra(ERA currentEra) {
        this.currentEra = currentEra;
    }

    /**
     * Returns the current phase of the game.
     *
     * @return the current game phase.
     */
    public GAME_PHASE getCurrentGamePhase() {
        return currentGamePhase;
    }

    /**
     * Sets the current game phase.
     *
     * @param currentGamePhase the new game phase.
     */
    public void setCurrentGamePhase(GAME_PHASE currentGamePhase) {
        this.currentGamePhase = currentGamePhase;
    }

    /**
     * Returns the nickname of the player who must place their totem next.
     *
     * @return nickname of the player who must place their totem.
     */
    public String getPlayerToPlace() {
        return playerToPlace;
    }

    /**
     * Sets the nickname of the player who must place their totem next.
     *
     * @param playerToPlace nickname of the player who must place their totem.
     */
    public void setPlayerToPlace(String playerToPlace) {
        this.playerToPlace = playerToPlace;
    }

    /**
     * Returns the nickname of the player whose turn it is to draw/play.
     *
     * @return nickname of the player whose turn it is to play.
     */
    public String getPlayerToPlay() {
        return playerToPlay;
    }

    /**
     * Sets the nickname of the player whose turn it is to draw/play.
     *
     * @param playerToPlay nickname of the player whose turn it is to play.
     */
    public void setPlayerToPlay(String playerToPlay) {
        this.playerToPlay = playerToPlay;
    }
}
