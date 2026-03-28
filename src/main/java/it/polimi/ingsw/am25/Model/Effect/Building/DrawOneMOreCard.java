package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

public class DrawOneMOreCard extends BuildingEffect {
    private final boolean turnFinished = false; /* flag per notificare la fine di un round, da rivedere il metodo da fare in game*/

    public DrawOneMOreCard() {
    }

    @Override
    public void applyEffect(Player player) {
        // serve l'implementazione del controller
        // questo effetto chiama la funzione in market di selezione carta da TopList, però mi serve la posizione che sceglie player
    }
}
