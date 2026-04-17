package it.polimi.ingsw.am25.server.webLayer;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Observers.BoardObserver;
import it.polimi.ingsw.am25.server.model.Observers.GameObserver;
import it.polimi.ingsw.am25.server.model.Observers.MarketObserver;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Player.Totem;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BoardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.GameDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.MarketDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.util.List;
import java.util.stream.Collectors;

public class VirtualView implements BoardObserver, GameObserver, MarketObserver, PlayerObserver {
    private GameDTO gameSnapShot;
    private List<PlayerDTO> playerDTOSnapshot;
    private BoardDTO boardSnapShot;

    public VirtualView() {
    }
    @Override
    public void onBoardChanged(List<OfferTile> offerTileList, List<DefaultTile> defaultTileList) {

    }

    @Override
    public void gameWinners(List<Player> winners) {
        gameSnapShot.setWinners(
                winners.stream().map(PlayerDTO::new).toList());

        //TODO: serializzazione e collegare parte notifica soket
    }

    @Override
    public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase, Player playerToPlace, Player playerToPlay) {

    }

    @Override
    public void playerToDefaultTile(List<Player> playerOrder) {

    }

    @Override
    public void playerPlacedOnOffertile(Player player, int tilePosition) {

    }

    @Override
    public void onPlayerAdded(Player playerAdded) {

    }

    @Override
    public void onEraChanged(ERA currentEra) {

    }

    @Override
    public void onGamePhaseChanged(GAME_PHASE gamePhase) {

    }

    @Override
    public void onPlayerToPlaceChanged(Player newPlayerToPlace) {

    }

    @Override
    public void onPlayerToPlayChanged(Player newPlayerToPlay) {

    }

    @Override
    public void onTopCardRefreshed(List<Card> topCards) {

    }

    @Override
    public void onTopBuildingRefreshed(List<Card> topCards) {

    }

    @Override
    public void onCardRemovedFromTop(int position, CARD_TYPE cardType) {

    }

    @Override
    public void onCardRemovedFromBottom(int position, CARD_TYPE cardType) {

    }

    @Override
    public void notifyFoodChanged(int newFood) {

    }

    @Override
    public void notifyPPChanged(int newPP) {

    }

    @Override
    public void onMarketChanged(List<Card> topCards, List<Card> bottomCards, List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {
    }

    @Override
    public void onPlayerChanged(String nickname, Totem totem, int food, int prestigePoint, List<Card> tribe, List<BuildingCard> buildingCards) {

    }
}
