package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.BoardView;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    private List<Player> placingOrder;
    private List<Player> playingOrder;
    private int currentPlayingPlayer;
    private int currentPlacingPlayer;
    private final BoardView boardView;

    public TurnManager(BoardView boardView) {
        this.placingOrder = new ArrayList<>();
        this.playingOrder= new ArrayList<>();
        this.boardView = boardView;
        this.currentPlacingPlayer=0;
        this.currentPlayingPlayer=0;
    }
    public Player getCurrentPlayingPlayer(){
        return playingOrder.get(currentPlayingPlayer);
    }
    public Player getCurrentPlacingPlayer(){
        return playingOrder.get(currentPlacingPlayer);
    }
    


    public List<Player> getPlacingOrder() {
        return placingOrder;
    }

    public List<Player> getPlayingOrder() {
        return playingOrder;
    }
    public void updatePlayingOrder(){
        this.playingOrder= boardView.getOrderedPlayerOnOfferTile();
    }
    public void updatePlacingOrder(){
        this.placingOrder = boardView.getOrderedPlayerOnDefaultTile();
    }
}
