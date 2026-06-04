package it.polimi.ingsw.am25.server.model.Controller;

import it.polimi.ingsw.am25.server.model.DBmanager.DBManager;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.CONNECTION_STATUS;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Game.Game;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.model.persistance.GameMemento;
import it.polimi.ingsw.am25.server.model.persistance.PersistanceLogger;
import it.polimi.ingsw.am25.server.model.persistance.PlayerMemento;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;

/**
 * MVC controller for a Mesos game session. Validates player actions against the current
 * game phase and turn, delegates them to the {@link Game} model, and handles all phase
 * transitions (placing → resolve-action → next round → end game).
 */
public class Controller {
    private static final String LOG_PREFIX = "[SERVER][CONTROLLER]";
    private final List<Player> players;
    PersistanceLogger persistanceLogger = new PersistanceLogger();
    private Game game;

    /**
     * Creates the MVC controller for a Mesos game session.
     * The {@link Game} instance is not created here — call {@link #createGame} first.
     */
    public Controller() {
        this.players = new ArrayList<>();
    }

    /**
     * Initializes the {@link Game} model with the host player and the required player count.
     * Must be called exactly once before any other game logic.
     *
     * @param playerHost   the host player, added as the first participant.
     * @param playerNumber the total number of players required to start the game (2–5).
     * @throws IllegalStateException if a game has already been created on this controller.
     */
    public void createGame(Player playerHost, int playerNumber) throws IllegalStateException {
        if (this.game == null) {
            this.game = new Game(playerHost, playerNumber);
        } else {
            throw new IllegalStateException("Game already created");
        }

    }

    /**
     * Registers a {@link ServerVirtualView} as an observer of the game model,
     * so the client associated with that view receives all game-event notifications
     * (phase changes, market updates, board updates, etc.).
     *
     * @param virtualView the virtual view to register.
     */
    public void linkObserver(ServerVirtualView virtualView) {
        game.linkObserver(virtualView);
    }

    /**
     * Returns all players currently in the game.
     *
     * @return unmodifiable list of {@link Player} instances.
     */
    public List<Player> getAllPlayers() {
        return game.getPlayerList();
    }

    /**
     * Cross-registers every provided {@link ServerVirtualView} as a
     * {@link it.polimi.ingsw.am25.server.model.Observers.PlayerObserver} on every player.
     *
     * <p>By default each {@link Player} only notifies its own view when its tribe
     * changes. Calling this method after all players have been created ensures that
     * <em>every</em> client receives the {@code addedCardToTribe} notification
     * whenever <em>any</em> player draws a card, so all clients can display an
     * up-to-date view of the other players' tribes.
     *
     * @param views the list of all connected {@link ServerVirtualView} instances.
     */
    public void crossRegisterPlayerObservers(List<ServerVirtualView> views) {
        for (Player player : game.getPlayerList()) {
            for (ServerVirtualView view : views) {
                player.addObserver(view); // addObserver ignores duplicates
            }
        }
    }

    /**
     * Adds a player to the game lobby.
     * If the lobby is already full (game not in SETUP phase) the call is silently ignored.
     * When the added player fills the lobby, the game starts automatically and transitions
     * to the PLACING_PHASE.
     *
     * @param player player to add
     */
    public void addPlayer(Player player) {
        if (game.getGamePhase() == GAME_PHASE.SETUP) {
            try {
                game.addPlayer(player);
            } catch (GameReadyToStartException e) {
                // Game start is handled by controllerGameStar() after observers and
                // initial sync are set up — calling gameStart() here would fire food
                // notifications before crossRegisterPlayerObservers and
                // forceInitialPlayersSync run, so the updates would be lost.
            }
        }
    }

    /**
     * Starts the Mesos game and pushes the initial state snapshot to all connected clients.
     * Must be called after all players have joined and all observers have been linked.
     */
    public void controllerGameStart() {
        Thread DBthread = new Thread(() -> {
            try {
                DBManager.getConnection();
            } catch (IOException e) {
                UtilitiesFunction.logError(LOG_PREFIX + "IOexception DB");
            } catch (SQLException e) {
                UtilitiesFunction.logError(LOG_PREFIX + "Errore comunicazione server");
            }

        });
        DBthread.setName("DB Thread");
        DBthread.start();
        game.gameStart();
        game.notifyChanges();
    }


