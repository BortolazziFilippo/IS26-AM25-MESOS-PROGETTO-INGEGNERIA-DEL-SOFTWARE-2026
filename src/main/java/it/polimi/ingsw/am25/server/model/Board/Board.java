package it.polimi.ingsw.am25.server.model.Board;

import it.polimi.ingsw.am25.server.model.Enums.CONNECTION_STATUS;
import it.polimi.ingsw.am25.server.model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.server.model.Factory.OfferTile.OfferTileFactory;
import it.polimi.ingsw.am25.server.model.Game.GameView;
import it.polimi.ingsw.am25.server.model.Observers.BoardObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.TileOccupiedException;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.model.persistance.BoardMemento;
import it.polimi.ingsw.am25.server.model.persistance.MementoManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The Mesos game board, containing the offer tiles (where players place their totems
 * during the placing phase) and the default tiles (where players return at the end
 * of each round to collect food).
 */
public class Board implements BoardView, MementoManager<BoardMemento> {
    private static final String LOG_PREFIX = "[SERVER][BOARD]";
    private final List<OfferTile> offerTiles;
    private final List<DefaultTile> defaultTiles;
    private final List<BoardObserver> observers = new ArrayList<>();

    /**
     * Creates a new board instance.
     *
     * @param gameView parameter gameView.
     */
    public Board(GameView gameView) {
        defaultTiles = new DefaultTileFactory().buildDefaultTiles(gameView.getPlayerNumber());
        offerTiles = new OfferTileFactory().offertileBuilder(gameView.getPlayerNumber());
    }


    /**
     * Moves all players from the offer tiles back to the default tiles and awards food
     * according to the food-per-slot value of their assigned default tile.
     */
    public void returnOnDefaultTiles() {
        List<Player> offerPlayers          = collectOfferTilePlayers();
        List<Player> disconnectedOnDefault = liftDisconnectedFromDefaultTiles();
        int front = fillDefaultTilesFromFront(offerPlayers);
        packDisconnectedAtBack(disconnectedOnDefault, front);
        notifyPlayerToDefaultTile();
    }

    /**
     * Collects and returns the players currently on offer tiles (those who placed this round).
     *
     * @return list of players on occupied offer tiles, in tile order.
     */
    private List<Player> collectOfferTilePlayers() {
        return new ArrayList<>(offerTiles.stream()
                .filter(Tile::isOccupied)
                .map(Tile::getPlayerOn)
                .toList());
    }

    /**
     * Removes all disconnected players from default tiles and returns them.
     * They will be re-inserted at the back of the default tile order after
     * the connected players have been placed.
     *
     * @return list of players that were lifted out.
     */
    private List<Player> liftDisconnectedFromDefaultTiles() {
        List<Player> disconnected = new ArrayList<>(defaultTiles.stream()
                .filter(t -> t.isOccupied() && t.getPlayerOn().getConnection() == CONNECTION_STATUS.DISCONNECTED)
                .map(Tile::getPlayerOn)
                .toList());
        disconnected.forEach(p -> defaultTiles.stream()
                .filter(t -> t.isOccupied() && t.getPlayerOn().equals(p))
                .forEach(Tile::removePlayer));
        return disconnected;
    }

    /**
     * Places offer-tile players onto default tiles from the front, awarding food for each slot.
     * Skips tiles that are already occupied (e.g. a player who reconnected mid-round).
     *
     * @param offerPlayers players to place, in priority order.
     * @return the index of the first free slot after the last placed player.
     */
    private int fillDefaultTilesFromFront(List<Player> offerPlayers) {
        int front = 0;
        for (Player p : offerPlayers) {
            while (front < defaultTiles.size() && defaultTiles.get(front).isOccupied()) front++;
            if (front >= defaultTiles.size()) break;
            defaultTiles.get(front).placePlayer(p);
            p.manageFoodAndPP(defaultTiles.get(front).getFoodPerSlotPosition());
            logServerEvent("Moved player '" + p.getNickname() + "' to default tile " + front
                    + " and applied food delta " + defaultTiles.get(front).getFoodPerSlotPosition());
            offerTiles.stream()
                    .filter(t -> t.isOccupied() && t.getPlayerOn().equals(p))
                    .forEach(Tile::removePlayer);
            front++;
        }
        return front;
    }

