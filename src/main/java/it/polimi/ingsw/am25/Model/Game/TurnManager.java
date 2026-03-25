package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.TurnOrderView;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    private List<Player> placingOrder;
    private List<Player> playingOrder;
    private int currentPlayingPlayer;
    private int currentPlacingPlayer;
    private final TurnOrderView turnOrderView;

    public TurnManager(TurnOrderView turnOrderView) {
        this.placingOrder = new ArrayList<>();
        this.playingOrder= new ArrayList<>();
        this.turnOrderView=turnOrderView;
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
        this.playingOrder= turnOrderView.getOrderedPlayerOnOfferTile();
    }
    public void updatePlacingOrder(){
        this.placingOrder =turnOrderView.getOrderedPlayerOnDefaultTile();
    }
}
