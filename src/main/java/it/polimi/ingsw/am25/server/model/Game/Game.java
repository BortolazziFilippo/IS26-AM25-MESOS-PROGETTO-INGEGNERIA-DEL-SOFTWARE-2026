package it.polimi.ingsw.am25.server.model.Game;

import it.polimi.ingsw.am25.server.model.Board.Board;
import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.DBmanager.DBManager;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Observers.GameObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesConstant;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Core game model for a Mesos session. Manages the board, market, turn order, era
 * progression, and all game-phase transitions from setup through end-game.
 * Notifies registered {@link it.polimi.ingsw.am25.server.model.Observers.GameObserver}s on every state change.
 */
public class Game implements GameView {
    private static final String LOG_PREFIX = "[SERVER][GAME]";
    private ERA currentEra = ERA.ERA_I;
    private final Board board;
    private final BoardView boardView;
    private final Market market;
    private final TurnManager turnManager;
    private final Map<String, Player> players;
    private final Player playerHost;
    private final int playerNumber;
    private GAME_PHASE gamePhase;
    private Player playerToPlace;
    private Player playerToPlay;
    private OfferTile offertilePlayerIsOn;
    private final List<GameObserver> observers = new ArrayList<>();

    /**
     * default constructor of game, this method when called creates the deck and the buildings by launching the factories.
     *
     * @param playerHost   player who created the game
     * @param playerNumber number of players
     */
    public Game(Player playerHost, int playerNumber) {
        if (playerHost == null) {
            throw new IllegalArgumentException("playerHost is null");
        }
        this.playerNumber = playerNumber;
        this.gamePhase = GAME_PHASE.SETUP;
        this.board = new Board(this);
        this.boardView = board;
        this.market = new Market(this, board);
        this.turnManager = new TurnManager(board);
        this.playerHost = playerHost;
        this.players = new HashMap<>();
        players.put(playerHost.getNickname(), playerHost);
        notifyGameChanged();
    }

    /**
     * Returns the current game phase.
     *
     * @return the current {@link GAME_PHASE}
     */
    public GAME_PHASE getGamePhase() {
        return gamePhase;
    }

    /**
     * this method tries to add a player, in case the lobby is already full it does nothing.
     * in case, after adding the player, the lobby is ready it launches the gameReadyToStartException
     *
     * @param player player to add
     * @throws GameReadyToStartException exception thrown when the lobby gets full after adding the player
     */
    public void addPlayer(Player player) throws GameReadyToStartException {
        if (players.size() < playerNumber) {
            players.put(player.getNickname(), player);
            notifyPlayerAdded(player);
            if (players.size() == playerNumber) {
                throw new GameReadyToStartException("The lobby is full, game can start");
            }
        }
    }

    /**
     * this method sets up the game after it is full:
     * - it randomly place players on a default tile
     * - it sets the game to the next phase, it notifies the player updating the views
     */
    public void gameStart() {
        if (gamePhase != GAME_PHASE.SETUP) {
            return;
        }
        List<Integer> random = UtilitiesFunction.shuffledFromYToXExclusive(0, playerNumber);
        int counter = 1;
        for (Player player : players.values().stream().toList()) {
            try {
                board.placePlayerOnDefaultTile(player, random.getFirst());
                switch (counter) {
                    case 1:
                        player.manageFoodAndPP(+2);
                        break;
                    case 2, 3:
                        player.manageFoodAndPP(+3);
                        break;
                    case 4, 5:
                        player.manageFoodAndPP(+4);
                        break;
                }
                counter++;
            } catch (TileOccupiedException e) {
                throw new RuntimeException(getClass() + " Errore gamestart placePlayer");
            }
            random.removeFirst();
        }
        turnManager.updatePlacingOrder();
        try {
            this.playerToPlace = turnManager.getNextPlacingPlayer();
        } catch (EndOfPlacingPhaseException e) {
            throw new RuntimeException(getClass() + " Errore placing player in gamestart");
        }

        this.gamePhase = GAME_PHASE.PLACING_PHASE;
        notifyGamePhaseChanged();
        notifyPlayerToPlaceChanged();
    }

