package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Card.ShamanCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

/**
 * Building effect that temporarily adds a 3-star Shaman card to the player's tribe
 * before a shamanic ritual event, then removes it afterwards.
 * This effectively grants the player 3 extra Shaman stars for the duration of that event.
 */
public class ThreeMoreShamanStar extends BuildingEffect{
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    private boolean flag = false;
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
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "ThreeMoreShamanStar: adding temporary 3-star Shaman card to player '" +
                            player.getNickname() + "' tribe for shamanic ritual event");
            player.addCardToTribe(new ShamanCard(ERA.ERA_I, CARD_TYPE.SHAMAN, SHAMAN_STAR.THREE));
            flag = true;
        }else{
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "ThreeMoreShamanStar: removing temporary 3-star Shaman card from player '" +
                            player.getNickname() + "' tribe after shamanic ritual event");
            player.getTribe().remove(player.getTribe().size() - 1);
            flag = false;
        }
    }
}
