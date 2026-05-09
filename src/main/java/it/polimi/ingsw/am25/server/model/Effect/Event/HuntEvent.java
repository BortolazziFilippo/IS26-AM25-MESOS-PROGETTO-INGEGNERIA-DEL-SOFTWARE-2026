package it.polimi.ingsw.am25.server.model.Effect.Event;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.util.List;

/**
 * Event effect for a hunt event:
 * every player receives {@code food} food and {@code PPtoMultiply * hunterCount} prestige points.
 * Building effects tagged {@link EVENT_TYPE#HUNT} are also triggered.
 */
public class HuntEvent extends EventEffect {
    private static final String LOG_PREFIX = "[SERVER][EVENT]";
    private final int food;
    private final int PPtoMultiply;

    /**
     * Creates a hunt event effect.
     *
     * @param food         the flat food bonus granted to every player.
     * @param PPtoMultiply the prestige-point multiplier applied per Hunter card in the tribe.
     */
    public HuntEvent(int food, int PPtoMultiply) {
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
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "HUNT event started: all players gain " + food + " food and " + PPtoMultiply + " PP per Hunter"
        );
        for (Player player : playersList) {
            int hunterCount = player.getHunterNumber();
            int ppGain = PPtoMultiply * hunterCount;
            UtilitiesFunction.logInfo(
                    LOG_PREFIX,
                    "HUNT event on player '" + player.getNickname() + "': hunters=" + hunterCount +
                            ", food delta=" + food + ", PP delta=" + ppGain
            );
            player.manageFoodAndPP(food);
            player.managePP(ppGain);
            List<BuildingCard> triggeredBuildings = player.getBuildingCards().stream()
                    .filter(buildingCard -> buildingCard.getApplyOn() == EVENT_TYPE.HUNT)
                    .toList();
            triggeredBuildings.forEach(buildingCard -> buildingCard.applyBuildingEffect(player));
            UtilitiesFunction.logInfo(
                    LOG_PREFIX,
                    "HUNT event triggered " + triggeredBuildings.size() + " building effect(s) for player '" +
                            player.getNickname() + "'"
            );
        }
        UtilitiesFunction.logInfo(LOG_PREFIX, "HUNT event completed");
    }
}
