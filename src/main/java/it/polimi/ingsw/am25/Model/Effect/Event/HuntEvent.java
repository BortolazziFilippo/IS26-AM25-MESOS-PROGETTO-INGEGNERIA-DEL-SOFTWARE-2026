package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class HuntEvent extends EventEffect {
    private final int food;
    private final int PPtoMultiply;

    public HuntEvent(int food, int PPtoMultiply){
        this.food = food;
        this.PPtoMultiply = PPtoMultiply;
    }

    @Override
    public void solveEvent(List<Player> playersList) {
        playersList.forEach(player -> player.manageFood(food));
        playersList.forEach(player -> player.managePP(PPtoMultiply*player.getHunterNumber()));
        playersList.forEach(player -> player.getBuildingCards().stream().filter(buildingCard->buildingCard.getApplyOn()== EVENT_TYPE.HUNT).forEach(buildingCard->buildingCard.applyBuildingEffect(player)));
    }
}
