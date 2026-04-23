package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class GameDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<PlayerDTO> winners;
    private ERA currentEra;
    private GAME_PHASE currentGamePhase;
    private String playerToPlace;
    private String playerToPlay;

    /**
     * Creates a new game dto instance.
     * @param currentEra parameter currentEra.
     * @param currentGamePhase parameter currentGamePhase.
     * @param playerToPlace parameter playerToPlace.
     * @param playerToPlay parameter playerToPlay.
     */
    public GameDTO(ERA currentEra,  GAME_PHASE currentGamePhase, String playerToPlace, String playerToPlay) {
        this.currentEra = currentEra;
        this.winners=null;
        this.currentGamePhase = currentGamePhase;
        this.playerToPlace = playerToPlace;
        this.playerToPlay = playerToPlay;
    }

    /**
     * Returns winners.
     * @return the result of the operation.
     */
    public List<PlayerDTO> getWinners() {
        return winners;
    }

    /**
     * Sets winners.
     * @param winners parameter winners.
     */
    public void setWinners(List<PlayerDTO> winners) {
        this.winners = winners;
    }

    /**
     * Returns current era.
     * @return the result of the operation.
     */
    public ERA getCurrentEra() {
        return currentEra;
    }

    /**
     * Sets current era.
     * @param currentEra parameter currentEra.
     */
    public void setCurrentEra(ERA currentEra) {
        this.currentEra = currentEra;
    }

    /**
     * Returns current game phase.
     * @return the result of the operation.
     */
    public GAME_PHASE getCurrentGamePhase() {
        return currentGamePhase;
    }

    /**
     * Sets current game phase.
     * @param currentGamePhase parameter currentGamePhase.
     */
    public void setCurrentGamePhase(GAME_PHASE currentGamePhase) {
        this.currentGamePhase = currentGamePhase;
    }

    /**
     * Returns player to place.
     * @return the result of the operation.
     */
    public String getPlayerToPlace() {
        return playerToPlace;
    }

    /**
     * Sets player to place.
     * @param playerToPlace parameter playerToPlace.
     */
    public void setPlayerToPlace(String playerToPlace) {
        this.playerToPlace = playerToPlace;
    }

    /**
     * Returns player to play.
     * @return the result of the operation.
     */
    public String getPlayerToPlay() {
        return playerToPlay;
    }

    /**
     * Sets player to play.
     * @param playerToPlay parameter playerToPlay.
     */
    public void setPlayerToPlay(String playerToPlay) {
        this.playerToPlay = playerToPlay;
    }
}
