package it.polimi.ingsw.am25.Model.Controller;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.Model.Game.Game;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.*;

public class Controller{
    private Game game;


    public Controller(Player playerHost, int playerNumber) {
        this.game = new Game(playerHost, playerNumber);
    }

    /**
     * this method allows to add a player to the game
     * in case the player added fulls the lobby it starts the game
     *
     * @param player player to add
     */
    //da aggiungere eccezione partita gia in corso
    public void addPlayer(Player player) {
        if (game.getGamePhase() == GAME_PHASE.SETUP) {
            try {
                game.addPlayer(player);
            } catch (GameReadyToStartException e) {
                game.gameStart();
            }
        }
    }
    /**
     * Places the player on the tile specified by the position field.
     *
     * @param playerToPlace the player instance to be positioned on the board.
     * @param position the index or coordinate of the target tile.
     * @throws IndexOutOfBoundsException if the provided position is outside the valid range of the board.
     * @throws TileOccupiedException if the target tile is already occupied by another player.
     */
    //da costruire eccezioni giuste per il caso
    public void placingPlayer(Player playerToPlace, int position) throws IndexOutOfBoundsException, TileOccupiedException {
        if (game.getGamePhase() == GAME_PHASE.PLACING_PHASE || game.getGamePhase()==GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            if (checkIsPlayerPlacingTurn(playerToPlace)) {
                try {
                    game.placePlayer(playerToPlace, position);
                } catch (IndexOutOfBoundsException e) {
                    throw new IndexOutOfBoundsException("Indice non valido");
                } catch (TileOccupiedException e) {
                    throw new TileOccupiedException("Tile non valida");
                }catch(EndOfPlacingPhaseException e){
                    game.advancePlayingPhase();
                }
            }
        }
    }

    /**
     * Selects a card from the top list and adds it to the player.
     *
     * @param player the player who will receive the selected card.
     * @param cardType the type of card to be selected.
     * @param position the index of the card within the top list.
     * @throws IndexOutOfBoundsException if the position index is out of the list's range.
     * @throws NotEnoughFoodException if the player does not have sufficient food resources to acquire the card.
     * @throws NotSelectableCardException if the player attempts to select an Event card, which cannot be picked.
     */
    //da costruire eccezioni giuste
    public void selectCardFromTopList(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        if (game.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || game.getGamePhase()==GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            if (checkIsPlayerPlayingTurn(player)) {
                if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop() > 0) {
                    try {
                        game.selectGenericCardTopLists(cardType, position, player);
                    } catch (IndexOutOfBoundsException e) {
                        throw new IndexOutOfBoundsException("Indice non valido");
                    } catch (NotEnoughFoodException e) {
                        throw new NotEnoughFoodException("Non ha abbastanza cibo");
                    } catch (NotSelectableCardException e) {
                        throw new NotSelectableCardException("Non puoi selezionare un evento");
                    } catch (EmptyMarketException e) {
                        throw new EmptyMarketException(); //da capire come gestire questo metodo
                    }catch (NoMoreActionToDo e){
                        try {
                            game.goNextPlayer();
                        }catch (EndOfPlayingPhaseException ex) {
                            game.nextRoundIter();
                        }

                    }
                }
            }
        }
    }
    /**
     * Selects a card from the bottom list and adds it to the player.
     *
     * @param player the player who will receive the selected card.
     * @param cardType the type of card to be selected.
     * @param position the index of the card within the bottom list.
     * @throws IndexOutOfBoundsException if the position index is out of the list's range.
     * @throws NotEnoughFoodException if the player does not have sufficient food resources to acquire the card.
     * @throws NotSelectableCardException if the player attempts to select an Event card, which cannot be picked.
     */
    public void selectCardFromBottomList(Player player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        if (game.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || game.getGamePhase()==GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            if (checkIsPlayerPlayingTurn(player)) {
                if (game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom() > 0) {
                    try {
                        game.selectGenericCardBottomLists(cardType, position, player);
                    } catch (IndexOutOfBoundsException e) {
                        throw new IndexOutOfBoundsException("Indice non valido");
                    } catch (NotEnoughFoodException e) {
                        throw new NotEnoughFoodException("Non ha abbastanza cibo");
                    } catch (NotSelectableCardException e) {
                        throw new NotSelectableCardException("Non puoi selezionare un evento");
                    }catch(EmptyMarketException e){
                        throw new EmptyMarketException();
                    }catch (NoMoreActionToDo e){
                        try {
                            game.goNextPlayer();
                        }catch (EndOfPlayingPhaseException ex) {
                            game.nextRoundIter();
                        }
                    }
                }
            }
        }
    }

    private boolean checkIsPlayerPlacingTurn(Player player) {
        return game.getPlayerToPlace().equals(player);
    }
    private boolean checkIsPlayerPlayingTurn(Player player){
        return game.getPlayerToPlay().equals(player);
    }

}
