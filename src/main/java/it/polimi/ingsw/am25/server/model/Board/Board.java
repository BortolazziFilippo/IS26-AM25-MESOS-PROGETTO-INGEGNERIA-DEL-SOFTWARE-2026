package it.polimi.ingsw.am25.server.model.Board;

import it.polimi.ingsw.am25.server.model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.server.model.Factory.OfferTile.OfferTileFactory;
import it.polimi.ingsw.am25.server.model.Game.GameView;
import it.polimi.ingsw.am25.server.model.Observers.BoardObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.TileOccupiedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Board implements BoardView {
    private final List<OfferTile> offerTiles;
    private final List<DefaultTile> defaultTiles;
    private  GameView gameView;
    private final  List<BoardObserver> observers=new ArrayList<>();

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
        List<Player> pl= new ArrayList<>(this.offerTiles.stream().filter(Tile::isOccupied).map(Tile::getPlayerOn).toList());
        for(DefaultTile defaultTile:defaultTiles){
            defaultTile.placePlayer(pl.getFirst());
            defaultTile.getPlayerOn().manageFoodAndPP(defaultTile.getFoodPerSlotPosition());
            this.offerTiles.stream().filter(offerTile -> Objects.equals(pl.getFirst(),offerTile.getPlayerOn())).forEach(Tile::removePlayer);
            pl.removeFirst();
        }
        notifyPlayerToDefaultTile(); //notifying the vw the player went back to the default tile and updating order
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
            notifyPlayerToDefaultTile(); //notifying the VW of the starting order on the default tile
        }else{
            throw new IndexOutOfBoundsException(getClass()+" TilePosition not valid");
        }
    }

    /**
     * this method place the player on the selected OfferTile and removes it from the defaultTile
     * in case the selected tile is occupied it throws an Exception
     * @param player player to be moved
     * @param tilePosition index of the target tile
     * @throws TileOccupiedException in the case the tile is occupied it throws TileOccupiedException
     * @throws IndexOutOfBoundsException in case tilePosition is not valid
     */
    public void placePlayerOnOffertile(Player player,int tilePosition) throws IndexOutOfBoundsException,TileOccupiedException {
        if(tilePosition>=0 && tilePosition<offerTiles.size()){
            if(!offerTiles.get(tilePosition).isOccupied()){
                offerTiles.get(tilePosition).placePlayer(player);
                defaultTiles.stream().filter(defaultTile -> Objects.equals(player, defaultTile.getPlayerOn())).forEach(Tile::removePlayer);
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
     * this method unsubscribe an observer
     * @param observerToRemove observer to remove
     */
    public void removeObserver(BoardObserver observerToRemove){
        observers.remove(observerToRemove);
    }

    /**
     * This method notifies the subscriber of the changes
     */
    public void notifyBoardChanged(){
        List<OfferTile> offertileSnapshot = List.copyOf(offerTiles);
        List<DefaultTile> defaultTileSnapshot = List.copyOf(defaultTiles);

        for(BoardObserver boardObserver:observers){
            boardObserver.onBoardChanged(offertileSnapshot,defaultTileSnapshot);
        }
    }

    /**
     * this method notifies the order of the player once the go back to the default tile
     */
    private void notifyPlayerToDefaultTile(){
        List<Player> players=getOrderedPlayerOnDefaultTile();
        for(BoardObserver observer:observers){
            observer.playerToDefaultTile(players);
        }
    }

    /**
     * this method notifies that the player has been placed on the offertile on tilepositiom
     * @param player player moved
     * @param tilePosition index
     */
    private void notifyPlayerToOfferTile(Player player,int tilePosition){
        for(BoardObserver observer:observers){
            observer.playerPlacedOnOffertile(player,tilePosition);
        }
    }


    /**
     * Returns the players currently on offer tiles, in tile order.
     *
     * @return ordered list of players on offer tiles
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
                .findFirst()                                      // 1. Take the first (and unique) match
                .map(tile -> tile.getFoodPerSlotPosition() >= 0)  // 2. check the value
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
}
