package it.polimi.ingsw.am25.server.model.Controller;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Game.Game;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.util.List;

public class Controller {
    private Game game;
    private List<Player> players;


    public Controller() {
    }

    public void createGame(Player playerHost, int playerNumber) throws IllegalStateException{
        if(this.game==null){
            this.game=new Game(playerHost,playerNumber);
        }else {
            throw new IllegalStateException("Game already created");
        }

    }
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
    //TODO: add an exception for the case where a game is already in progress
    public void addPlayer(Player player) throws GameReadyToStartException {
        if (game.getGamePhase() == GAME_PHASE.SETUP) {
            game.addPlayer(player);
        }
    }

    public void controllerGameStar(){
        game.gameStart();
    }


    /**
     * Places the player on the tile specified by the position field.
     * The call is silently ignored if the game is not in a placing phase or if it is not
     * this player's turn to place.
     * When all players have placed their totems the game automatically advances to the
     * RESOLVE_ACTION phase.
     *
     * @param playerToPlace the player instance to be positioned on the board.
     * @param position the index of the target offer tile.
     * @throws IndexOutOfBoundsException if the provided position is outside the valid range of the board.
     * @throws TileOccupiedException if the target tile is already occupied by another player.
     */
    //TODO: build proper exceptions for this case
    public void placingPlayer(Player playerToPlace, int position) throws IndexOutOfBoundsException, TileOccupiedException {
        if (game.getGamePhase() == GAME_PHASE.PLACING_PHASE || game.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            if (checkIsPlayerPlacingTurn(playerToPlace)) {
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
        }
    }

    /**
     * Selects a card from the top list and adds it to the player.
     * The call is silently ignored if:
     * <ul>
     *   <li>the game is not in an action-resolution phase, or</li>
     *   <li>it is not this player's turn, or</li>
     *   <li>the player's current offer tile grants no top-list draws.</li>
     * </ul>
     * When the player exhausts all remaining actions, the turn automatically advances to the
     * next player. If no more players remain in this round, the round is advanced via
     * {@link Game#nextRoundIter()}.
     *
     * @param player the player who will receive the selected card.
     * @param cardType the type of card to be selected.
     * @param position the index of the card within the top list.
     * @throws IndexOutOfBoundsException if the position index is out of the list's range.
     * @throws NotEnoughFoodException if the player does not have sufficient food to acquire the card.
     * @throws NotSelectableCardException if the player attempts to select an Event card.
     * @throws EmptyMarketException if the top list contains no selectable cards.
     */
    //TODO: build proper exceptions for this case
    public void selectCardFromTopList(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        if (game.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || game.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            if (checkIsPlayerPlayingTurn(player)) {
                if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop() > 0) {
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
            }
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
     * Selects a card from the bottom list and adds it to the player.
     * The call is silently ignored if:
     * <ul>
     *   <li>the game is not in an action-resolution phase, or</li>
     *   <li>it is not this player's turn, or</li>
     *   <li>the player's current offer tile grants no bottom-list draws.</li>
     * </ul>
     * When the player exhausts all remaining actions, the turn automatically advances to the
     * next player. If no more players remain in this round, the round is advanced via
     * {@link Game#nextRoundIter()}.
     *
     * @param player the player who will receive the selected card.
     * @param cardType the type of card to be selected.
     * @param position the index of the card within the bottom list.
     * @throws IndexOutOfBoundsException if the position index is out of the list's range.
     * @throws NotEnoughFoodException if the player does not have sufficient food to acquire the card.
     * @throws NotSelectableCardException if the player attempts to select an Event card.
     * @throws EmptyMarketException if the bottom list contains no selectable cards.
     */
    public void selectCardFromBottomList(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        if (game.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || game.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            if (checkIsPlayerPlayingTurn(player)) {
                if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom() > 0) {
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
        if (game.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || game.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            if (checkIsPlayerPlayingTurn(player)) {
                if (game.canCurrentPlayingPlayerDoSomething()) {
                    throw new Exception("The player still has available moves and cannot skip their turn");
                }
                advanceTurnOrRound();
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