    /**
     * Places disconnected players at the back of the default tiles (no food awarded —
     * they did not participate this round).
     *
     * @param disconnected players to pack at the back.
     * @param front        index of the first slot already occupied by connected players;
     *                     disconnected players are placed only in slots at or beyond this index.
     */
    private void packDisconnectedAtBack(List<Player> disconnected, int front) {
        int back = defaultTiles.size() - 1;
        for (Player p : disconnected) {
            while (back >= front && defaultTiles.get(back).isOccupied()) back--;
            if (back >= front) {
                defaultTiles.get(back).placePlayer(p);
                logServerEvent("Placed disconnected player '" + p.getNickname() + "' on last default tile " + back);
                back--;
            }
        }
    }

    /**
     * Validates that {@code tilePosition} is a legal index into {@code tiles} and that
     * the tile at that position is not already occupied.
     *
     * @param tiles        the tile list to check against
     * @param tilePosition the requested index
     * @throws IndexOutOfBoundsException if {@code tilePosition} is outside {@code [0, tiles.size())}
     * @throws TileOccupiedException     if the tile at {@code tilePosition} is already occupied
     */
    private void validateTileAccess(List<? extends Tile> tiles, int tilePosition)
            throws IndexOutOfBoundsException, TileOccupiedException {
        if (tilePosition < 0 || tilePosition >= tiles.size()) {
            throw new IndexOutOfBoundsException(getClass() + " TilePosition " + tilePosition + " not valid");
        }
        if (tiles.get(tilePosition).isOccupied()) {
            throw new TileOccupiedException(getClass() + " tile is already occupied");
        }
    }

    /**
     * Places the given player on the default tile at the specified position.
     *
     * @param player       player to be placed
     * @param tilePosition index of the target default tile
     * @throws IndexOutOfBoundsException if {@code tilePosition} is outside the valid range
     * @throws TileOccupiedException     if the target tile is already occupied
     */
    public void placePlayerOnDefaultTile(Player player, int tilePosition) throws IndexOutOfBoundsException, TileOccupiedException {
        validateTileAccess(defaultTiles, tilePosition);
        defaultTiles.get(tilePosition).placePlayer(player);
        logServerEvent("Placed player '" + player.getNickname() + "' on default tile position " + tilePosition);
        notifyPlayerToDefaultTile();
    }

    /**
     * Places a player on the selected offer tile and removes them from default tiles.
     *
     * @param player       player to move
     * @param tilePosition target offer-tile index
     * @throws TileOccupiedException     if the target tile is already occupied
     * @throws IndexOutOfBoundsException if {@code tilePosition} is outside valid bounds
     */
    public void placePlayerOnOffertile(Player player, int tilePosition) throws IndexOutOfBoundsException, TileOccupiedException {
        validateTileAccess(offerTiles, tilePosition);
        offerTiles.get(tilePosition).placePlayer(player);
        defaultTiles.stream()
                .filter(t -> Objects.equals(player, t.getPlayerOn()))
                .forEach(Tile::removePlayer);
        logServerEvent("Placed player '" + player.getNickname() + "' on offer tile position " + tilePosition);
        notifyPlayerToOfferTile(player, tilePosition);
    }

    /**
     * Returns the list of default tiles on the board.
     *
     * @return list of {@link DefaultTile}
     */
    public List<DefaultTile> getDefaultTiles() {
        return defaultTiles;
    }

    /**
     * Returns the list of offer tiles on the board.
     *
     * @return list of {@link OfferTile}
     */
    public List<OfferTile> getOfferTiles() {
        return offerTiles;
    }

    /**
     * Subscribes an observer to board-state changes.
     *
     * @param observerToAdd observer to add; ignored if null or already subscribed
     */
    public void addObserver(BoardObserver observerToAdd) {
        if (observerToAdd != null && !observers.contains(observerToAdd)) {
            observers.add(observerToAdd);
        }
    }

    /**
     * Unsubscribes an observer.
     *
     * @param observerToRemove observer to remove
     */
    public void removeObserver(BoardObserver observerToRemove) {
        observers.remove(observerToRemove);
    }

    private void notify(Consumer<BoardObserver> action) {
        for (BoardObserver boardObserver : observers) {
            action.accept(boardObserver);
        }
    }

