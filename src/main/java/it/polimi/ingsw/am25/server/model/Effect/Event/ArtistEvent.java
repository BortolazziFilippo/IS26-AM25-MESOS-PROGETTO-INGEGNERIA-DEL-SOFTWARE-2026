package it.polimi.ingsw.am25.server.model.Effect.Event;

import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.util.List;

/**
 * Event effect for a paintings (artist) event:
 * players with at least {@code artistNeeded} Artists gain {@code PPtoMultiply * artistCount} PP;
 * players with fewer Artists lose {@code PPLost} PP.
 * Building effects tagged {@link EVENT_TYPE#PAINTINGS} are also triggered.
 */
public class ArtistEvent extends EventEffect{
    private static final String LOG_PREFIX = "[SERVER][EVENT]";
    private final int artistNeeded;
    private final int PPLost;
    private final int PPtoMultiply;

    /**
     * Creates an ArtistEvent.
     *
     * @param artistNeeded  minimum number of Artist cards needed to gain PP
     * @param PPLost        prestige points lost by players who do not meet the threshold
     * @param PPtoMultiply  prestige points multiplier applied per Artist for qualifying players
     */
    public ArtistEvent(int artistNeeded, int PPLost, int PPtoMultiply){
        this.artistNeeded = artistNeeded;
        this.PPLost = PPLost;
        this.PPtoMultiply = PPtoMultiply;
    }

    /**
     * Applies the artist event: rewards qualifying players and penalises the rest,
     * then triggers all PAINTINGS building effects.
     *
     * @param playersList the list of players participating in the event
     */
    @Override
    public void solveEvent(List<Player> playersList) {
        UtilitiesFunction.logInfo(
                LOG_PREFIX,
                "PAINTINGS event started: players with at least " + artistNeeded + " Artists gain " +
                        PPtoMultiply + " PP per Artist, others lose " + PPLost + " PP"
        );
        for (Player player : playersList) {
            int artistCount = player.getArtistNumber();
            if (artistCount >= artistNeeded) {
                int ppGain = PPtoMultiply * artistCount;
                UtilitiesFunction.logInfo(
                        LOG_PREFIX,
                        "PAINTINGS event on player '" + player.getNickname() + "': artists=" + artistCount +
                                ", PP delta=" + ppGain
                );
                player.managePP(ppGain);
            } else {
                UtilitiesFunction.logInfo(
                        LOG_PREFIX,
                        "PAINTINGS event on player '" + player.getNickname() + "': artists=" + artistCount +
                                ", PP delta=-" + PPLost
                );
                player.managePP(-PPLost);
            }
            List<BuildingCard> triggeredBuildings = player.getBuildingCards().stream()
                    .filter(buildingCard -> buildingCard.getApplyOn() == EVENT_TYPE.PAINTINGS)
                    .toList();
            triggeredBuildings.forEach(buildingCard -> buildingCard.applyBuildingEffect(player));
            UtilitiesFunction.logInfo(
                    LOG_PREFIX,
                    "PAINTINGS event triggered " + triggeredBuildings.size() + " building effect(s) for player '" +
                            player.getNickname() + "'"
            );
        }
        UtilitiesFunction.logInfo(LOG_PREFIX, "PAINTINGS event completed");
    }
}
