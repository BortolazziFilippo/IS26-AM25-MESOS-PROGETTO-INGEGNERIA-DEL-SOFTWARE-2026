package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

/**
 * Building effect that prevents the player from losing prestige points during a shamanic
 * ritual event. On the first call it snapshots the player's PP before the event; on the
 * second call it restores any PP that was lost.
 */
public class NoPPLostOnShaman extends BuildingEffect{
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    private int prevPP;
    private boolean flag = false;

    public NoPPLostOnShaman() {

    }

    /**
     * First invocation (before the event): snapshots the player's current PP.
     * Second invocation (after the event): if PP decreased, restores the lost amount.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        if(!flag){
            prevPP = player.getPrestigePoint();
            flag = true;
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "NoPPLostOnShaman: snapshotted PP=" + prevPP + " for player '" + player.getNickname() + "' before shamanic event");
        }else{
            if(player.getPrestigePoint()<prevPP){
                int restored = prevPP - player.getPrestigePoint();
                UtilitiesFunction.logInfo(LOG_PREFIX,
                        "NoPPLostOnShaman: player '" + player.getNickname() + "' lost " + restored +
                                " PP during shamanic event, restoring them (PP " + player.getPrestigePoint() + " -> " + prevPP + ")");
                player.managePP(restored);
                flag = false;
            } else {
                UtilitiesFunction.logInfo(LOG_PREFIX,
                        "NoPPLostOnShaman: player '" + player.getNickname() + "' did not lose PP during shamanic event, no restoration needed");
                flag = false;
            }
        }
    }
}