    /**
     * Places the player's totem on the offer tile at the given board position
     * during the placing phase. The chosen tile determines how many draws from
     * the top and bottom market rows the player will have in the resolve-action phase.
     * When all players have placed their totems the game automatically advances to the
     * RESOLVE_ACTION phase.
     *
     * @param playerToPlace the player placing their totem.
     * @param position      the index of the target offer tile on the board.
     * @throws ActionNotAvailable        if the game is not in a placing phase or it is not this player's turn to place.
     * @throws TileOccupiedException     if another player's totem is already on that tile.
     * @throws IndexOutOfBoundsException if {@code position} is out of range.
     */
    public void placingPlayer(Player playerToPlace, int position) throws IndexOutOfBoundsException, TileOccupiedException {
        requirePlacingPhase();
        if (!checkIsPlayerPlacingTurn(playerToPlace)) {
            throw new ActionNotAvailable("It is not " + playerToPlace.getNickname() + "'s turn to place");
        }
        try {
            game.placePlayer(playerToPlace, position);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Invalid index");
        } catch (TileOccupiedException e) {
            throw new TileOccupiedException("Invalid tile");
        } catch (EndOfPlacingPhaseException e) {
            game.advancePlayingPhase();
        }
    }

    /**
     * Lets the player pick a tribe member or building from the current-round (top) market row.
     * When the player exhausts all remaining actions, the turn automatically advances to the
     * next player. If no more players remain in this round, the round is advanced via
     * {@link Game#nextRoundIter()}.
     *
     * @param player   the player who will receive the selected card.
     * @param cardType the type of card to be selected (tribe member or {@link CARD_TYPE#BUILDING}).
     * @param position the index of the card within the top row.
     * @throws ActionNotAvailable         if the game is not in a resolve-action phase, it is not this player's turn,
     *                                    or their offer tile grants no top-row draws.
     * @throws IndexOutOfBoundsException  if {@code position} is out of range.
     * @throws NotEnoughFoodException     if the player lacks the food to buy a building.
     * @throws NotSelectableCardException if the card at that position is an event card.
     * @throws EmptyMarketException       if the top row has no selectable cards.
     */
    public void selectCardFromTopList(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        selectCardFromList(player, cardType, position, () -> game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop(), "top", game::selectGenericCardTopLists);
    }

    /**
     * Lets the player pick a tribe member or building from the previous-round (bottom) market row.
     * When the player exhausts all remaining actions, the turn automatically advances to the
     * next player. If no more players remain in this round, the round is advanced via
     * {@link Game#nextRoundIter()}.
     *
     * @param player   the player who will receive the selected card.
     * @param cardType the type of card to be selected (tribe member or {@link CARD_TYPE#BUILDING}).
     * @param position the index of the card within the bottom row.
     * @throws ActionNotAvailable         if the game is not in a resolve-action phase, it is not this player's turn,
     *                                    or their offer tile grants no bottom-row draws.
     * @throws IndexOutOfBoundsException  if {@code position} is out of range.
     * @throws NotEnoughFoodException     if the player lacks the food to buy a building.
     * @throws NotSelectableCardException if the card at that position is an event card.
     * @throws EmptyMarketException       if the bottom row has no selectable cards.
     */
    public void selectCardFromBottomList(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        selectCardFromList(player, cardType, position, () -> game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom(), "bottom", game::selectGenericCardBottomLists);
    }

