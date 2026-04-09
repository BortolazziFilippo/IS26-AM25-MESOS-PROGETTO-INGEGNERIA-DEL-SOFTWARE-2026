package it.polimi.ingsw.am25.Model.Effect.Event;

import it.polimi.ingsw.am25.Model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;
/**
 * Event effect for a shamanic ritual event:
 * the player(s) with the most Shaman stars earn {@code PPToMost} PP;
 * the player(s) with the fewest stars lose {@code PPToLeast} PP.
 * Building effects tagged {@link EVENT_TYPE#SHAMANIC_RIT} are triggered both before
 * and after the PP distribution (enabling effects that snapshot or modify PP around the event).
 */
public class ShamanEvent extends EventEffect{
    private final int PPToMost;
    private final int PPToLeast;
    /**
     * Creates a ShamanEvent.
     *
     * @param PPToMost   PP gained by the player(s) with the most Shaman stars
     * @param PPToLeast  PP lost by the player(s) with the fewest Shaman stars
     */
    public ShamanEvent(int PPToMost, int PPToLeast){
        this.PPToMost = PPToMost;
        this.PPToLeast = PPToLeast;
    }
    /**
     * Applies the shamanic ritual event:
     * <ol>
     *   <li>Triggers SHAMANIC_RIT building effects (first pass — snapshot phase).</li>
     *   <li>Finds the maximum and minimum Shaman-star totals among all players.</li>
     *   <li>Awards/deducts PP accordingly.</li>
     *   <li>Triggers SHAMANIC_RIT building effects again (second pass — resolution phase).</li>
     * </ol>
     *
     * @param playersList the list of players participating in the event
     */
    @Override
    public void solveEvent(List<Player> playersList) {
        for(Player player : playersList){
            player.getBuildingCards().stream()
                    .filter(b->b.getApplyOn() == EVENT_TYPE.SHAMANIC_RIT)
                    .forEach(b-> b.applyBuildingEffect(player));
        }

        int max = playersList.stream().mapToInt(Player::getShamanStarTotal).max().orElse(0);
        int min = playersList.stream().mapToInt(Player::getShamanStarTotal).min().orElse(0);

        for(Player player : playersList){
            int stars = player.getShamanStarTotal();
            if(stars == max) player.managePP(PPToMost);
            if(stars == min) player.managePP(-PPToLeast);
        }

        for(Player player : playersList){
            player.getBuildingCards().stream()
                    .filter(b->b.getApplyOn() == EVENT_TYPE.SHAMANIC_RIT)
                    .forEach(b-> b.applyBuildingEffect(player));
        }
    }
}
