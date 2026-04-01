package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.Action;
import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Board.BoardView;
import it.polimi.ingsw.am25.Model.Board.OfferTile;
import it.polimi.ingsw.am25.Model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.Model.Player.Player;
import it.polimi.ingsw.am25.Model.Utilities.Exception.*;
import it.polimi.ingsw.am25.Model.Utilities.UtilitiesConstant;
import it.polimi.ingsw.am25.Model.Utilities.UtilitiesFunction;

import java.util.ArrayList;
import java.util.List;

public class Game implements GameView {
    private ERA currentEra = ERA.ERA_I;
    private Board board;
    private BoardView boardView;
    private Market market;
    private TurnManager turnManager;
    private List<Player> players;
    private Player playerHost;
    private int playerNumber;
    private GAME_PHASE gamePhase;
    private Player playerToPlace;
    private Player playerToPlay;
    private OfferTile offertilePlayerIsOn;

    /**
     * default constructor of game, this method when called manage to create the Deck anc the building By launching the factories
     *
     * @param playerHost   player who created the game
     * @param playerNumber number of player
     */
    public Game(Player playerHost, int playerNumber) {
        this.playerNumber = playerNumber;
        this.gamePhase = GAME_PHASE.SETUP;
        this.board = new Board(this);
        this.boardView = board;
        this.market = new Market(this, board);
        this.turnManager = new TurnManager(board);
        this.playerHost = playerHost;
        this.players = new ArrayList<>();
        players.add(playerHost);
        //solita eccezione per gli argoementi passati
        if (playerHost == null) {
            throw new IllegalArgumentException("playerHost nullo");
        }
    }

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
            players.add(player);
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
        List<Integer> random = UtilitiesFunction.shuffledFromYToXExclusive(0, playerNumber);
        for (Player player : players) {
            board.placePlayerOnDefaultTile(player, random.getFirst());
            random.removeFirst();
        }
        turnManager.updatePlacingOrder();
        this.playerToPlace = turnManager.getCurrentPlacingPlayer();
        this.gamePhase = GAME_PHASE.PLACING_PHASE;
        //to add the observer notify
    }

    /**
     * this method try placing a player on the selected position, in case something goes wrong it throws exception (look in the methods called to see more)
     * if is detected that all the player are placed it set the game to the next phases
     *
     * @param player   player to place
     * @param position position to place the player
     * @throws IndexOutOfBoundsException postion not valid
     * @throws TileOccupiedException     tile already occupied
     */
    public void placePlayer(Player player, int position) throws IndexOutOfBoundsException, TileOccupiedException {
        try {
            board.placePlayerOnOffertile(player, position);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException();
        } catch (TileOccupiedException e) {
            throw new TileOccupiedException();
        }
        try {
            this.playerToPlace = turnManager.getCurrentPlacingPlayer();
        } catch (EndOfPlacingPhaseException e) {
            turnManager.updatePlayingOrder();
            this.playerToPlay = turnManager.getCurrentPlayingPlayer();
            this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(player);
            //in caso il player sia sulla casella A questo non ha azioni da svolgere
            checkPlayerOfferTile(player);
            this.gamePhase = GAME_PHASE.RESOLVE_ACTION;
        }
        //la board automaticamente aggiorna gli observer
        //devo aggiungere notifica observer game
    }

    /**
     * this method checks if a player is on the offertile with ID A, in case it adds the food
     * and goes to the next player.
     * only the first player can be on the offerTileA
     *
     * @param player player to check
     */
    private void checkPlayerOfferTile(Player player) {
        if (offertilePlayerIsOn.getOfferTileID() == 'A') {
            player.manageFoodAndPP(UtilitiesConstant.FOOD_OFFERTILE_A);
            this.playerToPlay = turnManager.getCurrentPlayingPlayer();
            this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(player);
        }
    }

    public Player checkWinner() {
        //questa è solo per completezza ma se il costruttore funziona non dovrebbe mai verificarsi
        if (this.players == null || this.players.isEmpty()) {
            throw new IllegalStateException("Nessun giocatore presente, errore nel costruttore");
        }

        //prende il primo player della lista, come se fosse una variabile tmp prima di fare il for
        Player winner = this.players.get(0);

        //scorre la lista e aggiorna man mano
        for (Player p : this.players) {
            if (p.getPrestigePoint() > winner.getPrestigePoint()) {
                winner = p;
            }
        }

        return winner;
    }

    /**
     * this method launches all the end round actions that has to be done
     * and set the gamePhase to the placing phase
     */
    //da aggiungere il caso venga rilevata una deckFinished, bisogna impostare gamePhase alla fine
    public void nextRoundIter() {
        board.returnOnDefaultTiles();
        players.forEach(Player::triggerEndRoundBuilding);
        market.endOfRoundMarketActions();
        this.gamePhase=GAME_PHASE.PLACING_PHASE;
    }

    public void endGameIter() {
        players.forEach(Player::triggerEndGameBuilding);
        //metodo calcolo punti in base a carte da aggiungere in player
    }

    /**
     * this method adds a card from the top list
     * @param toBuyCardType cardType to buy
     * @param position position of the card
     * @param player player buying the card
     *@throws IndexOutOfBoundsException in case the position is not valid
     *@throws NotSelectableCardException in case the player has not enough food
     */
    public void selectGenericCardTopLists(CARD_TYPE toBuyCardType, int position, Player player)throws IndexOutOfBoundsException, NotSelectableCardException,NotEnoughFoodException {
        switch (toBuyCardType) {
            case BUILDING -> market.buyBuildingTopList(position, player);
            case EVENT -> throw new NotSelectableCardException("cannot select an event");
            default -> market.selectCardFromTopList(position, player);
        }
        offertilePlayerIsOn.getActionAvailable().subtractOneTopAction();
        if(offertilePlayerIsOn.getActionAvailable().getDrawFromBottom()==0 && offertilePlayerIsOn.getActionAvailable().getDrawTop()==0){
            goNextPlayer();
        }
    }

    /**
     * this method adds a card from the top list
     * @param toBuyCardType cardType to buy
     * @param position position of the card
     * @param player player buying the card
     * @throws IndexOutOfBoundsException in case the position is not valid
     * @throws NotSelectableCardException in case the player has not enough food
     */
    public void selectGenericCardBottomLists(CARD_TYPE toBuyCardType, int position, Player player) throws IndexOutOfBoundsException, NotSelectableCardException,NotEnoughFoodException {
        switch (toBuyCardType) {
            case BUILDING -> market.buyBuildingBottomList(position, player);
            case EVENT -> throw new NotSelectableCardException("cannot select an event");
            default -> market.selectCardFromBottomList(position, player);
        }
        offertilePlayerIsOn.getActionAvailable().subtractOneBotAction();
        if(offertilePlayerIsOn.getActionAvailable().getDrawFromBottom()==0 && offertilePlayerIsOn.getActionAvailable().getDrawTop()==0){
            goNextPlayer();
        }
    }

    private void goNextPlayer() {
        try {
            this.playerToPlace = turnManager.getCurrentPlayingPlayer();
            this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(playerToPlace);
        } catch (EndOfPlayingPhaseException e) {
            nextRoundIter();
        }
    }

    public OfferTile getOffertilePlayerIsOn() {
        return offertilePlayerIsOn;
    }

    public Player getPlayerToPlace() {
        return playerToPlace;
    }

    public Player getPlayerToPlay() {
        return playerToPlay;
    }

    public Market getMarket() {
        return this.market;
    } // da aggiungere in UML

    //da aggiungere in UML volendo
    public Board getBoard() {
        return this.board;
    }

    @Override
    public int getPlayerNumber() {
        return this.playerNumber;
    }

    @Override
    public ERA getCurrentEra() {
        return this.currentEra;
    }

    @Override
    public List<Player> getPlayerList() {
        return this.players;
    }

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
    }


}