    /**
     * Shared implementation for top- and bottom-row card selection.
     * Validates the game phase, the acting player's turn, and the number of remaining draws
     * for the requested row. Then delegates to the supplied {@code gameAction}, re-throws any
     * market exception with a clean message, and advances the turn when all actions are used up.
     *
     * <p>This method is intentionally {@code private}: callers should use the public
     * {@link #selectCardFromTopList} and {@link #selectCardFromBottomList} entry points,
     * which bind the correct action-count supplier and game action.
     *
     * @param player         the player selecting a card.
     * @param cardType       the type of card to select.
     * @param position       zero-based index of the card within the market row.
     * @param getActionCount supplies the number of remaining draws for this row
     *                       (e.g. {@code getDrawTop()} or {@code getDrawFromBottom()}).
     * @param rowLabel       human-readable row label ({@code "top"} or {@code "bottom"})
     *                       used in log and exception messages.
     * @param gameAction     the game-model method to invoke
     *                       ({@link Game#selectGenericCardTopLists} or
     *                       {@link Game#selectGenericCardBottomLists}).
     * @throws ActionNotAvailable         if the phase is wrong, it is not this player's turn,
     *                                    or the row has no remaining draws.
     * @throws IndexOutOfBoundsException  if {@code position} is out of range.
     * @throws NotEnoughFoodException     if the player lacks food to buy a building.
     * @throws NotSelectableCardException if the selected card is an event card.
     * @throws EmptyMarketException       if the row has no selectable cards.
     */
    private void selectCardFromList(Player player, CARD_TYPE cardType, int position, IntSupplier getActionCount, String rowLabel, GameSelectAction gameAction) {
        requireResolveActionPhase();
        if (!checkIsPlayerPlayingTurn(player)) {
            throw new ActionNotAvailable("It is not " + player.getNickname() + "'s turn to play");
        }
        if (getActionCount.getAsInt() <= 0) {
            UtilitiesFunction.logError(LOG_PREFIX, player.getNickname() + " tried to draw a card from " + rowLabel + " list but has no action for it");
            throw new ActionNotAvailable("Cannot draw " + rowLabel + " card: no " + rowLabel + "-list draws remaining on this offer tile");
        }
        try {
            gameAction.execute(cardType, position, player);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Invalid index");
        } catch (NotEnoughFoodException e) {
            throw new NotEnoughFoodException("Not enough food");
        } catch (NotSelectableCardException e) {
            throw new NotSelectableCardException("Cannot select an event card");
        } catch (EmptyMarketException e) {
            throw new EmptyMarketException();
        } catch (NoMoreActionToDo e) {
            advanceTurnOrRound();
        }
    }

    /**
     * Advances the turn to the next playing player.
     * If there are no more players in the current round, the round is advanced via
     * {@link Game#nextRoundIter()}.
     */
    private void advanceTurnOrRound() {
        try {
            game.goNextPlayingPlayer();
        } catch (EndOfPlayingPhaseException e) {
            try {
                game.nextRoundIter();
                persistanceLogger.save(game.createMemento());
            } catch (EndGameException ex) {
                game.endGameIter();
                game.checkWinner();
                persistanceLogger.deleteFile();
            }

        }
    }

    /**
     * Handles the action of a player attempting to pass or skip their turn.
     * This method can only be executed during the action resolution phases
     * (RESOLVE_ACTION or LAST_ROUND_RESOLVE_ACTION) and only if it is the given player's turn.
     * <p>
     * A player is strictly allowed to "do nothing" ONLY IF they have no legal moves
     * left to play. If the player attempts to skip while they still have available
     * actions on the board/market, the move is rejected and an exception is thrown.
     * If the skip is valid, the turn automatically passes to the next player, or
     * advances the game to the next round if the current playing phase is over.
     *
     * @param player The player attempting to skip their action phase.
     * @throws Exception If the player attempts to pass but still has playable actions available.
     */
    public void playerDoNothing(Player player) throws Exception {
        requireResolveActionPhase();
        if (!checkIsPlayerPlayingTurn(player)) {
            throw new ActionNotAvailable("It is not " + player.getNickname() + "'s turn to play");
        }
        if (game.canCurrentPlayingPlayerDoSomething()) {
            throw new Exception("The player still has available moves and cannot skip their turn");
        }
        advanceTurnOrRound();
    }

    /**
     * Resolves the bonus draw granted by the draw-one-more mechanic: picks the chosen card
     * from the top market row and adds it to the player's tribe.
     *
     * @param player   the player claiming the extra card.
     * @param cardType the type of card being selected (tribe member or {@link CARD_TYPE#BUILDING}).
     * @param position the index of the card in the top market row.
     * @throws NotEnoughFoodException     if the player lacks the food to buy a building.
     * @throws NotSelectableCardException if the selected card is an event card.
     * @throws EmptyMarketException       if the top row has no selectable cards.
     * @throws IndexOutOfBoundsException  if {@code position} is out of range.
     */
    public void selectExtraCard(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        game.selectExtraCardFromTopList(cardType, position, player);
    }

    /**
     * Called when the player declines the extra draw granted by the draw-one-more building effect.
     * This is intentionally a no-op: {@code askExtraDraw} is sent fire-and-forget by the server,
     * which does not block waiting for the player's response. The game has already advanced by the
     * time this method is called, so no further action is needed.
     *
     * @param player the player declining the extra draw.
     */
    public void skipExtraDraw(Player player) {
    }