    /**
     * Places a player on an offer tile during the placing phase.
     * If all players have already placed, it signals the end of placing phase.
     *
     * @param player   player to place
     * @param position target offer-tile index
     * @throws IndexOutOfBoundsException if the position is not valid
     * @throws TileOccupiedException     if the target tile is already occupied
     */
    public void placePlayer(Player player, int position) throws IndexOutOfBoundsException, TileOccupiedException, EndOfPlacingPhaseException {
        Player player1 = players.get(player.getNickname());
        board.placePlayerOnOffertile(player1, position);
        logServerEvent("Player '" + player1.getNickname() + "' placed on offer tile position " + position);
        // Try to set the next placing player; if there is none, placing phase is over.
        try {
            this.playerToPlace = turnManager.getNextPlacingPlayer();
        } catch (EndOfPlacingPhaseException e) {
            throw new EndOfPlacingPhaseException("all Player have placed");
        }
        notifyPlayerToPlaceChanged();
    }

    /**
     * Advances the placing turn without an actual totem placement, used when the current
     * placing player has disconnected. Mirrors the tail of {@link #placePlayer} after a
     * successful placement.
     */
    public void forceAdvancePlacing() {
        try {
            this.playerToPlace = turnManager.getNextPlacingPlayer();
            notifyPlayerToPlaceChanged();
        } catch (EndOfPlacingPhaseException e) {
            advancePlayingPhase();
        }
    }

    /**
     * Transitions the game from the placing phase to the playing (resolve-action) phase.
     * Updates the playing order, sets the first player to play, and — if that player is
     * on offer tile 'A' (no actions, only food) — automatically advances to the next player.
     */
    public void advancePlayingPhase() {
        turnManager.updatePlayingOrder();
        try {
            this.playerToPlay = turnManager.getNextPlayingPlayer();
        } catch (EndOfPlayingPhaseException ex) {
            throw new RuntimeException(getClass() + " errore transizione placing->playing");
        }
        this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(playerToPlay);
        // If the player is on tile A, they have no actions and only gain food.
        // This check is needed only for the first playing player.
        checkPlayerOfferTile(playerToPlay);
        if (this.gamePhase == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            gamePhase = GAME_PHASE.LAST_ROUND_RESOLVE_ACTION;
        } else {
            this.gamePhase = GAME_PHASE.RESOLVE_ACTION;
        }
        notifyActionChanged();
        notifyGamePhaseChanged();
        notifyPlayerToPlayChanged();
    }

    /**
     * Checks whether the given player is on offer tile A.
     * If so, grants the tile-A food bonus and advances to the next player.
     *
     * @param player player to check
     */
    private void checkPlayerOfferTile(Player player) {
        if (offertilePlayerIsOn.getOfferTileID() == 'A') {
            player.manageFoodAndPP(UtilitiesConstant.FOOD_OFFERTILE_A);
            logServerEvent("Player '" + player.getNickname() + "' received " + UtilitiesConstant.FOOD_OFFERTILE_A + " food from offer tile A");
            try {
                this.playerToPlay = turnManager.getNextPlayingPlayer();
            } catch (EndOfPlayingPhaseException e) {
                throw new RuntimeException(getClass() + " errore controllo checkPlayerOffertile");
            }
            this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(playerToPlay);
        }
    }

