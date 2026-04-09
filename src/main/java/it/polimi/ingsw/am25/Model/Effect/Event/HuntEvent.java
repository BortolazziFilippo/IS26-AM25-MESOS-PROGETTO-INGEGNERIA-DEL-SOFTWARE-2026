package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;
/**
 * Event effect for a hunt event:
 * every player receives {@code food} food and {@code PPtoMultiply * hunterCount} prestige points.
 * Building effects tagged {@link EVENT_TYPE#HUNT} are also triggered.
 */
public class HuntEvent extends EventEffect {
    private final int food;
    private final int PPtoMultiply;
    /**
     * Event effect for a hunt event:
     * every player receives {@code food} food and {@code PPtoMultiply * hunterCount} prestige points.
     * Building effects tagged {@link EVENT_TYPE#HUNT} are also triggered.
     */
    public HuntEvent(int food, int PPtoMultiply){
        this.food = food;
        this.PPtoMultiply = PPtoMultiply;
    }


    /**
     * Applies the hunt event: gives food and Hunter-based PP to all players,
     * then triggers all HUNT building effects.
     *
     * @param playersList the list of players participating in the event
     */
    @Override
    public void solveEvent(List<Player> playersList) {
        playersList.forEach(player -> player.manageFoodAndPP(food));
        playersList.forEach(player -> player.managePP(PPtoMultiply*player.getHunterNumber()));
        playersList.forEach(player -> player.getBuildingCards().stream().filter(buildingCard->buildingCard.getApplyOn()== EVENT_TYPE.HUNT).forEach(buildingCard->buildingCard.applyBuildingEffect(player)));
    }
}