    /**
     * Returns {@code true} if it is the given player's turn to place their totem.
     *
     * @param player the player to check
     * @return whether it is this player's placing turn
     */
    private boolean checkIsPlayerPlacingTurn(Player player) {
        return game.getPlayerToPlace().getNickname().equals(player.getNickname());
    }

    /**
     * Returns {@code true} if it is the given player's turn to resolve their actions.
     *
     * @param player the player to check
     * @return whether it is this player's playing turn
     */
    private boolean checkIsPlayerPlayingTurn(Player player) {
        return game.getPlayerToPlay().getNickname().equals(player.getNickname());
    }

    /**
     * Throws {@link ActionNotAvailable} if the game is not currently in a placing phase
     * ({@link GAME_PHASE#PLACING_PHASE} or {@link GAME_PHASE#LAST_ROUND_PLACING_PHASE}).
     *
     * @throws ActionNotAvailable if the current phase is not a placing phase.
     */
    private void requirePlacingPhase() {
        if (game.getGamePhase() != GAME_PHASE.PLACING_PHASE && game.getGamePhase() != GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            throw new ActionNotAvailable("Cannot place a totem outside of the placing phase");
        }
    }

    /**
     * Throws {@link ActionNotAvailable} if the game is not currently in a resolve-action phase
     * ({@link GAME_PHASE#RESOLVE_ACTION} or {@link GAME_PHASE#LAST_ROUND_RESOLVE_ACTION}).
     *
     * @throws ActionNotAvailable if the current phase is not a resolve-action phase.
     */
    private void requireResolveActionPhase() {
        if (game.getGamePhase() != GAME_PHASE.RESOLVE_ACTION && game.getGamePhase() != GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            throw new ActionNotAvailable("Cannot perform this action outside of the resolve-action phase");
        }
    }

    /**
     * Looks up a player in the game by nickname.
     *
     * @param nickname the nickname to search for.
     * @return the matching {@link Player}, or {@code null} if not found.
     */
    private Player findPlayerByNickname(String nickname) {
        return game.getPlayerList().stream().filter(p -> p.getNickname().equals(nickname)).findFirst().orElse(null);
    }

    /**
     * Handles a player disconnection at the game-logic level.
     * <ol>
     *   <li>Marks the player as {@link CONNECTION_STATUS#DISCONNECTED} in the model.</li>
     *   <li>Removes them from the placing/playing turn queues.</li>
     *   <li>Ends the game immediately if only one (or zero) connected players remain.</li>
     *   <li>If it was the disconnected player's turn, automatically advances to the next player.</li>
     * </ol>
     *
     * @param nickname the nickname of the disconnected player.
     */
    public synchronized void notifyPlayerDisconnected(String nickname) {
        if (game == null) return;

        // 1. Find and mark the player as disconnected
        Player disconnectedPlayer = findPlayerByNickname(nickname);
        if (disconnectedPlayer == null) return;
        disconnectedPlayer.setConnection(CONNECTION_STATUS.DISCONNECTED);

        // 2. Remove from turn queues so the game never waits for them
        game.removeFromTurnQueues(disconnectedPlayer);

        // 3. Count remaining connected players
        long connectedCount = game.getPlayerList().stream().filter(p -> p.getConnection() != CONNECTION_STATUS.DISCONNECTED).count();
        if (connectedCount <= 1) {
            if (game.getGamePhase() != GAME_PHASE.END_GAME) {
                forceEndGame();
            }
            return;
        }

        // 4. If it was this player's turn, advance the game
        GAME_PHASE phase = game.getGamePhase();
        if (phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            Player toPlace = game.getPlayerToPlace();
            if (toPlace != null && toPlace.getNickname().equals(nickname)) {
                try {
                    game.skipDisconnectedPlacingPlayer();
                } catch (EndOfPlacingPhaseException e) {
                    game.advancePlayingPhase();
                }
            }
        } else if (phase == GAME_PHASE.RESOLVE_ACTION || phase == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            Player toPlay = game.getPlayerToPlay();
            if (toPlay != null && toPlay.getNickname().equals(nickname)) {
                advanceTurnOrRound();
            }
        }
    }

    // --- DISCONNECTION ---

    /**
     * Returns {@code true} if the game has reached the {@link GAME_PHASE#END_GAME} phase.
     *
     * @return {@code true} if the game is over, {@code false} otherwise.
     */
    public boolean isGameOver() {
        return game != null && game.getGamePhase() == GAME_PHASE.END_GAME;
    }

