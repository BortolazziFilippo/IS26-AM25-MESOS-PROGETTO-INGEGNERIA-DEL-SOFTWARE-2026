package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
/**
 * Building effect triggered during a paintings (artist) event: awards 1 food
 * per Artist card in the player's tribe.
 */
public class OnEventPaintingsOneFoodPerArtist extends BuildingEffect{
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    /**
     * Default constructor for OnEventPaintingsOneFoodPerArtist.
     */
    public OnEventPaintingsOneFoodPerArtist() {
    }
    /**
     * Awards the player 1 food per Artist card in their tribe.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        int artistCount = (int) player.getTribe().stream().filter(card -> card.getCardType() == CARD_TYPE.ARTIST).count();
        UtilitiesFunction.logInfo(LOG_PREFIX,
                "OnEventPaintingsOneFoodPerArtist: player '" + player.getNickname() +
                        "' has " + artistCount + " Artist(s), awarding " + artistCount + " food");
        player.manageFoodAndPP(artistCount);
    }
}
