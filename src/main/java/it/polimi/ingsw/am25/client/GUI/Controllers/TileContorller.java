package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.GUI.GUIObserver;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.List;

public class TileContorller implements GUIObserver {
    private ClientVirtualView clienHandler;
    @FXML
    private HBox tileRail;
    public TileContorller() {

    }
    @Override
    public void onGamePhaseChanged(GAME_PHASE phase) {
        GUIObserver.super.onGamePhaseChanged(phase);
    }

    @Override
    public void onPlayerAdded(PlayerDTO player) {
        GUIObserver.super.onPlayerAdded(player);
    }

    @Override
    public void onError(String message) {
        GUIObserver.super.onError(message);
    }

    @Override
    public void onPlayerToPlaceChanged(String nickname) {
        GUIObserver.super.onPlayerToPlaceChanged(nickname);
    }

    @Override
    public void onPlayerToPlayChanged(String nickname) {
        GUIObserver.super.onPlayerToPlayChanged(nickname);
    }

    @Override
    public void onPlayerPPChanged(String nickname, int newPP) {
        GUIObserver.super.onPlayerPPChanged(nickname, newPP);
    }

    @Override
    public void onPlayerFoodChanged(String nickname, int newFood) {
        GUIObserver.super.onPlayerFoodChanged(nickname, newFood);
    }

    @Override
    public void onMarketInitialized(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld) {
        GUIObserver.super.onMarketInitialized(top, bot, topBld);
    }

    @Override
    public void onTopCardRemoved(int position) {
        GUIObserver.super.onTopCardRemoved(position);
    }

    @Override
    public void onBottomCardRemoved(int position) {
        GUIObserver.super.onBottomCardRemoved(position);
    }

    @Override
    public void onTopBuildRemoved(int position) {
        GUIObserver.super.onTopBuildRemoved(position);
    }

    @Override
    public void onTopCardRefreshed(List<CardDTO> top) {
        GUIObserver.super.onTopCardRefreshed(top);
    }

    @Override
    public void onTopBuildingRefreshed(List<BuildingDTO> topBld) {
        GUIObserver.super.onTopBuildingRefreshed(topBld);
    }

    @Override
    public void onBoardInitialized(List<OffertileDTO> tiles, List<DefaultTileDTO> defs) {
        Platform.runLater(() -> {
            ImageView imageView = new ImageView(new Image("/images/Tiles/defaultTile/"+defs.size()+"plDefTile.png"));
            tileRail.getChildren().add(imageView);
            for (OffertileDTO tile : tiles) {
                imageView = new ImageView(new Image("/images/Tiles/offertiles/"+tile.getOfferTileID()+"offerTile.png"));
                tileRail.getChildren().add(imageView);
            }
        });
    }

    @Override
    public void onPlayerPlacedOnOfferTile(String nickname, int tilePosition) {
        GUIObserver.super.onPlayerPlacedOnOfferTile(nickname, tilePosition);
    }

    @Override
    public void onActionAvailableChanged(int drawTop, int drawBot) {
        GUIObserver.super.onActionAvailableChanged(drawTop, drawBot);
    }

    @Override
    public void onEventResolved(String description) {
        GUIObserver.super.onEventResolved(description);
    }

    @Override
    public void onWinners(List<PlayerDTO> winners) {
        GUIObserver.super.onWinners(winners);
    }

}
