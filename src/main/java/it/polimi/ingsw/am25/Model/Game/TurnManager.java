package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.BoardView;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EndOfPlacingPhaseException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.EndOfPlayingPhaseException;

import java.util.ArrayList;
import java.util.List;
/**
 * Manages the turn order for both the placing and playing phases.
 * The placing order is determined by the positions on the default tiles;
 * the playing order by the positions on the offer tiles.
 */
public class TurnManager {
    private List<Player> placingOrder;
    private List<Player> playingOrder;
    private Player currentPlayingPlayer;
    private Player currentPlacingPlayer;
    private final BoardView boardView;
    /**
     * Creates a TurnManager backed by the given board view.
     *
     * @param boardView the board view used to determine player positions
     */
    public TurnManager(BoardView boardView) {
        this.placingOrder = new ArrayList<>();
        this.playingOrder= new ArrayList<>();
        this.boardView = boardView;

    }

    /**
     * Returns the next playing player and removes them from the playing-order queue.
     * When the queue is empty all players have resolved their actions for this round.
     *
     * @return the next player to resolve their actions
     * @throws EndOfPlayingPhaseException if there are no more players in the playing-order queue
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
     * Returns the next placing player and removes them from the placing-order queue.
     * When the queue is empty all players have placed their totems for this round.
     *
     * @return the next player to place their totem
     * @throws EndOfPlacingPhaseException if there are no more players in the placing-order queue
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
    


    /**
     * Returns the current (unmodified) placing-order queue.
     *
     * @return list of players yet to place, in order
     */
    public List<Player> getPlacingOrder() {
        return placingOrder;
    }
    /**
     * Returns the current (unmodified) playing-order queue.
     *
     * @return list of players yet to play, in order
     */
    public List<Player> getPlayingOrder() {
        return playingOrder;
    }
    /**
     * Refreshes the playing order from the board by reading the current offer-tile positions.
     * Must be called at the start of every playing phase.
     */

    public void updatePlayingOrder(){
        this.playingOrder= boardView.getOrderedPlayerOnOfferTile();
    }
    /**
     * Refreshes the placing order from the board by reading the current default-tile positions.
     * Must be called at the start of every placing phase.
     */

    public void updatePlacingOrder(){
        this.placingOrder = boardView.getOrderedPlayerOnDefaultTile();
    }
}
