package it.polimi.ingsw.am25.server.model.Controller;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Game.Game;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.util.List;

public class Controller {
    private Game game;
    private List<Player> players;
    private static final String LOG_PREFIX = "[SERVER][CONTROLLER]";

    /**
     * Creates the MVC controller for a Mesos game session.
     * The {@link Game} instance is not created here — call {@link #createGame} first.
     */
    public Controller() {
    }

    /**
     * Initializes the {@link Game} model with the host player and the required player count.
     * Must be called exactly once before any other game logic.
     *
     * @param playerHost   the host player, added as the first participant.
     * @param playerNumber the total number of players required to start the game (2–5).
     * @throws IllegalStateException if a game has already been created on this controller.
     */
    public void createGame(Player playerHost, int playerNumber) throws IllegalStateException{
        if(this.game==null){
            this.game=new Game(playerHost,playerNumber);
        }else {
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
    public void linkObserver(ServerVirtualView virtualView){
        game.linkObserver(virtualView);
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
                game.gameStart();
                game.notifyChanges();
            }
        }
    }

    /**
     * Starts the Mesos game and pushes the initial state snapshot to all connected clients.
     * Must be called after all players have joined and all observers have been linked.
     */
    public void controllerGameStar(){
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
        if (game.getGamePhase() != GAME_PHASE.PLACING_PHASE && game.getGamePhase() != GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            throw new ActionNotAvailable("Cannot place a totem outside of the placing phase");
        }
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
     * @throws ActionNotAvailable        if the game is not in a resolve-action phase, it is not this player's turn,
     *                                   or their offer tile grants no top-row draws.
     * @throws IndexOutOfBoundsException if {@code position} is out of range.
     * @throws NotEnoughFoodException    if the player lacks the food to buy a building.
     * @throws NotSelectableCardException if the card at that position is an event card.
     * @throws EmptyMarketException      if the top row has no selectable cards.
     */
    public void selectCardFromTopList(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        if (game.getGamePhase() != GAME_PHASE.RESOLVE_ACTION && game.getGamePhase() != GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            throw new ActionNotAvailable("Cannot select a card outside of the resolve-action phase");
        }
        if (!checkIsPlayerPlayingTurn(player)) {
            throw new ActionNotAvailable("It is not " + player.getNickname() + "'s turn to play");
        }
        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop() <= 0) {
            UtilitiesFunction.logError(LOG_PREFIX, player.getNickname() + " tried to draw a card from top list but has no action for it");
            throw new ActionNotAvailable("Cannot draw top card: no top-list draws remaining on this offer tile");
        }
        try {
            game.selectGenericCardTopLists(cardType, position, player);
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
            } catch (EndGameException ex) {
                game.endGameIter();
                game.checkWinner();
            }

        }
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
     * @throws ActionNotAvailable        if the game is not in a resolve-action phase, it is not this player's turn,
     *                                   or their offer tile grants no bottom-row draws.
     * @throws IndexOutOfBoundsException if {@code position} is out of range.
     * @throws NotEnoughFoodException    if the player lacks the food to buy a building.
     * @throws NotSelectableCardException if the card at that position is an event card.
     * @throws EmptyMarketException      if the bottom row has no selectable cards.
     */
    public void selectCardFromBottomList(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        if (game.getGamePhase() != GAME_PHASE.RESOLVE_ACTION && game.getGamePhase() != GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            throw new ActionNotAvailable("Cannot select a card outside of the resolve-action phase");
        }
        if (!checkIsPlayerPlayingTurn(player)) {
            throw new ActionNotAvailable("It is not " + player.getNickname() + "'s turn to play");
        }
        if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom() <= 0) {
            UtilitiesFunction.logError(LOG_PREFIX, player.getNickname() + " tried to draw a card from bottom list but has no action for it");
            throw new ActionNotAvailable("Cannot draw bottom card: no bottom-list draws remaining on this offer tile");
        }
        try {
            game.selectGenericCardBottomLists(cardType, position, player);
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
        if (game.getGamePhase() != GAME_PHASE.RESOLVE_ACTION && game.getGamePhase() != GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            throw new ActionNotAvailable("Cannot skip a turn outside of the resolve-action phase");
        }
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
     * from the top market row and adds it to the player's tribe, then wakes the
     * {@link ServerVirtualView} thread that was blocking on {@code extraDrawLock} waiting
     * for the player's response.
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
        // Actually perform the draw from top cards
        game.selectGenericCardTopLists(cardType, position, player);
        // WAKE THE SERVER: find the ServerVirtualView among the player observers
        for (PlayerObserver obs : game.getSpecificPlayer(player).getObservers()) {
            if (obs instanceof ServerVirtualView svv) {
                synchronized (svv.extraDrawLock) {
                    svv.extraDrawLock.notifyAll();
                }
                break;
            }
        }
    }

    /**
     * Returns {@code true} if it is the given player's turn to place their totem.
     *
     * @param player the player to check
     * @return whether it is this player's placing turn
     */
    private boolean checkIsPlayerPlacingTurn(Player player) {
        return game.getPlayerToPlace().equals(player);
    }

    /**
     * Returns {@code true} if it is the given player's turn to resolve their actions.
     *
     * @param player the player to check
     * @return whether it is this player's playing turn
     */
    private boolean checkIsPlayerPlayingTurn(Player player) {
        return game.getPlayerToPlay().equals(player);
    }

}
