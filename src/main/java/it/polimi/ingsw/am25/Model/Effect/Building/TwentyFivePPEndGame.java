package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Player.Player;

public class TwentyFivePPEndGame extends BuildingEffect{
    public TwentyFivePPEndGame() {
    }

    @Override
    public void applyEffect(Player player) {
        player.managePP(25);

    }
    /* non so se va bene solo questo, però in teoria il metodo che annuncia il fine della partita lancia questo metodo
    * e modifica semplicemnte il PP */
}
