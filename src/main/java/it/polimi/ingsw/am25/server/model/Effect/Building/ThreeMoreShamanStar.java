package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Card.ShamanCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.model.Player.Player;

/**
 * Building effect that temporarily adds a 3-star Shaman card to the player's tribe
 * before a shamanic ritual event, then removes it afterwards.
 * This effectively grants the player 3 extra Shaman stars for the duration of that event.
 */
public class ThreeMoreShamanStar extends BuildingEffect{
    private boolean flag = false;
    /**
     * Default constructor for ThreeMoreShamanStar.
     */
    public ThreeMoreShamanStar() {
    }

    /**
     * First invocation (before the event): adds a temporary 3-star Shaman card to the tribe.
     * Second invocation (after the event): removes the last card added (the temporary Shaman).
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        if(!flag) {
            player.addCardToTribe(new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE));
            flag = true;
        }else{
            player.getTribe().remove(player.getTribe().size() - 1);
            flag = false;
        }
    }
}
