package it.polimi.ingsw.am25.server.model.Effect.Event;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.util.List;
/**
 * Event effect for a shamanic ritual event:
 * the player(s) with the most Shaman stars earn {@code PPToMost} PP;
 * the player(s) with the fewest stars lose {@code PPToLeast} PP.
 * Building effects tagged {@link EVENT_TYPE#SHAMANIC_RIT} are triggered both before
 * and after the PP distribution (enabling effects that snapshot or modify PP around the event).
 */
public class ShamanEvent extends EventEffect{
    private static final String LOG_PREFIX = "[SERVER][EVENT]";
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
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "SHAMANIC_RIT event started: top stars gain " + PPToMost + " PP, bottom stars lose " + PPToLeast + " PP"
        );
        for(Player player : playersList){
            List<BuildingCard> triggeredBuildings = player.getBuildingCards().stream()
                    .filter(b->b.getApplyOn() == EVENT_TYPE.SHAMANIC_RIT)
                    .toList();
            triggeredBuildings.forEach(b -> b.applyBuildingEffect(player));
            UtilitiesFunction.logInfo(
                    LOG_PREFIX,
                    "SHAMANIC_RIT pre-resolution triggered " + triggeredBuildings.size() + " building effect(s) for player '" +
                            player.getNickname() + "'"
            );
        }

        int max = playersList.stream().mapToInt(Player::getShamanStarTotal).max().orElse(0);
        int min = playersList.stream().mapToInt(Player::getShamanStarTotal).min().orElse(0);
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "SHAMANIC_RIT stars range resolved: max=" + max + ", min=" + min
        );

        for(Player player : playersList){
            int stars = player.getShamanStarTotal();
            if(stars == max) {
                UtilitiesFunction.logInfo(
                        LOG_PREFIX,
                        "Player '" + player.getNickname() + "' has max stars (" + stars + "), PP delta=" + PPToMost
                );
                player.managePP(PPToMost);
            }
            if(stars == min) {
                UtilitiesFunction.logInfo(
                        LOG_PREFIX,
                        "Player '" + player.getNickname() + "' has min stars (" + stars + "), PP delta=-" + PPToLeast
                );
                player.managePP(PPToLeast);
            }
        }

        for(Player player : playersList){
            List<BuildingCard> triggeredBuildings = player.getBuildingCards().stream()
                    .filter(b->b.getApplyOn() == EVENT_TYPE.SHAMANIC_RIT)
                    .toList();
            triggeredBuildings.forEach(b -> b.applyBuildingEffect(player));
            UtilitiesFunction.logInfo(
                    LOG_PREFIX,
                    "SHAMANIC_RIT post-resolution triggered " + triggeredBuildings.size() + " building effect(s) for player '" +
                            player.getNickname() + "'"
            );
        }
        UtilitiesFunction.logInfo(LOG_PREFIX, "SHAMANIC_RIT event completed");
    }
}
