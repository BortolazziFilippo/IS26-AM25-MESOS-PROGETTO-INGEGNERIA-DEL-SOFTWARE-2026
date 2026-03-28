package it.polimi.ingsw.am25.Model.Effect.Building;

import it.polimi.ingsw.am25.Model.Board.BoardView;
import it.polimi.ingsw.am25.Model.Player.Player;

public class PlusOneFoodOnReturnDefaultTile extends BuildingEffect{
    private BoardView boardView;
    public PlusOneFoodOnReturnDefaultTile() {
    }

    public void setBoardView(BoardView boardView) {
        this.boardView = boardView;
    }

    @Override
    public void applyEffect(Player player) {
        /*Se fai boardView.isPlayerOnAnEligibleDefaultTile(player); questo metodo ti restituisce true se un player è su una casella con cibo >=0 (dalle regole non
        da il bonus solo se si è su una casella con cibo negativo)
        Per testarlo è quindi necessario creare una Board, fare buildingEffect.setBoardView(Board boardCheHaiCreato).
        Mettere il player su una DefaultTile Board.placePlayerOnDefaultTile()
        * */

        player.manageFoodAndPP(1);
    }
}
