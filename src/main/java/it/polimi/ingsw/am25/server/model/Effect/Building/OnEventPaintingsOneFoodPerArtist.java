package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
/**
 * Building effect triggered during a paintings (artist) event: awards 1 food
 * per Artist card in the player's tribe.
 */
public class OnEventPaintingsOneFoodPerArtist extends BuildingEffect{
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
        player.manageFoodAndPP( (int) player.getTribe().stream().filter(card -> card.getCardType()== CARD_TYPE.ARTIST).count() );
    }
}