    /**
     * Forces the game to end immediately, running end-game scoring and notifying all clients.
     * Called when too few connected players remain to continue.
     */
    public void forceEndGame() {
        if (game == null) return;
        game.endGameIter();
        game.checkWinner();
    }

    /**
     * Marks a reconnected player as connected again and re-adds them to the end of
     * the turn queues so they participate from the next turn/round.
     *
     * @param nickname the nickname of the reconnected player.
     */
    public synchronized void notifyPlayerReconnected(String nickname) {
        if (game == null) return;
        Player player = findPlayerByNickname(nickname);
        if (player == null) return;
        player.setConnection(CONNECTION_STATUS.CONNECTED);
        game.reAddToTurnQueues(player);
    }

    /**
     * Loads a saved game from disk and restores the game state.
     * Players are recreated from the persisted memento; the requesting player is marked
     * as connected while all others are marked as disconnected, waiting to reconnect.
     *
     * @param player the player requesting the game load.
     * @throws IllegalStateException      if the game has already been initialised, or if the
     *                                    player's nickname is not present in the saved game.
     * @throws GameAlreadyLoadedException if an active game instance already exists.
     * @throws NoGameToLoadException      if no save file exists.
     */
    public synchronized void loadGame(Player player) throws IllegalStateException, GameAlreadyLoadedException, NoGameToLoadException {
        if (game != null) {
            throw new GameAlreadyLoadedException("Game already initialized");
        }
        boolean playerWasPlaying = false;
        Optional<GameMemento> gameMemento = persistanceLogger.load();
        if (gameMemento.isPresent()) {
            for (PlayerMemento playerDTO : gameMemento.get().players()) {
                Player p = new Player(playerDTO.nickname(), playerDTO.totemColor());
                players.add(p);
                if (p.getNickname().equals(player.getNickname())) {
                    p.setConnection(CONNECTION_STATUS.CONNECTED);
                    playerWasPlaying = true;
                } else {
                    p.setConnection(CONNECTION_STATUS.DISCONNECTED);
                }
            }
            if (!playerWasPlaying) {
                throw new IllegalStateException("Nickname not found");
            }
            this.game = new Game(gameMemento.get().playerNumber());
            game.restoreMemento(gameMemento.get());
        } else {
            throw new NoGameToLoadException("No game to load");
        }
    }

    /**
     * Registers the reconnection of a player to a loaded game, marking them as connected.
     * If all players are connected again, throws {@link GameReadyToStartException}
     * to signal that the game can resume.
     *
     * @param player the player who is reconnecting.
     * @throws IllegalStateException     if the game has not been loaded yet.
     * @throws GameReadyToStartException if all players are now connected and the game is ready to resume.
     */
    public synchronized void reconnectLoadedPlayer(Player player) throws IllegalStateException, GameReadyToStartException {
        if (game == null) {
            throw new IllegalStateException("Game not loaded");
        }
        Player playerToAdd = this.players.stream().filter(p -> p.getNickname().equals(player.getNickname())).findFirst().orElse(null);
        if (playerToAdd == null) {
            throw new IllegalStateException("Nickname not found");
        }
        if (playerToAdd.getConnection() == CONNECTION_STATUS.CONNECTED) {
            throw new IllegalStateException("Player already connected");
        }
        playerToAdd.setConnection(CONNECTION_STATUS.CONNECTED);
        if (players.stream().allMatch(p -> p.getConnection() == CONNECTION_STATUS.CONNECTED)) {
            throw new GameReadyToStartException("Game ready to start");
        }
    }

    /**
     * Pushes the full game state (market, board, era, phase, current turn) to all
     * registered observers. Called when resuming a loaded game after all players
     * have reconnected, so every client gets an up-to-date snapshot.
     */
    public void resumeGame() {
        game.notifyChanges();
        game.notifyAllPlayerTribes();
        game.notifyCurrentState();
    }

    /**
     * Functional interface for a game-level market selection action (top or bottom row).
     * Implementations wrap either {@link Game#selectGenericCardTopLists} or
     * {@link Game#selectGenericCardBottomLists}.
     */
    @FunctionalInterface
    private interface GameSelectAction {
        /**
         * Executes a market card selection on the game model.
         *
         * @param cardType the type of card to select.
         * @param position zero-based index within the market row.
         * @param player   the player performing the selection.
         */
        void execute(CARD_TYPE cardType, int position, Player player);
    }

}
