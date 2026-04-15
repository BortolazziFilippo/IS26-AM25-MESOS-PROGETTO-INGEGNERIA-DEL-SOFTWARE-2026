package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Player.Player;
/**
 * Building effect that doubles any prestige points gained during a shamanic ritual event.
 * On the first call it records the player's PP before the event; on the second call it
 * awards the difference a second time (effectively doubling the gain).
 */
public class DoublePPOnShamanEvent extends BuildingEffect{
    private boolean flag = false;
    private int prevPP;

    /**
     * Default constructor for DoublePPOnShamanEvent.
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
        if(!flag){
            prevPP = player.getPrestigePoint();
            flag = true;
        }else{
            if(player.getPrestigePoint()>prevPP){
                player.managePP(player.getPrestigePoint()-prevPP);
                flag = false;
            }
        }
    }
}
