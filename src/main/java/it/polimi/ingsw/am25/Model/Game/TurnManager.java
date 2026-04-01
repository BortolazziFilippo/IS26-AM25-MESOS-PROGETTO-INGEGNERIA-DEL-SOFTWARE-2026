package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.BoardView;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EndOfPlacingPhaseException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EndOfPlayingPhaseException;

import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    private List<Player> placingOrder;
    private List<Player> playingOrder;
    private Player currentPlayingPlayer;
    private Player currentPlacingPlayer;
    private final BoardView boardView;

    public TurnManager(BoardView boardView) {
        this.placingOrder = new ArrayList<>();
        this.playingOrder= new ArrayList<>();
        this.boardView = boardView;

    }

    /**
     * this method return the current playing player and removes it from the placingOrderedList
     * in the case the list is empty it means all the players are placed and throws EndOfPlayingPhaseException
     * @return the current playing player
     * @throws EndOfPlayingPhaseException in case all the players have played
     */
    public Player getNextPlayingPlayer() throws EndOfPlayingPhaseException {
        if(!playingOrder.isEmpty()){
            Player playerToRet=playingOrder.getFirst();
            playingOrder.removeFirst();
            return playerToRet;
        }else{
            throw new EndOfPlayingPhaseException("Tutti i giocatori hanno risolto le loro azioni");
        }
    }

    /**
     * this method return the current placing player and removes it from the placingOrderList
     * int the case the list is empty it means all the player are placed and throws an EndOfPacingPhaseException
     * @return the player next
     * @throws EndOfPlacingPhaseException in the case thera are no more player to place
     */
    public Player getNextPlacingPlayer() throws EndOfPlacingPhaseException{
        if(!placingOrder.isEmpty()){
            Player playerToRet=placingOrder.getFirst();
            placingOrder.removeFirst();
            return playerToRet;
        }else{
            throw new EndOfPlacingPhaseException("Tutti i giocatori sono stati posizionati");
        }

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
