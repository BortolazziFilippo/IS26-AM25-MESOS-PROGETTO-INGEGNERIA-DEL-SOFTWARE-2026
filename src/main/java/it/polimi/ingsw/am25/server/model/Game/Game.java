package it.polimi.ingsw.am25.server.model.Game;

import it.polimi.ingsw.am25.server.model.Board.Board;
import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.DBmanager.DBManager;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Factory.Building.BuildingFactory;
import it.polimi.ingsw.am25.server.model.Observers.GameObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesConstant;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.model.persistance.GameMemento;
import it.polimi.ingsw.am25.server.model.persistance.MementoManager;
import it.polimi.ingsw.am25.server.model.persistance.PlayerMemento;
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
public class Game implements GameView, MementoManager<GameMemento> {
    private static final String LOG_PREFIX = "[SERVER][GAME]";
    private final Board board;
    private final BoardView boardView;
    private final Market market;
    private final TurnManager turnManager;
    private final Map<String, Player> players;
    private final int playerNumber;
    private final List<GameObserver> observers = new ArrayList<>();
    private ERA currentEra = ERA.ERA_I;
    private GAME_PHASE gamePhase;
    private Player playerToPlace;
    private Player playerToPlay;
    private OfferTile offertilePlayerIsOn;

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
        this.players = new HashMap<>();
        players.put(playerHost.getNickname(), playerHost);
        notifyGameChanged();
    }

    /**
     * constructor for loading game
     *
     * @param playerNumber number of players
     */
    public Game(int playerNumber) {
        this.playerNumber = playerNumber;
        this.gamePhase = GAME_PHASE.SETUP;
        this.board = new Board(this);
        this.boardView = board;
        this.market = new Market(this, board);
        this.turnManager = new TurnManager(board);
        this.players = new HashMap<>();
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
        for (Player player : players.values()) {
            try {
                board.placePlayerOnDefaultTile(player, random.getFirst());
                int food = switch (counter) {
                    case 1 -> 2;
                    case 2, 3 -> 3;
                    default -> 4;  // case 4, 5
                };
                player.manageFoodAndPP(food);
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

        List<Player> winners = players.values().stream().sorted(Comparator.comparing(Player::getPrestigePoint).thenComparing(Player::getFood).reversed()).collect(Collectors.toCollection(ArrayList::new));

        Player topWinner = winners.get(0);

        boolean ppTied = topWinner.getPrestigePoint() == winners.get(1).getPrestigePoint();
        boolean foodTied = topWinner.getFood() == winners.get(1).getFood();

        List<Player> winningPlayers;
        if (ppTied && foodTied) {
            winningPlayers = winners.stream().filter(p -> p.getPrestigePoint() == topWinner.getPrestigePoint() && p.getFood() == topWinner.getFood()).collect(Collectors.toCollection(ArrayList::new));
        } else {
            winningPlayers = new ArrayList<>(List.of(topWinner));
        }
        Thread dbThread = new Thread(() -> {
            try {
                DBManager.logMatch(winners);
            } catch (IOException e) {
                UtilitiesFunction.logError(LOG_PREFIX + "Errore lettura file credenziali database");
            } catch (SQLException e) {
                UtilitiesFunction.logError(LOG_PREFIX + "Errore comunicazione con database");
            }
        });
        dbThread.setDaemon(true);
        dbThread.setName("db-log-match");
        dbThread.start();
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

            // 7. Now ask the player regardless of the previous try/catch!
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
        selectGenericCard(toBuyCardType, position, player, (p, pos) -> market.buyBuildingTopList(pos, p), (p, pos) -> market.selectCardFromTopList(pos, p), "top list", () -> offertilePlayerIsOn.getActionAvailable().subtractOneTopAction());
    }

    /**
     * Shared implementation for top- and bottom-list card selection.
     * Resolves the player, dispatches to the correct market method based on card type,
     * logs the event, subtracts the appropriate action, and throws {@link NoMoreActionToDo}
     * when all remaining actions are exhausted.
     *
     * @param cardType       the type of card to select.
     * @param position       index in the market row.
     * @param player         the acting player (looked up by nickname).
     * @param buyBuilding    market method to call when buying a building.
     * @param selectCard     market method to call for a tribe card.
     * @param rowLabel       "top list" or "bottom list" — used for the log message.
     * @param subtractAction action to decrement the correct counter on the offer tile.
     */
    private void selectGenericCard(CARD_TYPE cardType, int position, Player player, MarketAction buyBuilding, MarketAction selectCard, String rowLabel, Runnable subtractAction) throws IndexOutOfBoundsException, NotSelectableCardException, NotEnoughFoodException, EmptyMarketException, NoMoreActionToDo {
        Player player1 = players.get(player.getNickname());
        switch (cardType) {
            case BUILDING -> buyBuilding.execute(player1, position);
            case EVENT -> throw new NotSelectableCardException("cannot select an event");
            default -> selectCard.execute(player1, position);
        }
        logServerEvent("Player '" + player1.getNickname() + "' selected a " + cardType + " card from " + rowLabel + " at position " + position);
        subtractAction.run();
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
    public void selectExtraCardFromTopList(CARD_TYPE cardType, int position, Player player) throws IndexOutOfBoundsException, NotSelectableCardException, NotEnoughFoodException, EmptyMarketException {
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
        boolean isTopBlocked = market.getTopCardList().stream().allMatch(card -> card.getCardType() == CARD_TYPE.EVENT);

        boolean isBottomBlocked = market.getBottomCardList().stream().allMatch(card -> card.getCardType() == CARD_TYPE.EVENT);

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
        selectGenericCard(toBuyCardType, position, player, (p, pos) -> market.buyBuildingBottomList(pos, p), (p, pos) -> market.selectCardFromBottomList(pos, p), "bottom list", () -> offertilePlayerIsOn.getActionAvailable().subtractOneBotAction());
    }

    /**
     * Advances the game turn to the next playing player.
     * Updates {@code playerToPlay}  and refreshes the tile the player is on.
     *
     * @throws EndOfPlayingPhaseException if there are no more players left to play this round
     */
    public void goNextPlayingPlayer() throws EndOfPlayingPhaseException {
        // Loop so we can skip players who reconnected mid-round without having placed
        // their totem (they are not on any offer tile and cannot act this round).
        while (true) {
            Player next = turnManager.getNextPlayingPlayer(); // throws EndOfPlayingPhaseException when queue empty
            try {
                this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(next);
                this.playerToPlay = next;
                notifyActionChanged();
                logServerEvent("Turn passed to player '" + this.playerToPlay.getNickname() + "'");
                notifyPlayerToPlayChanged();
                return;
            } catch (IllegalStateException e) {
                logServerEvent("Skipping player '" + next.getNickname() + "' (not on any offer tile this round).");
            }
        }
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
        notify(observer -> observer.onGameChanged(this.currentEra, playersSnapshot, this.gamePhase, this.playerToPlace, this.playerToPlay));
    }

    /**
     * Notifies all observers of the final winners list.
     *
     * @param winners list of winning players.
     */
    private void notifyWinners(List<Player> winners) {
        notify(observer -> observer.gameWinners(winners));
    }

    /**
     * Notifies all observers that a player has been added to the lobby.
     *
     * @param player the player who joined.
     */
    private void notifyPlayerAdded(Player player) {
        notify(observer -> observer.onPlayerAdded(player));

    }

    /**
     * Notifies all observers that the era has changed.
     */
    private void notifyEraChanged() {
        notify(observer -> observer.onEraChanged(this.currentEra));
    }

    /**
     * Notifies all observers that the game phase has changed.
     */
    private void notifyGamePhaseChanged() {
        notify(observer -> observer.onGamePhaseChanged(gamePhase));
    }

    /**
     * Notifies all observers that the current placing player has changed.
     */
    private void notifyPlayerToPlaceChanged() {
        notify(observer -> observer.onPlayerToPlaceChanged(playerToPlace));
    }

    /**
     * Notifies all observers that the current playing player has changed.
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
     * Registers a {@link ServerVirtualView} as an observer of the game, board, and market,
     * so it receives all state-change notifications.
     *
     * @param vv the virtual view to register.
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
     * Re-fires the full market and board state to all observers.
     * Used when resuming a loaded game so every client gets an up-to-date snapshot.
     */
    public void notifyChanges() {
        this.market.notifyMarketChanged();
        this.board.notifyBoardChanged();
    }

    /**
     * Re-fires notifyCardAddedToTribe for every card in every player's tribe.
     * Used when resuming a saved game so all clients receive the full tribe state.
     */
    public void notifyAllPlayerTribes() {
        for (Player player : players.values()) {
            player.notifyCurrentTribe();
        }
    }

    /**
     * Pushes the current era, game phase, and active turn info to all observers.
     * Used when resuming a loaded game after all players have reconnected.
     */
    public void notifyCurrentState() {
        notifyEraChanged();
        notifyGamePhaseChanged();
        if (playerToPlace != null) notifyPlayerToPlaceChanged();
        if (playerToPlay != null) {
            notifyPlayerToPlayChanged();
            if (offertilePlayerIsOn != null) notifyActionChanged();
        }
    }

    /**
     * Notifies all observers of the current playing player's remaining draw actions.
     */
    private void notifyActionChanged() {
        notify(o -> o.actionOfferTileChanged(this.offertilePlayerIsOn.getActionAvailable().getDrawTop(), this.offertilePlayerIsOn.getActionAvailable().getDrawFromBottom()));
    }

    /**
     * Logs an informational server event with the game log prefix.
     *
     * @param message the message to log.
     */
    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }

    /**
     * Removes the given player from both the placing and playing turn queues immediately.
     * Called when a player disconnects so the game never waits for them.
     *
     * @param player the disconnected player.
     */
    public void removeFromTurnQueues(Player player) {
        turnManager.removePlayer(player);
    }

    // --- DISCONNECTION SUPPORT ---

    /**
     * Re-adds a reconnected player to the end of both turn queues.
     *
     * @param player the reconnected player.
     */
    public void reAddToTurnQueues(Player player) {
        turnManager.reAddPlayer(player);
    }

    /**
     * Advances the placing phase to the next connected player without placing a totem.
     * Used when the current placing player has disconnected mid-phase.
     *
     * @throws EndOfPlacingPhaseException if no more connected players need to place.
     */
    public void skipDisconnectedPlacingPlayer() throws EndOfPlacingPhaseException {
        this.playerToPlace = turnManager.getNextPlacingPlayer();
        notifyPlayerToPlaceChanged();
    }

    /**
     * Creates a memento capturing the entire current game state: era, game phase,
     * number of players, the current placing player, the current action player,
     * every player's state, the market state, and the board state.
     *
     * @return a {@link GameMemento} representing the complete game snapshot.
     */
    @Override
    public GameMemento createMemento() {
        UtilitiesFunction.logInfo(LOG_PREFIX, "Creating game memento (era=" + currentEra + ", phase=" + gamePhase + ")");
        return new GameMemento(this.currentEra, this.gamePhase, this.playerNumber, this.playerToPlace != null ? this.playerToPlace.getNickname() : null, this.playerToPlay != null ? this.playerToPlay.getNickname() : null, this.players.values().stream().map(Player::createMemento).toList(), this.market.createMemento(), this.board.createMemento());

    }

    /**
     * Restores the game from the serialised state stored in the provided memento.
     * Recreates players with their tribe and buildings, restores the market and board,
     * repositions players on default tiles, and realigns the turn queues.
     *
     * @param memento the {@link GameMemento} from which to restore the game state.
     */
    @Override
    public void restoreMemento(GameMemento memento) {
        UtilitiesFunction.logInfo(LOG_PREFIX, "Restoring game memento (era=" + memento.currentEra() + ", phase=" + memento.gamePhase() + ", players=" + memento.players().size() + ")");
        // 1. Restore game-level state
        this.currentEra = memento.currentEra();
        this.gamePhase = memento.gamePhase();
        // 2. Recreate players with tribe and buildings
        this.players.clear();
        BuildingFactory buildingFactory = new BuildingFactory();
        for (PlayerMemento pm : memento.players()) {
            Player p = new Player(pm.nickname(), pm.totemColor());
            p.restoreMemento(pm);
            List<BuildingCard> buildings = pm.buildingIDs().stream().map(id -> buildingFactory.createBuildingById(id, boardView)).toList();
            p.restoreBuildings(buildings);
            this.players.put(p.getNickname(), p);
        }
        // 3. Restore market
        this.market.restoreMemento(memento.market());
        // 4. Clear board tiles and place players on default tiles in saved order
        this.board.restoreMemento(memento.board());
        List<String> ordered = memento.board().orderedNicknamesOnDefaultTiles();
        for (int i = 0; i < ordered.size(); i++) {
            try {
                board.placePlayerOnDefaultTile(players.get(ordered.get(i)), i);
            } catch (TileOccupiedException e) {
                throw new RuntimeException("Error restoring board state", e);
            }
        }
        // 5. Sync turn manager with restored board order.
        //    Mirror the getNextPlacingPlayer() call in gameStart() so the queue is
        //    already advanced past the first placer — otherwise placePlayer() would
        //    return that same player a second time.
        turnManager.updatePlacingOrder();
        // 6. Restore current placing/playing player references
        if (memento.playerToPlaceNickname() != null) {
            this.playerToPlace = this.players.get(memento.playerToPlaceNickname());
            // Remove from queue so the next getNextPlacingPlayer() call correctly advances to the following player
            turnManager.getPlacingOrder().removeIf(p -> p.getNickname().equals(memento.playerToPlaceNickname()));
        }
        if (memento.playerToPlayNickname() != null) {
            this.playerToPlay = this.players.get(memento.playerToPlayNickname());
        }
    }

    /**
     * Functional interface for a market action that takes a player and a position and
     * may throw any of the checked exceptions raised by the market methods.
     */
    @FunctionalInterface
    private interface MarketAction {
        void execute(Player player, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException;
    }
}
