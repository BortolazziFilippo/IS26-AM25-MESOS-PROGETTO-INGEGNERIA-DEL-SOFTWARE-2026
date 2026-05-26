package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;

import java.util.List;
import java.util.Map;

/**
 * Observer interface for GUI-level game events. All methods have empty default
 * implementations so that implementing classes only need to override the events they care about.
 */
public interface GUIObserver {

    /**
     * Called when the game phase transitions to a new state.
     *
     * @param phase the new game phase.
     */
    default void onGamePhaseChanged(GAME_PHASE phase) {
    }

    /**
     * Called when a new player joins the lobby.
     *
     * @param player the DTO of the newly added player.
     */
    default void onPlayerAdded(PlayerDTO player) {
    }

    /**
     * Called when an error message is received from the server.
     *
     * @param message a human-readable description of the error.
     */
    default void onError(String message) {
    }

    /**
     * Called when the player who must place their totem changes.
     *
     * @param nickname the nickname of the player who must place next.
     */
    default void onPlayerToPlaceChanged(String nickname) {
    }

    /**
     * Called when the active player (whose turn it is to draw/play) changes.
     *
     * @param nickname the nickname of the player whose turn it is.
     */
    default void onPlayerToPlayChanged(String nickname) {
    }

    /**
     * Called when a player's prestige-point total changes.
     *
     * @param nickname the nickname of the player whose PP changed.
     * @param newPP    the new prestige-point total.
     */
    default void onPlayerPPChanged(String nickname, int newPP) {
    }

    /**
     * Called when a player's food supply changes.
     *
     * @param nickname the nickname of the player whose food changed.
     * @param newFood  the new food total.
     */
    default void onPlayerFoodChanged(String nickname, int newFood) {
    }

    /**
     * Called once at game start with the full initial market state.
     *
     * @param top    the initial top card row.
     * @param bot    the initial bottom card row.
     * @param topBld the initial top building row.
     * @param botBld the initial bottom building row.
     */
    default void onMarketInitialized(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld, List<BuildingDTO> botBld) {
    }

    /**
     * Called when a card is removed from the top market row.
     *
     * @param position zero-based index of the removed card.
     */
    default void onTopCardRemoved(int position) {
    }

    /**
     * Called when a card is removed from the bottom market row.
     *
     * @param position zero-based index of the removed card.
     */
    default void onBottomCardRemoved(int position) {
    }

    /**
     * Called when a building is removed from the top building row.
     *
     * @param position zero-based index of the removed building.
     */
    default void onTopBuildRemoved(int position) {
    }

    /**
     * Called when a building is removed from the bottom building row.
     *
     * @param position zero-based index of the removed building.
     */
    default void onBottomBuildRemoved(int position) {
    }

    /**
     * Called when the top card row is refreshed after an end-of-round market action.
     *
     * @param top the new top card row.
     */
    default void onTopCardRefreshed(List<CardDTO> top) {
    }

    /**
     * Called when the top building row is refreshed after an end-of-round market action.
     *
     * @param topBld the new top building row.
     */
    default void onTopBuildingRefreshed(List<BuildingDTO> topBld) {
    }

    /**
     * Called once at game start with the full initial board layout.
     *
     * @param tiles the list of offer tile DTOs.
     * @param defs  the list of default tile DTOs.
     */
    default void onBoardInitialized(List<OffertileDTO> tiles, List<DefaultTileDTO> defs) {
    }

    /**
     * Called when a player moves their totem to an offer tile.
     *
     * @param nickname     the nickname of the player who moved.
     * @param tilePosition the index of the destination offer tile.
     * @param fromSlot     the slot within the tile that was occupied.
     */
    default void onPlayerPlacedOnOfferTile(String nickname, int tilePosition, int fromSlot) {
    }

    /**
     * Called when the order of players on the default tile changes.
     *
     * @param order the new ordered list of players on the default tile.
     */
    default void onDefaultTileOrderChanged(List<PlayerDTO> order) {
    }

    /**
     * Called when the number of draws available to the current player changes.
     *
     * @param drawTop the number of top-row draws remaining.
     * @param drawBot the number of bottom-row draws remaining.
     */
    default void onActionAvailableChanged(int drawTop, int drawBot) {
    }

    /**
     * Called when a card is added to a player's tribe.
     *
     * @param nickname the nickname of the player who received the card.
     * @param card     the DTO of the card that was added.
     */
    default void onCardAddedToTribe(String nickname, CardDTO card) {
    }

    /**
     * Called when an event card is resolved.
     *
     * @param eventID   the unique identifier of the resolved event.
     * @param eventType the category of the resolved event.
     */
    default void onEventResolved(int eventID, EVENT_TYPE eventType) {
    }

    /**
     * Called when the game ends and winners are determined.
     *
     * @param winners the ordered list of winning players.
     */
    default void onWinners(List<PlayerDTO> winners) {
    }

    /**
     * Called when the player must choose an extra card or building to draw
     * at the end of a round (triggered by the draw-one-more building effect).
     *
     * @param cards     the top card row snapshot available for the extra draw.
     * @param buildings the top building row snapshot available for the extra draw.
     */
    default void onAskExtraDraw(List<CardDTO> cards, List<BuildingDTO> buildings) {
    }

    /**
     * Called when a player disconnects from the game.
     *
     * @param nickname the nickname of the disconnected player.
     */
    default void onPlayerDisconnected(String nickname) {
    }

    /**
     * Called when a previously disconnected player reconnects.
     *
     * @param nickname the nickname of the reconnected player.
     */
    default void onPlayerReconnected(String nickname) {
    }

    /**
     * Called when the server becomes unreachable and the connection is considered dead.
     */
    default void onServerDead() {
    }

    /**
     * Called when the server sends the global leaderboard in response to an
     * {@code askForRank} request.
     *
     * @param leaderboards a map from player count to ordered list of leaderboard row strings.
     */
    default void onRankReceived(Map<Integer, List<String>> leaderboards) {
    }

}
