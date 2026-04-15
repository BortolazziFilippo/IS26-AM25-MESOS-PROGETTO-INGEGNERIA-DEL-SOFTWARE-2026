package it.polimi.ingsw.am25.server.webLayer;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Observers.BoardObserver;
import it.polimi.ingsw.am25.server.model.Observers.GameObserver;
import it.polimi.ingsw.am25.server.model.Observers.MarketObserver;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Player.Totem;

import java.util.List;

public class VirtualView implements BoardObserver, GameObserver, MarketObserver, PlayerObserver {

    public VirtualView() {

    }
    @Override
    public void onBoardChanged(List<OfferTile> offerTileList, List<DefaultTile> defaultTileList) {

    }

    @Override
    public void gameWinners(List<Player> winners) {

    }

    @Override
    public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase, Player playerToPlace, Player playerToPlay, OfferTile offerTilePlayerIsOn) {

    }

    @Override
    public void onMarketChanged(List<Card> topCards, List<Card> bottomCards, List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {

    }

    @Override
    public void onPlayerChanged(String nickname, Totem totem, int food, int prestigePoint, List<Card> tribe, List<BuildingCard> buildingCards) {

    }
}
