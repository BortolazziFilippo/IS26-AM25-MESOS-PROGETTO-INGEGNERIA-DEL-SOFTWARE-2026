package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;

import java.util.List;

public interface GUIObserver {
    default void onGamePhaseChanged(GAME_PHASE phase) {}
    default void onPlayerAdded(PlayerDTO player) {}
    default void onError(String message) {}
    default void onPlayerToPlaceChanged(String nickname) {}
    default void onPlayerToPlayChanged(String nickname) {}
    default void onPlayerPPChanged(String nickname, int newPP) {}
    default void onPlayerFoodChanged(String nickname, int newFood) {}
    default void onMarketInitialized(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld) {}
    default void onTopCardRemoved(int position) {}
    default void onBottomCardRemoved(int position) {}
    default void onTopBuildRemoved(int position) {}
    default void onBottomBuildRemoved(int position) {}
    default void onTopCardRefreshed(List<CardDTO> top) {}
    default void onTopBuildingRefreshed(List<BuildingDTO> topBld) {}
    default void onBoardInitialized(List<OffertileDTO> tiles, List<DefaultTileDTO> defs) {}
    default void onPlayerPlacedOnOfferTile(String nickname, int tilePosition, int fromSlot) {}
    default void onDefaultTileOrderChanged(List<PlayerDTO> order) {}
    default void onActionAvailableChanged(int drawTop, int drawBot) {}
    default void onCardAddedToTribe(String nickname, CardDTO card) {}
    default void onEventResolved(int eventID, EVENT_TYPE eventType) {}
    default void onWinners(List<PlayerDTO> winners) {}
    default void onAskExtraDraw(List<CardDTO> cards, List<BuildingDTO> buildings) {}
    default void onPlayerDisconnected(String nickname) {}

}
