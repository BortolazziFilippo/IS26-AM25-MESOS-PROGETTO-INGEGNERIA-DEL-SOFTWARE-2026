package it.polimi.ingsw.am25.Model.Controller;

import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.Model.Game.Game;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.GameReadyToStartException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.NotEnoughFoodException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.NotSelectableCardException;
import it.polimi.ingsw.am25.Model.Utilities.Exception.TileOccupiedException;

public class Controller {
    private Game game;


    public Controller(Player playerHost, int playerNumber) {
        this.game = new Game(playerHost, playerNumber);
    }

    public void addPlayer(Player player) {
        if (game.getGamePhase() == GAME_PHASE.SETUP) {
            try {
                game.addPlayer(player);
            } catch (GameReadyToStartException e) {
                game.gameStart();
            }
        }
    }

    //da costruire eccezioni giuste per il caso
    public void placingPlayer(Player playerToPlace, int position) throws IndexOutOfBoundsException,TileOccupiedException {
        if (game.getGamePhase() == GAME_PHASE.PLACING_PHASE) {
            if (checkIsPlayerPlacingTurn(playerToPlace)) {
                try {
                    game.placePlayer(playerToPlace, position);
                } catch (IndexOutOfBoundsException e) {
                    throw new IndexOutOfBoundsException("Indice non valido");
                }catch (TileOccupiedException e) {
                    throw new TileOccupiedException("Tile non valida");
                }
            }
        }
    }
    //da costruire eccezioni giuste
    public void selectCardFromTopList(Player player, CARD_TYPE cardType,int position) throws IndexOutOfBoundsException,NotEnoughFoodException,NotSelectableCardException{
        if(game.getGamePhase()==GAME_PHASE.RESOLVE_ACTION){
            if(game.getPlayerToPlay().equals(player)){
                if(game.getOffertilePlayerIsOn().getActionAvailable().getDrawTop()>0){
                    try {
                        game.selectGenericCardTopLists(cardType,position,player);
                    }catch (IndexOutOfBoundsException e){
                        throw new IndexOutOfBoundsException("Indice non valido");
                    }catch (NotEnoughFoodException e){
                        throw new NotEnoughFoodException("Non ha abbastanza cibo");
                    }catch (NotSelectableCardException e){
                        throw new NotSelectableCardException("Non puoi selezionare un evento");
                    }
                }
            }
        }
    }
    public void selectCardFromBottomList(Player player, CARD_TYPE cardType,int position) throws IndexOutOfBoundsException,NotEnoughFoodException,NotSelectableCardException{
        if(game.getGamePhase()==GAME_PHASE.RESOLVE_ACTION){
            if(game.getPlayerToPlay().equals(player)){
                if(game.getOffertilePlayerIsOn().getActionAvailable().getDrawFromBottom()>0){
                    try {
                        game.selectGenericCardBottomLists(cardType,position,player);
                    }catch (IndexOutOfBoundsException e){
                        throw new IndexOutOfBoundsException("Indice non valido");
                    }catch (NotEnoughFoodException e){
                        throw new NotEnoughFoodException("Non ha abbastanza cibo");
                    }catch (NotSelectableCardException e){
                        throw new NotSelectableCardException("Non puoi selezionare un evento");
                    }
                }
            }
        }
    }

private boolean checkIsPlayerPlacingTurn(Player player) {
    return game.getPlayerToPlace().equals(player);
}
}
