package it.polimi.ingsw.am25.server.model.Effect.Building;

import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Building effect that awards 6 prestige points for each complete set of one of each of
 * the six tribe-card types that was added since the last evaluation.
 */
public class SetSixCard extends BuildingEffect{
    private static final String LOG_PREFIX = "[SERVER][EFFECT]";
    private final int foodToGive = 5;
    private List<Card> oldCards = new ArrayList<>();
    public SetSixCard() {
    }
    /**
     * Counts one of each of the six card types among newly added tribe members.
     * For each complete set found, awards 6 prestige points.
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        int setCompleti;
        int quantity = 6;
        // Take the full current tribe as our working copy
        List<Card> fullTribe = new ArrayList<>(player.getTribe());
        List<Card> newCards = new ArrayList<>(fullTribe);
        List<Integer> setCards = new ArrayList<>(Collections.nCopies(quantity, 0));
        // Keep only cards added since the last call (the "diff")
        newCards.removeAll(oldCards);
        UtilitiesFunction.countOccurrence(newCards, setCards);
        // Save the full tribe state for the next call; must be a new list
        // to avoid aliasing: assigning newCards directly and then clearing it
        // would also empty oldCards (they would point to the same object).
        oldCards = fullTribe;
        if (!setCards.contains(0)) {
            setCompleti = Collections.min(setCards);
            int ppGain = 6 * setCompleti;
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "SetSixCard: player '" + player.getNickname() + "' completed " + setCompleti +
                            " full set(s) of all 6 card types, awarding " + ppGain + " PP");
            player.managePP(ppGain);
        } else {
            UtilitiesFunction.logInfo(LOG_PREFIX,
                    "SetSixCard: player '" + player.getNickname() + "' has not completed a full set this evaluation, no PP awarded");
        }
    }
}