    /**
     * Notifies subscribers with a snapshot of the current board.
     */
    public void notifyBoardChanged() {
        List<OfferTile> offertileSnapshot = List.copyOf(offerTiles);
        List<DefaultTile> defaultTileSnapshot = List.copyOf(defaultTiles);
        notify(boardObserver -> boardObserver.onBoardChanged(offertileSnapshot, defaultTileSnapshot));
        notifyPlayerToDefaultTile();
    }

    /**
     * Notifies observers with the updated default-tile player order.
     */
    private void notifyPlayerToDefaultTile() {
        List<Player> players = getOrderedPlayerOnDefaultTile();
        notify(observer -> observer.playerToDefaultTile(players));
    }

    /**
     * Notifies observers that a player has been placed on an offer tile.
     *
     * @param player       moved player
     * @param tilePosition target tile index
     */
    private void notifyPlayerToOfferTile(Player player, int tilePosition) {
        notify(observer -> observer.playerPlacedOnOffertile(player, tilePosition));
    }


    /**
     * Returns the players currently occupying the given tile list, in tile order.
     *
     * @param tiles the tile list to scan
     * @return mutable list of players on occupied tiles
     */
    private List<Player> getOccupants(List<? extends Tile> tiles) {
        return new ArrayList<>(tiles.stream()
                .filter(Tile::isOccupied)
                .map(Tile::getPlayerOn)
                .toList());
    }

    /**
     * Returns the players currently on offer tiles, in tile order.
     *
     * @return ordered list of players on offer tiles.
     */
    @Override
    public List<Player> getOrderedPlayerOnOfferTile() {
        return getOccupants(offerTiles);
    }

    /**
     * Returns the players currently on default tiles, in tile order.
     *
     * @return ordered list of players on default tiles.
     */
    @Override
    public List<Player> getOrderedPlayerOnDefaultTile() {
        return getOccupants(defaultTiles);
    }

    /**
     * Returns {@code true} if the given player is on a default tile whose food reward is >= 0.
     * (Players on a tile with a negative food value do not receive the bonus.)
     *
     * @param player the player to check
     * @return whether the player is on an eligible default tile
     */
    @Override
    public boolean isPlayerOnAnEligibleDefaultTile(Player player) {
        return this.defaultTiles.stream()
                .filter(DefaultTile::isOccupied)
                .filter(defaultTile -> defaultTile.getPlayerOn().equals(player))
                .findFirst()
                .map(tile -> tile.getFoodPerSlotPosition() >= 0)
                .orElse(false);
    }

    /**
     * Returns a defensive copy of the offer tile the given player is currently on.
     *
     * @param player the player whose tile should be returned
     * @return a copy of the {@link OfferTile} the player occupies
     * @throws IllegalStateException if the player is not found on any offer tile
     */
    @Override
    public OfferTile getCopyTilePlayerIsOn(Player player) {
        OfferTile offerTileToReturn = offerTiles.stream()
                .filter(OfferTile::isOccupied)
                .filter(offerTile -> offerTile.getPlayerOn().equals(player))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nessun player trovato"));
        return new OfferTile(offerTileToReturn);
    }

    /**
     * Executes log server event.
     *
     * @param message parameter message.
     */
    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }

    /**
     * Creates a memento capturing the current order of players on default tiles.
     *
     * @return a {@link BoardMemento} containing the ordered list of nicknames of players on default tiles.
     */
    @Override
    public BoardMemento createMemento() {
        logServerEvent("Creating board memento (" + getOrderedPlayerOnDefaultTile().size() + " players on default tiles)");
        return new BoardMemento(
                this.getOrderedPlayerOnDefaultTile().stream().map(Player::getNickname).toList()
        );
    }

    /**
     * Restores the board state from the given memento, removing all players
     * from default tiles and offer tiles in preparation for repositioning.
     *
     * @param memento the {@link BoardMemento} from which to restore the board state.
     */
    @Override
    public void restoreMemento(BoardMemento memento) {
        logServerEvent("Restoring board memento (clearing all tiles)");
        defaultTiles.stream().filter(Tile::isOccupied).forEach(Tile::removePlayer);
        offerTiles.stream().filter(Tile::isOccupied).forEach(Tile::removePlayer);
    }
}
