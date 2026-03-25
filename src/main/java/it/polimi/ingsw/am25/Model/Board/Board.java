package it.polimi.ingsw.am25.Model.Board;

import it.polimi.ingsw.am25.Model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.Model.Game.GameView;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.List;

public class Board implements TurnOrderView{
    private List<OfferTile> offerTiles;
    private List<DefaultTile> defaultTiles;
    private final GameView gameView;

    public Board(GameView gameView) {
        this.gameView = gameView;
        defaultTiles=new DefaultTileFactory().buildDefaultTiles(gameView.getPlayerNumber());
    }

    @Override
    public List<Player> getOrderedPlayerOnOfferTile() {
        return List.of();
    }

    @Override
    public List<Player> getOrderedPlayerOnDefaultTile() {
        return List.of();
    }
}
