package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Remote interface implemented by each Mesos client. The server calls these methods
 * to push game-state updates (phase changes, market refreshes, player actions, errors)
 * back to the client, both via RMI stubs and via the {@link it.polimi.ingsw.am25.server.webLayer.Socket.ClientSocketProxy}.
 */
public interface ClientRemoteInterface extends Remote {
    // --- Game updates ---

    /**
     * Sends the initial game state to the client at game start.
     *
     * @param currentEra    the starting era.
     * @param gamePhase     the starting game phase.
     * @param PlayerToPlace nickname of the first player to place their totem.
     * @param PlayerToPlay  nickname of the first player to resolve actions.
     * @throws RemoteException if the RMI call fails.
     */
    void initializeGame(ERA currentEra, GAME_PHASE gamePhase, String PlayerToPlace, String PlayerToPlay) throws RemoteException;

    /**
     * Sends the list of winners when the game ends.
     *
     * @param playerDTOSWinner the ordered list of winning players.
     * @throws RemoteException if the RMI call fails.
     */
    void gameWinners(List<PlayerDTO> playerDTOSWinner) throws RemoteException;

    /**
     * Notifies the client that a new player has joined the lobby.
     *
     * @param playerAdded DTO of the player who just joined.
     * @throws RemoteException if the RMI call fails.
     */
    void playerAdded(PlayerDTO playerAdded) throws RemoteException;

    /**
     * Notifies the client that the game has advanced to a new era.
     *
     * @param newEra the era that just started.
     * @throws RemoteException if the RMI call fails.
     */
    void eraChanged(ERA newEra) throws RemoteException;

    /**
     * Notifies the client that the game phase has changed.
     *
     * @param gamePhase the new game phase.
     * @throws RemoteException if the RMI call fails.
     */
    void gamePhaseChanged(GAME_PHASE gamePhase) throws RemoteException;

    /**
     * Notifies the client which player must place their totem next.
     *
     * @param playerChanged DTO of the player whose turn it is to place.
     * @throws RemoteException if the RMI call fails.
     */
    void playerToPlaceChanged(PlayerDTO playerChanged) throws RemoteException;

    /**
     * Notifies the client which player must resolve their offer-tile actions next.
     *
     * @param playerChanged DTO of the player whose turn it is to play.
     * @throws RemoteException if the RMI call fails.
     */
    void playerToPlayChanged(PlayerDTO playerChanged) throws RemoteException;

    /**
     * Notifies the client of the updated draw counts for the current player's offer tile.
     *
     * @param action DTO carrying the remaining top-draw and bottom-draw counts.
     * @throws RemoteException if the RMI call fails.
     */
    void actionAvailableChanged(ActionDTO action) throws RemoteException;

    // --- Market updates ---

    /**
     * Sends the initial market state (top/bottom card rows and top building row) to the client.
     *
     * @param topCards     the initial top card row.
     * @param bottomCards  the initial bottom card row.
     * @param topBuildings    the initial top building row.
     * @param bottomBuildings the initial bottom building row.
     * @throws RemoteException if the RMI call fails.
     */
    void initializeMarket(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings, List<BuildingDTO> bottomBuildings) throws RemoteException;

    /**
     * Notifies the client that a card was removed from the top market row.
     *
     * @param position zero-based index of the removed card.
     * @throws RemoteException if the RMI call fails.
     */
    void topCardRemoved(int position) throws RemoteException;

    /**
     * Notifies the client that a building was removed from the top market row.
     *
     * @param position zero-based index of the removed building.
     * @throws RemoteException if the RMI call fails.
     */
    void topBuildRemoved(int position) throws RemoteException;

    /**
     * Notifies the client that a card was removed from the bottom market row.
     *
     * @param position zero-based index of the removed card.
     * @throws RemoteException if the RMI call fails.
     */
    void bottomCardRemoved(int position) throws RemoteException;

    /**
     * Notifies the client that a building was removed from the bottom market row.
     *
     * @param position zero-based index of the removed building.
     * @throws RemoteException if the RMI call fails.
     */
    void bottomBuildRemoved(int position) throws RemoteException;

