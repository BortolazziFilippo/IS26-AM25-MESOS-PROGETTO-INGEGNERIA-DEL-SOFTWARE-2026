package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.Model.Factory.OfferTile.OfferTileFactory;
import it.polimi.ingsw.am25.Model.Game.GameView;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class Board implements BoardView {
    private final List<OfferTile> offerTiles;
    private final List<DefaultTile> defaultTiles;
    private final GameView gameView;

    public Board(GameView gameView) {
        this.gameView = gameView;
        defaultTiles=new DefaultTileFactory().buildDefaultTiles(gameView.getPlayerNumber());
        offerTiles= new OfferTileFactory().offertileBuilder(gameView.getPlayerNumber());
    }

    @Override
    public List<Player> getOrderedPlayerOnOfferTile() {
        return new ArrayList<>(offerTiles.stream().filter(OfferTile::isOccupied).map(Tile::getPlayerOn).toList());
    }

    @Override
    public List<Player> getOrderedPlayerOnDefaultTile() {
        return new ArrayList<>(defaultTiles.stream().filter(Tile::isOccupied).map(Tile::getPlayerOn).toList());

    }

}
