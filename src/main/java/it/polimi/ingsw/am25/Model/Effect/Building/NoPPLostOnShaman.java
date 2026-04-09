package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

/**
 * Building effect that prevents the player from losing prestige points during a shamanic
 * ritual event. On the first call it snapshots the player's PP before the event; on the
 * second call it restores any PP that was lost.
 */
public class NoPPLostOnShaman extends BuildingEffect{
    private int prevPP;
    private boolean flag = false;

    /**
     * Default constructor for NoPPLostOnShaman.
     */
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
        }else{
            if(player.getPrestigePoint()<prevPP){
                player.managePP(prevPP - player.getPrestigePoint());
                flag = false;
            }
        }
    }
}
