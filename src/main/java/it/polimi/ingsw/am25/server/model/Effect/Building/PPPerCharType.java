package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

/**
 * Building effect that awards a fixed number of prestige points for each tribe member
 * of a specified card type at end of game (or the configured trigger).
 */
public class PPPerCharType extends BuildingEffect {
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    private final int PrestigePoint;
    private final CARD_TYPE cardType;

    /**
     * Creates a PPPerCharType effect.
     *
     * @param prestigePoint prestige points awarded per matching tribe member
     * @param cardType      the card type to count in the player's tribe
     */
    public PPPerCharType(int prestigePoint, CARD_TYPE cardType) {
        PrestigePoint = prestigePoint;
        this.cardType = cardType;
    }

    /**
     * Awards {@code prestigePoint} PP for each tribe member of the configured {@code cardType}.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        int num_of_occurence = (int) player.getTribe().stream().filter(card -> card.getCardType() == cardType).count();
        int ppGain = num_of_occurence * PrestigePoint;
        UtilitiesFunction.logInfo(LOG_PREFIX,
                "PPPerCharType: player '" + player.getNickname() + "' has " + num_of_occurence +
                        " " + cardType + " card(s), awarding " + ppGain + " PP (" + PrestigePoint + " PP each)");
        player.managePP(ppGain);
    }
}
