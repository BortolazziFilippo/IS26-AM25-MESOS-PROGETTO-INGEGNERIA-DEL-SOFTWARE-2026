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
    private List<PlayerDTO> winners;
    private ERA currentEra;
    private GAME_PHASE currentGamePhase;
    private String playerToPlace;
    private String playerToPlay;

    /**
     * @param currentEra       the era the game is currently in.
     * @param currentGamePhase the current phase of the game.
     * @param playerToPlace    nickname of the player who must place their totem.
     * @param playerToPlay     nickname of the player whose turn it is to play.
     */
    public GameDTO(ERA currentEra,  GAME_PHASE currentGamePhase, String playerToPlace, String playerToPlay) {
        this.currentEra = currentEra;
        this.winners=null;
        this.currentGamePhase = currentGamePhase;
        this.playerToPlace = playerToPlace;
        this.playerToPlay = playerToPlay;
    }

    /** @return the ordered list of winners, or {@code null} if the game is still ongoing. */
    public List<PlayerDTO> getWinners() {
        return winners;
    }

    /** @param winners the ordered list of winners to set. */
    public void setWinners(List<PlayerDTO> winners) {
        this.winners = winners;
    }

    /** @return the era the game is currently in. */
    public ERA getCurrentEra() {
        return currentEra;
    }

    /** @param currentEra the new current era. */
    public void setCurrentEra(ERA currentEra) {
        this.currentEra = currentEra;
    }

    /** @return the current phase of the game. */
    public GAME_PHASE getCurrentGamePhase() {
        return currentGamePhase;
    }

    /** @param currentGamePhase the new game phase. */
    public void setCurrentGamePhase(GAME_PHASE currentGamePhase) {
        this.currentGamePhase = currentGamePhase;
    }

    /** @return nickname of the player who must place their totem. */
    public String getPlayerToPlace() {
        return playerToPlace;
    }

    /** @param playerToPlace nickname of the player who must place their totem. */
    public void setPlayerToPlace(String playerToPlace) {
        this.playerToPlace = playerToPlace;
    }

    /** @return nickname of the player whose turn it is to play. */
    public String getPlayerToPlay() {
        return playerToPlay;
    }

    /** @param playerToPlay nickname of the player whose turn it is to play. */
    public void setPlayerToPlay(String playerToPlay) {
        this.playerToPlay = playerToPlay;
    }
}