    /**
     * Notifies the client that the top card row has been refreshed with new cards.
     *
     * @param topCards the new top card row.
     * @throws RemoteException if the RMI call fails.
     */
    void topCardRefreshed(List<CardDTO> topCards) throws RemoteException;

    /**
     * Notifies the client that the top building row has been refreshed with new buildings.
     *
     * @param topBuildingCards the new top building row.
     * @throws RemoteException if the RMI call fails.
     */
    void topBuildingRefreshed(List<BuildingDTO> topBuildingCards) throws RemoteException;

    // --- Player updates ---

    /**
     * Notifies the client of an updated food total for the given player.
     *
     * @param nickname the affected player's nickname.
     * @param food     the player's new food total.
     * @throws RemoteException if the RMI call fails.
     */
    void playerUpdateFood(String nickname, int food) throws RemoteException;

    /**
     * Notifies the client of an updated prestige-point total for the given player.
     *
     * @param nickname the affected player's nickname.
     * @param PP       the player's new prestige-point total.
     * @throws RemoteException if the RMI call fails.
     */
    void playerUpdatePP(String nickname, int PP) throws RemoteException;

    /**
     * Notifies the client that a tribe card was added to the given player's tribe.
     *
     * @param nickname the affected player's nickname.
     * @param cardDTO  the card that was added.
     * @throws RemoteException if the RMI call fails.
     */
    void addedCardToTribe(String nickname, CardDTO cardDTO) throws RemoteException;

    // --- Board updates ---

    /**
     * Sends the initial board state (offer tiles and default tiles) to the client.
     *
     * @param offerTileList   the list of offer tiles on the board.
     * @param defaultTileList the list of default tiles on the board.
     * @throws RemoteException if the RMI call fails.
     */
    void boardInitialize(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) throws RemoteException;

    /**
     * Notifies the client that a player placed their totem on an offer tile.
     *
     * @param PlayerNickname    the nickname of the player who placed their totem.
     * @param offertilePosition zero-based index of the offer tile that was occupied.
     * @throws RemoteException if the RMI call fails.
     */
    void playerPlacedOnOffertile(String PlayerNickname, int offertilePosition) throws RemoteException;

    /**
     * Notifies the client of the updated player order on the default tiles.
     *
     * @param orderOnDefaultTile the new ordered list of players on the default tiles.
     * @throws RemoteException if the RMI call fails.
     */
    void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile) throws RemoteException;

    // --- Draw-one-more mechanic ---

    /**
     * Asks the client to select one extra card from the end-of-round market snapshot
     * (draw-one-more building effect). The lists carry exactly the cards that were
     * available at round close, before the market refresh.
     *
     * @param snapshotCards     the top card row at end of round.
     * @param snapshotBuildings the top building row at end of round.
     * @throws RemoteException if the RMI call fails.
     */
    void askExtraDraw(List<CardDTO> snapshotCards, List<BuildingDTO> snapshotBuildings) throws RemoteException;

    /**
     * Sends an error message to the client (e.g. invalid action, not your turn).
     *
     * @param errorMessage a human-readable description of the error.
     * @throws RemoteException if the RMI call fails.
     */
    void showErrorMessage(String errorMessage) throws RemoteException;

    void eventResolved(int eventID, EVENT_TYPE eventType) throws RemoteException;

    void sendRank(Map<Integer, List<String>> Leaderboard) throws RemoteException;

    /**
     * Notifies the client that a player has disconnected from the game.
     * The client uses this to mark the player as DISCONNESSO in the UI.
     *
     * @param nickname the nickname of the disconnected player.
     * @throws RemoteException if the RMI call fails.
     */
    void playerDisconnected(String nickname) throws RemoteException;

    /**
     * Notifies the client that a previously-disconnected player has reconnected.
     *
     * @param nickname the nickname of the player who reconnected.
     * @throws RemoteException if the RMI call fails.
     */
    void playerReconnected(String nickname) throws RemoteException;

}
