package it.polimi.ingsw.am25.server.model.persistance;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

import java.util.List;

/**
 * Memento snapshot of a single player's state at end of round.
 */
public class PlayerMemento {
    private final String nickname;
    private final COLOR totemColor;
    private final int food;
    private final int prestigePoints;
    private final List<CardDTO> tribe;
    private final List<Integer> buildingIDs;

    public PlayerMemento(String nickname, COLOR totemColor, int food, int prestigePoints,
                         List<CardDTO> tribe, List<Integer> buildingIDs) {
        this.nickname = nickname;
        this.totemColor = totemColor;
        this.food = food;
        this.prestigePoints = prestigePoints;
        this.tribe = tribe;
        this.buildingIDs = buildingIDs;
    }

    public String getNickname() { return nickname; }
    public COLOR getTotemColor() { return totemColor; }
    public int getFood() { return food; }
    public int getPrestigePoints() { return prestigePoints; }
    public List<CardDTO> getTribe() { return tribe; }
    public List<Integer> getBuildingIDs() { return buildingIDs; }
}
