package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

/**
 * Building effect that doubles any prestige points gained during a shamanic ritual event.
 * On the first call it records the player's PP before the event; on the second call it
 * awards the difference a second time (effectively doubling the gain).
 */
public class DoublePPOnShamanEvent extends BuildingEffect {
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    private boolean flag = false;
    private int prevPP;

    /**
     * Constructs a building effect that doubles the prestige points gained during a shamanic ritual event.
     */
    public DoublePPOnShamanEvent() {
    }

    /**
     * First invocation (before the event): snapshots the player's current PP.
     * Second invocation (after the event): if PP increased, awards the same delta again.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        if (!flag) {
            prevPP = player.getPrestigePoint();
            flag = true;
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "DoublePPOnShamanEvent: snapshotted PP=" + prevPP + " for player '" + player.getNickname() + "' before shamanic event");
        } else {
            if (player.getPrestigePoint() > prevPP) {
                int gained = player.getPrestigePoint() - prevPP;
                UtilitiesFunction.logInfo(LOG_PREFIX,
                        "DoublePPOnShamanEvent: player '" + player.getNickname() + "' gained " + gained +
                                " PP during shamanic event, awarding another " + gained + " PP to double the gain");
                player.managePP(gained);
                flag = false;
            } else {
                UtilitiesFunction.logInfo(LOG_PREFIX,
                        "DoublePPOnShamanEvent: player '" + player.getNickname() + "' did not gain PP during shamanic event, nothing to double");
                flag = false;
            }
        }
    }
}
