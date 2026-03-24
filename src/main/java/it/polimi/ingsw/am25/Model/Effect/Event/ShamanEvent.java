package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class ShamanEvent extends EventEffect{
    private final int PPToMost;
    private final int PPToLeast;

    public ShamanEvent(int PPToMost, int PPToLeast){
        this.PPToMost = PPToMost;
        this.PPToLeast = PPToLeast;
    }

    @Override
    public void solveEvent(List<Player> playersList) {
        int max = playersList.stream().mapToInt(Player ::getShamanStarTotal).max().orElse(0);
        int min = playersList.stream().mapToInt(Player::getShamanStarTotal).min().orElse(0);

        for(Player player : playersList){
            int stars = player.getShamanStarTotal();
            if(stars == max) player.managePP(PPToMost);
            if(stars == min) player.managePP(-PPToLeast);
        }

        for(Player player : playersList){
            player.getBuildingCards().stream().filter(b->b.getApplyOn() == EVENT_TYPE.SHAMANIC_RIT)
                    .forEach(b-> b.applyBuildingEffect(player));
        }
    }
}
