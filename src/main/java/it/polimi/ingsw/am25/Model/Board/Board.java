package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.Model.Factory.OfferTile.OfferTileFactory;
import it.polimi.ingsw.am25.Model.Game.GameView;
import it.polimi.ingsw.am25.Model.Observers.BoardObserver;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.TileOccupiedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * this method move all player from the offerTiles to the Default tiles and add the right amount of food to the player
     *
     */
    public void returnOnDefaultTiles(){
        List<Player> pl= new ArrayList<>(this.offerTiles.stream().filter(Tile::isOccupied).map(Tile::getPlayerOn).toList());
        for(DefaultTile defaultTile:defaultTiles){
            defaultTile.placePlayer(pl.getFirst());
            defaultTile.getPlayerOn().manageFoodAndPP(defaultTile.getFoodPerSlotPosition());
            this.offerTiles.stream().filter(offerTile -> Objects.equals(pl.getFirst(),offerTile.getPlayerOn())).forEach(Tile::removePlayer);
            pl.removeFirst();
        }
        notifyBoardChanged(); //here it notifies the changes
    }

    /**
     * this method places the given player ont the tile positino
     * @param player player To be Placed
     * @param tilePosition position to be placed
     * @throws IndexOutOfBoundsException in the case the given position is not in the right boundaries
     * @throws TileOccupiedException in the case the given position is already occupied
     */
    public void placePlayerOnDefaultTile(Player player,int tilePosition) throws IndexOutOfBoundsException, TileOccupiedException{

        if(tilePosition<defaultTiles.size() && tilePosition>=0){
            if(defaultTiles.get(tilePosition).isOccupied()){
                throw new TileOccupiedException(getClass() +" tile is already occupied");
            }
            defaultTiles.get(tilePosition).placePlayer(player);
            //here i don't need to notify the changes since this method is only called during the game creation
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
                notifyBoardChanged(); //after the player is placed it notifies the
            }else{
                throw new TileOccupiedException(getClass() + "Selected tile is occupied");
            }
        }else{
            throw new IndexOutOfBoundsException(getClass() +" TilePosition "+ tilePosition +" Not Valid");
        }

    }

    public List<DefaultTile> getDefaultTiles() {
        return defaultTiles;
    }

    public List<OfferTile> getOfferTiles() {
        return offerTiles;
    }

    /**
     * this method subscribe an observer
     * @param observerToAdd observer da aggiungere
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

    @Override
    public List<Player> getOrderedPlayerOnOfferTile() {
        return new ArrayList<>(offerTiles.stream().filter(OfferTile::isOccupied).map(Tile::getPlayerOn).toList());
    }

    @Override
    public List<Player> getOrderedPlayerOnDefaultTile() {
        return new ArrayList<>(defaultTiles.stream().filter(Tile::isOccupied).map(Tile::getPlayerOn).toList());

    }

    @Override
    public boolean isPlayerOnAnEligibleDefaultTile(Player player) {
        return this.defaultTiles.stream()
                .filter(DefaultTile::isOccupied)
                .filter(defaultTile -> defaultTile.getPlayerOn().equals(player))
                .findFirst()                                      // 1. Take the first (and unique) match
                .map(tile -> tile.getFoodPerSlotPosition() >= 0)  // 2. check the value
                .orElse(false);
    }

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
