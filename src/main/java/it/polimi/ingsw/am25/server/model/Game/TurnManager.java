package it.polimi.ingsw.am25.server.model.Game;

import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Enums.CONNECTION_STATUS;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.EndOfPlacingPhaseException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.EndOfPlayingPhaseException;

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
        this.playingOrder = new ArrayList<>();
        this.boardView = boardView;

    }

    /**
     * Returns the next playing player and removes them from the playing-order queue,
     * automatically skipping any DISCONNECTED players until a connected one is found.
     * When the queue is empty (or contains only disconnected players) all players have
     * resolved their actions for this round.
     *
     * @return the next connected player to resolve their actions
     * @throws EndOfPlayingPhaseException if there are no more connected players in the queue
     */
    public Player getNextPlayingPlayer() throws EndOfPlayingPhaseException {
        while (!playingOrder.isEmpty()) {
            Player playerToRet = playingOrder.remove(0);
            if (playerToRet.getConnection() != CONNECTION_STATUS.DISCONNECTED) {
                return playerToRet;
            }
        }
        throw new EndOfPlayingPhaseException("Tutti i giocatori hanno risolto le loro azioni");
    }

    /**
     * Returns the next placing player and removes them from the placing-order queue,
     * automatically skipping any DISCONNECTED players until a connected one is found.
     * When the queue is empty (or contains only disconnected players) all players have
     * placed their totems for this round.
     *
     * @return the next connected player to place their totem
     * @throws EndOfPlacingPhaseException if there are no more connected players in the queue
     */
    public Player getNextPlacingPlayer() throws EndOfPlacingPhaseException {
        while (!placingOrder.isEmpty()) {
            Player playerToRet = placingOrder.remove(0);
            if (playerToRet.getConnection() != CONNECTION_STATUS.DISCONNECTED) {
                return playerToRet;
            }
        }
        throw new EndOfPlacingPhaseException("Tutti i giocatori sono stati posizionati");
    }

    /**
     * Removes a disconnected player from both the placing and playing queues immediately.
     * This prevents the game from waiting for a player who will never act.
     *
     * @param player the player to remove.
     */
    public void removePlayer(Player player) {
        placingOrder.removeIf(p -> p.getNickname().equals(player.getNickname()));
        playingOrder.removeIf(p -> p.getNickname().equals(player.getNickname()));
    }

    /**
     * Re-adds a reconnected player to the end of both turn queues,
     * provided they are not already present.
     *
     * @param player the reconnected player.
     */
    public void reAddPlayer(Player player) {
        boolean inPlacing = placingOrder.stream().anyMatch(p -> p.getNickname().equals(player.getNickname()));
        boolean inPlaying = playingOrder.stream().anyMatch(p -> p.getNickname().equals(player.getNickname()));
        boolean alreadyPlaced = boardView.getOrderedPlayerOnOfferTile().stream()
                .anyMatch(p -> p.getNickname().equals(player.getNickname()));
        if (!inPlacing && !alreadyPlaced) placingOrder.add(player);
        if (!inPlaying) playingOrder.add(player);
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

    public void updatePlayingOrder() {
        this.playingOrder = boardView.getOrderedPlayerOnOfferTile();
    }

    /**
     * Refreshes the placing order from the board by reading the current default-tile positions.
     * Must be called at the start of every placing phase.
     */

    public void updatePlacingOrder() {
        this.placingOrder = boardView.getOrderedPlayerOnDefaultTile();
    }
}
