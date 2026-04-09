package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;
/**
 * Building effect that entitles the player to draw one extra card per round from the top list.
 * The actual card selection is delegated to the controller layer, which must invoke the
 * appropriate market method once this effect is triggered.
 */
public class DrawOneMoreCard extends BuildingEffect {
    private final boolean turnFinished = false; /* flag per notificare la fine di un round, da rivedere il metodo da fare in game*/

    /**
     * Default constructor for DrawOneMoreCard.
     */
    public DrawOneMoreCard() {
    }

    /**
     * Applies the "draw one more card" effect.
     * The actual card selection requires controller-layer intervention
     * (the player must choose a position in the top card list).
     *
     * @param player the player who owns this building
     */
    @Override
    public void applyEffect(Player player) {
        // serve l'implementazione del controller
        // questo effetto chiama la funzione in market di selezione carta da TopList, però mi serve la posizione che sceglie player
    }
}
