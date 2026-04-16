package it.polimi.ingsw.am25.server.webLayer.DTOs;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Player.Player;

import java.util.List;

public class GameDTO {
    private List<PlayerDTO> winners;
    private ERA currentEra;
    private GAME_PHASE currentGamePhase;
    private String playerToPlace;
    private String playerToPlay;

    public List<PlayerDTO> getWinners() {
        return winners;
    }

    public void setWinners(List<PlayerDTO> winners) {
        this.winners = winners;
    }

    public ERA getCurrentEra() {
        return currentEra;
    }

    public void setCurrentEra(ERA currentEra) {
        this.currentEra = currentEra;
    }

    public GAME_PHASE getCurrentGamePhase() {
        return currentGamePhase;
    }

    public void setCurrentGamePhase(GAME_PHASE currentGamePhase) {
        this.currentGamePhase = currentGamePhase;
    }

    public String getPlayerToPlace() {
        return playerToPlace;
    }

    public void setPlayerToPlace(String playerToPlace) {
        this.playerToPlace = playerToPlace;
    }

    public String getPlayerToPlay() {
        return playerToPlay;
    }

    public void setPlayerToPlay(String playerToPlay) {
        this.playerToPlay = playerToPlay;
    }
}
