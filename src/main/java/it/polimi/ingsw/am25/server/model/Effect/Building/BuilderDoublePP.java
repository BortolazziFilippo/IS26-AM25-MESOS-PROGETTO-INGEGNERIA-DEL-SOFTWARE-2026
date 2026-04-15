package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Card.BuilderCard;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
/**
 * Building effect that doubles the final prestige points of every Builder card in the player's tribe.
 * Triggered at the end of the game.
 */
public class BuilderDoublePP extends BuildingEffect{

    /**
     * Default constructor for BuilderDoublePP.
     */
    public BuilderDoublePP() {
    }

    /**
     * Doubles the {@code finalPrestigePoint} value of each Builder card in the player's tribe.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        player.getTribe().stream().filter(card -> card.getCardType()== CARD_TYPE.BUILDER).map(card -> (BuilderCard)card).forEach(card -> card.setFinalPrestigePoint(card.getFinalPrestigePoint()*2) );
    }
}