    /**
     * Determines the winner(s) at game end and notifies all observers.
     * Returns a single player when there is a clear prestige-point leader, a single player
     * when prestige points are tied but one has more food, or multiple players when both
     * prestige points and food are equal.
     *
     * @return the list of winning players (one or more).
     */
    public List<Player> checkWinner() {
        if (this.players == null || this.players.isEmpty()) {
            throw new IllegalStateException("No players found");
        }

        List<Player> winners = players.values().stream()
                .sorted(Comparator.comparing(Player::getPrestigePoint).thenComparing(Player::getFood).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        Player topWinner = winners.get(0);

        List<Player> winningPlayers;
        if (winners.get(0).getPrestigePoint() == winners.get(1).getPrestigePoint()) {
            if (winners.get(0).getFood() == winners.get(1).getFood()) {
                winningPlayers = winners.stream()
                        .filter(player -> player.getPrestigePoint() == topWinner.getPrestigePoint()
                                && player.getFood() == topWinner.getFood())
                        .collect(Collectors.toCollection(ArrayList::new));
            } else {
                winningPlayers = new ArrayList<>();
                winningPlayers.add(topWinner);
            }
        } else {
            winningPlayers = new ArrayList<>();
            winningPlayers.add(topWinner);
        }
        //thread for writing winners on DB
        try {
            DBManager.logMatch(winners);
        } catch (IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX + "Errore lettura file credenziali database");
        } catch (SQLException e) {
            UtilitiesFunction.logError(LOG_PREFIX + "Errore comunicazione con database");
        }
        this.notifyWinners(winningPlayers);
        return winningPlayers;


    }

