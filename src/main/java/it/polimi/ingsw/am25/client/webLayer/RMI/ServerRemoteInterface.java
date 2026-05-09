package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface exposed by the Mesos server. Clients call these methods to
 * create/join a lobby and to perform in-game actions (totem placement, card draws).
 */
public interface ServerRemoteInterface extends Remote {

    /**
     * Creates a new game lobby with the given host player.
     *
     * @param playerHost            the hosting player's data (nickname and totem color).
     * @param PlayerNumber          the number of players required to start (2–5).
     * @param clientRemoteInterface the host's RMI stub for receiving push notifications.
     * @throws RemoteException       if the RMI call fails.
     * @throws IllegalStateException if a lobby already exists.
     */
    void createGame(PlayerDTO playerHost, int PlayerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException, IllegalStateException;

    /**
     * Adds a player to the existing lobby.
     *
     * @param playerDTO             the joining player's data.
     * @param clientRemoteInterface the joining player's RMI stub.
     * @throws RemoteException                  if the RMI call fails.
     * @throws GameFullException                if the lobby is already at capacity.
     * @throws GameReadyToStartException        if this player fills the last slot (game will start).
     * @throws NameOrColorAlreadyTakenException if the nickname or totem color is already in use.
     */
    void addPlayer(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameFullException, GameReadyToStartException, NameOrColorAlreadyTakenException;

    /**
     * Places the given player's totem on the specified offer tile during the placing phase.
     *
     * @param playerToPlace the player placing their totem.
     * @param position      zero-based index of the target offer tile.
     * @throws RemoteException           if the RMI call fails.
     * @throws IndexOutOfBoundsException if the position is out of range.
     * @throws TileOccupiedException     if the tile is already occupied.
     */
    void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException;

    /**
     * Selects a card from the top market row and adds it to the player's tribe.
     *
     * @param player   the acting player.
     * @param cardType the type of card to select.
     * @param position zero-based index in the top row.
     * @throws RemoteException            if the RMI call fails.
     * @throws IndexOutOfBoundsException  if the position is out of range.
     * @throws NotEnoughFoodException     if the player cannot afford the card.
     * @throws NotSelectableCardException if the card type cannot be selected.
     * @throws EmptyMarketException       if the row has no selectable cards.
     * @throws ActionNotAvailable         if it is not this player's turn.
     */
    void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, ActionNotAvailable;

    /**
     * Selects a card from the bottom market row and adds it to the player's tribe.
     *
     * @param player   the acting player.
     * @param cardType the type of card to select.
     * @param position zero-based index in the bottom row.
     * @throws IndexOutOfBoundsException  if the position is out of range.
     * @throws NotEnoughFoodException     if the player cannot afford the card.
     * @throws NotSelectableCardException if the card type cannot be selected.
     * @throws EmptyMarketException       if the row has no selectable cards.
     * @throws RemoteException            if the RMI call fails.
     */
    void selectCardFromBottomList(PlayerDTO player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, RemoteException;

    /**
     * Signals that the player passes their turn without drawing a card
     * (used when no valid market action is available).
     *
     * @param playerDTO the player who is skipping.
     * @throws Exception if an unexpected error occurs.
     */
    void playerDoNothing(PlayerDTO playerDTO) throws Exception;

    /**
     * Selects one extra card from the market, triggered by the draw-one-more building effect.
     *
     * @param player   the acting player.
     * @param cardType the type of card to select.
     * @param position zero-based index in the relevant row.
     * @throws RemoteException            if the RMI call fails.
     * @throws IndexOutOfBoundsException  if the position is out of range.
     * @throws NotEnoughFoodException     if the player cannot afford the card.
     * @throws NotSelectableCardException if the card type cannot be selected.
     * @throws EmptyMarketException       if the row has no selectable cards.
     */
    void selectExtraCard(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException;

    /**
     * Skips the extra draw granted by the draw-one-more building effect without picking any
     * card. Releases the server thread waiting for the player's response so the round can
     * continue normally.
     *
     * @param player the player declining the extra draw.
     * @throws RemoteException if the RMI call fails.
     */
    void skipExtraDraw(PlayerDTO player) throws RemoteException;

    /**
     * asks the server for the ranks
     *
     * @param playerNumber number of player
     * @throws RemoteException if the RMI call fails.
     */
    void askForRank(String playerNumber, ClientRemoteInterface clientRemoteInterface) throws RemoteException;

    /**
     * Heartbeat signal sent by the client to the server to prove it is still connected.
     * The server resets the missed-ping counter for this player on every call.
     *
     * @param player the calling player's data (used to identify the sender).
     * @throws RemoteException if the RMI call fails.
     */
    void ping(PlayerDTO player) throws RemoteException;
}
