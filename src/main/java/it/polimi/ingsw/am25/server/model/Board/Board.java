package it.polimi.ingsw.am25.server.model.Board;

import it.polimi.ingsw.am25.server.model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.server.model.Factory.OfferTile.OfferTileFactory;
import it.polimi.ingsw.am25.server.model.Game.GameView;
import it.polimi.ingsw.am25.server.model.Observers.BoardObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.TileOccupiedException;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The Mesos game board, containing the offer tiles (where players place their totems
 * during the placing phase) and the default tiles (where players return at the end
 * of each round to collect food).
 */
public class Board implements BoardView {
    private static final String LOG_PREFIX = "[SERVER][BOARD]";
    private final List<OfferTile> offerTiles;
    private final List<DefaultTile> defaultTiles;
    private final GameView gameView;
    private final  List<BoardObserver> observers=new ArrayList<>();

    /**
     * Creates a new board instance.
     * @param gameView parameter gameView.
     */
    public Board(GameView gameView) {
        this.gameView = gameView;
        defaultTiles=new DefaultTileFactory().buildDefaultTiles(gameView.getPlayerNumber());
        offerTiles= new OfferTileFactory().offertileBuilder(gameView.getPlayerNumber());
    }


    /**
     * Moves all players from the offer tiles back to the default tiles and awards food
     * according to the food-per-slot value of their assigned default tile.
     */
    public void returnOnDefaultTiles(){
        int counter=-1;
        List<Player> pl= new ArrayList<>(this.offerTiles.stream().filter(Tile::isOccupied).map(Tile::getPlayerOn).toList());
        for(DefaultTile defaultTile:defaultTiles){
            counter++;
            defaultTile.placePlayer(pl.get(0));
            defaultTile.getPlayerOn().manageFoodAndPP(defaultTile.getFoodPerSlotPosition());
            logServerEvent(
                    "Moved player '" + defaultTile.getPlayerOn().getNickname() + "' to default "+ counter +" tile and applied food delta " +
                            defaultTile.getFoodPerSlotPosition()
            );
            this.offerTiles.stream().filter(offerTile -> Objects.equals(pl.get(0),offerTile.getPlayerOn())).forEach(Tile::removePlayer);
            pl.remove(0);
        }
        notifyPlayerToDefaultTile();
    }

    /**
     * Places the given player on the default tile at the specified position.
     *
     * @param player        player to be placed
     * @param tilePosition  index of the target default tile
     * @throws IndexOutOfBoundsException if {@code tilePosition} is outside the valid range
     * @throws TileOccupiedException     if the target tile is already occupied
     */
    public void placePlayerOnDefaultTile(Player player,int tilePosition) throws IndexOutOfBoundsException, TileOccupiedException{

        if(tilePosition<defaultTiles.size() && tilePosition>=0){
            if(defaultTiles.get(tilePosition).isOccupied()){
                throw new TileOccupiedException(getClass() +" tile is already occupied");
            }
            defaultTiles.get(tilePosition).placePlayer(player);
            logServerEvent("Placed player '" + player.getNickname() + "' on default tile position " + tilePosition);
            notifyPlayerToDefaultTile();
        }else{
            throw new IndexOutOfBoundsException(getClass()+" TilePosition not valid");
        }
    }

    /**
     * Places a player on the selected offer tile and removes them from default tiles.
     *
     * @param player player to move
     * @param tilePosition target offer-tile index
     * @throws TileOccupiedException if the target tile is already occupied
     * @throws IndexOutOfBoundsException if {@code tilePosition} is outside valid bounds
     */
    public void placePlayerOnOffertile(Player player,int tilePosition) throws IndexOutOfBoundsException,TileOccupiedException {
        if(tilePosition>=0 && tilePosition<offerTiles.size()){
            if(!offerTiles.get(tilePosition).isOccupied()){
                offerTiles.get(tilePosition).placePlayer(player);
                defaultTiles.stream().filter(defaultTile -> Objects.equals(player, defaultTile.getPlayerOn())).forEach(Tile::removePlayer);
                logServerEvent("Placed player '" + player.getNickname() + "' on offer tile position " + tilePosition);
                notifyPlayerToOfferTile(player,tilePosition);
            }else{
                throw new TileOccupiedException(getClass() + "Selected tile is occupied");
            }
        }else{
            throw new IndexOutOfBoundsException(getClass() +" TilePosition "+ tilePosition +" Not Valid");
        }

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
    public void addObserver(BoardObserver observerToAdd){
        if(observerToAdd!=null && !observers.contains(observerToAdd)){
            observers.add(observerToAdd);
        }
    }

    /**
     * Unsubscribes an observer.
     *
     * @param observerToRemove observer to remove
     */
    public void removeObserver(BoardObserver observerToRemove){
        observers.remove(observerToRemove);
    }
    private void  notify(Consumer<BoardObserver> action){
        for(BoardObserver boardObserver:observers){
            action.accept(boardObserver);
        }
    }
    /**
     * Notifies subscribers with a snapshot of the current board.
     */
    public void notifyBoardChanged(){
        List<OfferTile> offertileSnapshot = List.copyOf(offerTiles);
        List<DefaultTile> defaultTileSnapshot = List.copyOf(defaultTiles);
        notify(boardObserver -> boardObserver.onBoardChanged(offertileSnapshot,defaultTileSnapshot));
    }

    /**
     * Notifies observers with the updated default-tile player order.
     */
    private void notifyPlayerToDefaultTile(){
        List<Player> players=getOrderedPlayerOnDefaultTile();
        notify(observer -> observer.playerToDefaultTile(players));
    }

    /**
     * Notifies observers that a player has been placed on an offer tile.
     *
     * @param player moved player
     * @param tilePosition target tile index
     */
    private void notifyPlayerToOfferTile(Player player,int tilePosition){
        notify(observer -> observer.playerPlacedOnOffertile(player,tilePosition));
    }



    /**
     * Returns ordered player on offer tile.
     * @return the result of the operation.
     */
    @Override
    public List<Player> getOrderedPlayerOnOfferTile() {
        return new ArrayList<>(offerTiles.stream().filter(OfferTile::isOccupied).map(Tile::getPlayerOn).toList());
    }


    /**
     * Returns the players currently on default tiles, in tile order.
     *
     * @return ordered list of players on default tiles
     */
    @Override
    public List<Player> getOrderedPlayerOnDefaultTile() {
        return new ArrayList<>(defaultTiles.stream().filter(Tile::isOccupied).map(Tile::getPlayerOn).toList());

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
        OfferTile offerTileToReturn= offerTiles.stream()
                .filter(OfferTile::isOccupied)
                .filter(offerTile -> offerTile.getPlayerOn().equals(player))
                .findFirst()
                .orElseThrow(()->new IllegalStateException("Nessun player trovato"));
        return new OfferTile(offerTileToReturn);
    }

    /**
     * Executes log server event.
     * @param message parameter message.
     */
    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }
}