    /**
     * Triggers all end-of-round actions:
     * moves players back to default tiles, triggers end-round buildings,
     * and advances the market (shifting card lists and refilling them).
     * If the deck is exhausted the game phase transitions to {@link GAME_PHASE#END_GAME}.
     * If the game is already in END_GAME, delegates to {@link #endGameIter()}.
     *
     * @throws EndGameException when the game is finished
     */
    public void nextRoundIter() throws EndGameException {
        if (this.gamePhase != GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {

            // 1. all player goes back to default tile
            board.returnOnDefaultTiles();
            // 2. ricalculating order
            turnManager.updatePlacingOrder();
            // 3. Notify SOLVING_EVENTS before potentially blocking on extra-draw requests
            this.gamePhase = GAME_PHASE.SOLVING_EVENTS;
            notifyGamePhaseChanged();
            // 4. Snapshot top market state BEFORE refresh so draw-one-more sees round-closing cards
            market.snapshotForExtraDraw();
            // 5. Activate end-of-round buildings (such as Draw One More Card)
            players.values().forEach(Player::triggerEndRoundBuilding);
            // 6. Update the market and set the new phase
            try {
                market.endOfRoundMarketActions();
                this.gamePhase = GAME_PHASE.PLACING_PHASE;
            } catch (DeckFinishedException e) {
                this.gamePhase = GAME_PHASE.LAST_ROUND_PLACING_PHASE;
                logServerEvent("Deck exhausted. Advancing to LAST_ROUND_PLACING_PHASE");
            }

            // 5. Now ask the player regardless of the previous try/catch!
            try {
                this.playerToPlace = turnManager.getNextPlacingPlayer();
                logServerEvent("Round ended. Advancing to " + this.gamePhase);
                notifyGamePhaseChanged();
                notifyPlayerToPlaceChanged();
            } catch (EndOfPlacingPhaseException e) {
                e.printStackTrace();
            }
        } else {
            throw new EndGameException("Game finished");
        }
    }

    /**
     * applies the building effect if any of the players has at least one in their tribe
     * solves the final events and the other events if there are any other
     * manages the  prestige points by checking the tribe each of the players have
     */

    public void endGameIter() {
        gamePhase = GAME_PHASE.END_GAME;
        notifyGamePhaseChanged();
        players.values().forEach(Player::triggerEndGameBuilding);
        market.solveFinalEvents();
        for (Player p : players.values()) {
            p.managePP(p.checkpoints());
        }
    }

    /**
     * this method adds a card from the top list
     *
     * @param toBuyCardType cardType to buy
     * @param position      position of the card
     * @param player        player buying the card
     * @throws IndexOutOfBoundsException  in case the position is not valid
     * @throws NotEnoughFoodException     in case the player does not have enough food
     * @throws NotSelectableCardException if the player tries to select an event card
     * @throws NoMoreActionToDo           if the player has no remaining actions after this selection
     */
    public void selectGenericCardTopLists(CARD_TYPE toBuyCardType, int position, Player player) throws IndexOutOfBoundsException, NotSelectableCardException, NotEnoughFoodException, EmptyMarketException, NoMoreActionToDo {
        Player player1 = players.get(player.getNickname());
        switch (toBuyCardType) {
            case BUILDING -> market.buyBuildingTopList(position, player1);
            case EVENT -> throw new NotSelectableCardException("cannot select an event");
            default -> market.selectCardFromTopList(position, player1);
        }
        logServerEvent("Player '" + player1.getNickname() + "' selected a " + toBuyCardType + " card from top list at position " + position);
        offertilePlayerIsOn.getActionAvailable().subtractOneTopAction();
        notifyActionChanged();
        if (offertilePlayerIsOn.getActionAvailable().getDrawFromBottom() == 0 && offertilePlayerIsOn.getActionAvailable().getDrawTop() == 0) {
            throw new NoMoreActionToDo();
        }

    }

    /**
     * Draws one extra card from the top market row for the given player without touching
     * the offer-tile action counter. Used exclusively by the draw-one-more building effect.
     *
     * @param cardType type of card to draw
     * @param position index in the top row
     * @param player   the player who owns the building
     * @throws IndexOutOfBoundsException  if {@code position} is out of range
     * @throws NotSelectableCardException if the card at that position is an event
     * @throws NotEnoughFoodException     if the player cannot afford a building
     * @throws EmptyMarketException       if the top row has no selectable cards
     */
    public void selectExtraCardFromTopList(CARD_TYPE cardType, int position, Player player)
            throws IndexOutOfBoundsException, NotSelectableCardException, NotEnoughFoodException, EmptyMarketException {
        Player player1 = players.get(player.getNickname());
        switch (cardType) {
            case BUILDING -> market.buyExtraBuildingFromSnapshot(position, player1);
            case EVENT -> throw new NotSelectableCardException("cannot select an event");
            default -> market.selectExtraCardFromSnapshot(position, player1);
        }
        logServerEvent("Player '" + player1.getNickname() + "' drew extra card from snapshot of type " + cardType + " at position " + position);
    }

    /**
     * Checks whether the current playing player has at least one legal market action.
     *
     * @return {@code true} if the player can draw from at least one non-blocked market row;
     * {@code false} if both rows are empty or contain only events, or the player has no remaining draws.
     */
    public boolean canCurrentPlayingPlayerDoSomething() {

        // 1. Is the Market blocked?
        // (allMatch returns 'true' if the list only contains EVENT cards OR if the list is completely empty)
        boolean isTopBlocked = market.getTopCardList().stream()
                .allMatch(card -> card.getCardType() == CARD_TYPE.EVENT);

        boolean isBottomBlocked = market.getBottomCardList().stream()
                .allMatch(card -> card.getCardType() == CARD_TYPE.EVENT);

        // 2. Does the player have the necessary actions from their current OfferTile?
        boolean hasTopAction = offertilePlayerIsOn.getActionAvailable().getDrawTop() > 0;
        boolean hasBottomAction = offertilePlayerIsOn.getActionAvailable().getDrawFromBottom() > 0;

        // 3. A specific row is playable ONLY IF the player has the action AND the row is not blocked
        boolean canPlayTop = hasTopAction && !isTopBlocked;
        boolean canPlayBottom = hasBottomAction && !isBottomBlocked;

        // 4. Final result: as long as the player can perform at least ONE action, return true
        return canPlayTop || canPlayBottom;
    }

    /**
     * Selects a card from the bottom list and adds it to the player, then decrements the
     * player's remaining bottom-draw actions. When all actions are exhausted the turn
     * automatically advances to the next player.
     *
     * @param toBuyCardType cardType to buy
     * @param position      position of the card in the bottom list
     * @param player        player buying the card
     * @throws IndexOutOfBoundsException  in case the position is not valid
     * @throws NotEnoughFoodException     in case the player does not have enough food
     * @throws NotSelectableCardException if the player tries to select an event card
     */
    public void selectGenericCardBottomLists(CARD_TYPE toBuyCardType, int position, Player player) throws IndexOutOfBoundsException, NotSelectableCardException, NotEnoughFoodException, EmptyMarketException, NoMoreActionToDo {
        Player player1 = players.get(player.getNickname());
        switch (toBuyCardType) {
            case BUILDING -> market.buyBuildingBottomList(position, player1);
            case EVENT -> throw new NotSelectableCardException("cannot select an event");
            default -> market.selectCardFromBottomList(position, player1);
        }
        logServerEvent("Player '" + player1.getNickname() + "' selected a " + toBuyCardType + " card from bottom list at position " + position);
        offertilePlayerIsOn.getActionAvailable().subtractOneBotAction();
        notifyActionChanged();
        if (offertilePlayerIsOn.getActionAvailable().getDrawFromBottom() == 0 && offertilePlayerIsOn.getActionAvailable().getDrawTop() == 0) {
            throw new NoMoreActionToDo();
        }
    }

    /**
     * Advances the game turn to the next playing player.
     * Updates {@code playerToPlay}  and refreshes the tile the player is on.
     *
     * @throws EndOfPlayingPhaseException if there are no more players left to play this round
     */
    public void goNextPlayingPlayer() throws EndOfPlayingPhaseException {
        this.playerToPlay = turnManager.getNextPlayingPlayer();
        this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(playerToPlay);
        notifyActionChanged();
        logServerEvent("Turn passed to player '" + this.playerToPlay.getNickname() + "'");
        notifyPlayerToPlayChanged();
    }

    /**
     * Returns the offer tile the current playing player is on (snapshot copy).
     *
     * @return the {@link OfferTile} the current player is on
     */
    public OfferTile getOffertilePlayerIsOn() {
        return offertilePlayerIsOn;
    }

    /**
     * Returns the player whose turn it is to place during the placing phase.
     *
     * @return the player to place
     */
    public Player getPlayerToPlace() {
        return playerToPlace;
    }

    /**
     * Returns the player whose turn it is to resolve actions during the playing phase.
     *
     * @return the player to play
     */
    public Player getPlayerToPlay() {
        return playerToPlay;
    }

    /**
     * Returns the market instance for this game.
     *
     * @return the {@link Market}
     */
    public Market getMarket() {
        return this.market;
    }

    /**
     * Returns the board instance for this game.
     *
     * @return the {@link Board}
     */
    public Board getBoard() {
        return this.board;
    }

    /**
     * Subscribes an observer to game-state changes.
     *
     * @param observerToAdd observer to subscribe; ignored if null or already subscribed
     */
    public void addObserver(GameObserver observerToAdd) {
        if (observerToAdd != null && !observers.contains(observerToAdd)) {
            observers.add(observerToAdd);
        }
    }

    /**
     * Unsubscribes an observer from game-state changes.
     *
     * @param observerToRemove observer to unsubscribe
     */
    public void removeObserver(GameObserver observerToRemove) {
        observers.remove(observerToRemove);
    }

    /**
     * Notifies all subscribed observers with a snapshot of the current game state.
     */
    private void notifyGameChanged() {
        List<Player> playersSnapshot = List.copyOf(this.players.values());
        notify(observer -> observer.onGameChanged(
                this.currentEra,
                playersSnapshot,
                this.gamePhase,
                this.playerToPlace,
                this.playerToPlay
        ));
    }

    /**
     * this method notifies the winners
     *
     * @param winners list of winners
     */
    private void notifyWinners(List<Player> winners) {
        notify(observer -> observer.gameWinners(winners));
    }

    /**
     * method to notify that  {@code player} has been added
     *
     * @param player player to add
     */
    private void notifyPlayerAdded(Player player) {
        notify(observer -> observer.onPlayerAdded(player));

    }

    /**
     * Executes notify era changed.
     */
    private void notifyEraChanged() {
        notify(observer -> observer.onEraChanged(this.currentEra));
    }

    /**
     * Executes notify game phase changed.
     */
    private void notifyGamePhaseChanged() {
        notify(observer -> observer.onGamePhaseChanged(gamePhase));
    }

    /**
     * Executes notify player to place changed.
     */
    private void notifyPlayerToPlaceChanged() {
        notify(observer -> observer.onPlayerToPlaceChanged(playerToPlace));
    }

    /**
     * Executes notify player to play changed.
     */
    private void notifyPlayerToPlayChanged() {
        notify(observer -> observer.onPlayerToPlayChanged(playerToPlay));
    }

    /**
     * Returns the total number of players in this game.
     *
     * @return number of players
     */
    @Override
    public int getPlayerNumber() {
        return this.playerNumber;
    }

    /**
     * Returns the current era of the game.
     *
     * @return the current {@link ERA}
     */
    @Override
    public ERA getCurrentEra() {
        return this.currentEra;
    }

    /**
     * Returns the list of all players in this game.
     *
     * @return list of players
     */
    @Override
    public List<Player> getPlayerList() {
        return this.players.values().stream().toList();
    }

    /**
     * Returns the model {@link Player} instance that matches the given player's nickname.
     *
     * @param player the player whose nickname is used for the lookup.
     * @return the matching {@link Player}, or {@code null} if not found.
     */
    public Player getSpecificPlayer(Player player) {
        return players.get(player.getNickname());
    }

    /**
     * Advances the game to the next era.
     * If the current era is already the last one, the era is left unchanged.
     */
    @Override
    public void nextEra() {
        // 1. Get all available eras (e.g., [ERA_I, ERA_II, ERA_III])
        ERA[] allEras = ERA.values();
        // 2. Calculate the index of the next era
        int nextPosition = this.currentEra.ordinal() + 1;
        // 3. Safety check: are we already at the last era?
        if (nextPosition < allEras.length) {
            this.currentEra = allEras[nextPosition];
        }
        logServerEvent("Era changed: " + currentEra);
        notifyEraChanged();
    }

    /**
     * Executes link observer.
     *
     * @param vv parameter vv.
     */
    public void linkObserver(ServerVirtualView vv) {
        this.addObserver(vv);
        board.addObserver(vv);
        market.addObserver(vv);
    }

    private void notify(Consumer<GameObserver> action) {
        for (GameObserver gameObserver : List.copyOf(observers)) {
            action.accept(gameObserver);
        }
    }

    /**
     * Executes notify changes.
     */
    public void notifyChanges() {
        this.market.notifyMarketChanged();
        this.board.notifyBoardChanged();
    }

    /**
     * Executes notify action changed.
     */
    private void notifyActionChanged() {
        notify(o -> o.actionOfferTileChanged(this.offertilePlayerIsOn.getActionAvailable().getDrawTop(), this.offertilePlayerIsOn.getActionAvailable().getDrawFromBottom()));
    }

    /**
     * Executes log server event.
     *
     * @param message parameter message.
     */
    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }

    // --- DISCONNECTION SUPPORT ---

    /**
     * Removes the given player from both the placing and playing turn queues immediately.
     * Called when a player disconnects so the game never waits for them.
     * @param player the disconnected player.
     */
    public void removeFromTurnQueues(Player player) {
        turnManager.removePlayer(player);
    }

    /**
     * Re-adds a reconnected player to the end of both turn queues.
     * @param player the reconnected player.
     */
    public void reAddToTurnQueues(Player player) {
        turnManager.reAddPlayer(player);
    }

    /**
     * Advances the placing phase to the next connected player without placing a totem.
     * Used when the current placing player has disconnected mid-phase.
     * @throws EndOfPlacingPhaseException if no more connected players need to place.
     */
    public void skipDisconnectedPlacingPlayer() throws EndOfPlacingPhaseException {
        this.playerToPlace = turnManager.getNextPlacingPlayer();
        notifyPlayerToPlaceChanged();
    }

}
