package it.polimi.ingsw.am25.server.model.Game;

import it.polimi.ingsw.am25.server.model.Board.Board;
import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Observers.GameObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesConstant;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.util.*;
import java.util.stream.Collectors;


public class Game implements GameView {
    private static final String LOG_PREFIX = "[SERVER][GAME]";
    private ERA currentEra = ERA.ERA_I;
    private Board board;
    private BoardView boardView;
    private Market market;
    private TurnManager turnManager;
    private Map<String,Player> players;
    private Player playerHost;
    private int playerNumber;
    private GAME_PHASE gamePhase;
    private Player playerToPlace;
    private Player playerToPlay;
    private OfferTile offertilePlayerIsOn;
    private final List<GameObserver> observers=new ArrayList<>();

    /**
     * default constructor of game, this method when called creates the deck and the buildings by launching the factories.
     *
     * @param playerHost   player who created the game
     * @param playerNumber number of players
     */
    public Game(Player playerHost, int playerNumber) {
        if (playerHost == null) {
            throw new IllegalArgumentException("playerHost nullo");
        }
        this.playerNumber = playerNumber;
        this.gamePhase = GAME_PHASE.SETUP;
        this.board = new Board(this);
        this.boardView = board;
        this.market = new Market(this, board);
        this.turnManager = new TurnManager(board);
        this.playerHost = playerHost;
        this.players = new HashMap<>();
        players.put(playerHost.getNickname(),playerHost);
        //standard null-guard for the constructor argument
        if (playerHost == null) {
            throw new IllegalArgumentException("playerHost is null");
        }
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
            players.put(player.getNickname(),player);
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
        for (Player player : players.values().stream().toList()) {
            try{
                board.placePlayerOnDefaultTile(player, random.get(0));
            } catch (TileOccupiedException e) {
                throw new RuntimeException(getClass()+" Errore gamestart placePlayer");
            }
            random.remove(0);
        }
        turnManager.updatePlacingOrder();
        try {
            this.playerToPlace = turnManager.getNextPlacingPlayer();
        } catch (EndOfPlacingPhaseException e) {
            throw new RuntimeException(getClass()+" Errore placing player in gamestart");
        }

        this.gamePhase = GAME_PHASE.PLACING_PHASE;
        notifyGamePhaseChanged();
        notifyPlayerToPlaceChanged();
    }

    /**
     * Places a player on an offer tile during the placing phase.
     * If all players have already placed, it signals the end of placing phase.
     *
     * @param player player to place
     * @param position target offer-tile index
     * @throws IndexOutOfBoundsException if the position is not valid
     * @throws TileOccupiedException if the target tile is already occupied
     */
    public void placePlayer(Player player, int position) throws IndexOutOfBoundsException, TileOccupiedException,EndOfPlacingPhaseException {
        Player player1=players.get(player.getNickname());
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
     * Transitions the game from the placing phase to the playing (resolve-action) phase.
     * Updates the playing order, sets the first player to play, and — if that player is
     * on offer tile 'A' (no actions, only food) — automatically advances to the next player.
     */
    public void advancePlayingPhase(){
        turnManager.updatePlayingOrder();
        try {
            this.playerToPlay = turnManager.getNextPlayingPlayer();
        }catch (EndOfPlayingPhaseException ex) {
            throw new RuntimeException(getClass()+" errore transizione placing->playing");
        }
        this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(playerToPlay);
        // If the player is on tile A, they have no actions and only gain food.
        // This check is needed only for the first playing player.
        checkPlayerOfferTile(playerToPlay);
        if(this.gamePhase==GAME_PHASE.LAST_ROUND_PLACING_PHASE){
            gamePhase=GAME_PHASE.LAST_ROUND_RESOLVE_ACTION;
        }else{
            this.gamePhase = GAME_PHASE.RESOLVE_ACTION;
        }

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
            }catch (EndOfPlayingPhaseException e) {
                throw new RuntimeException(getClass()+" errore controllo checkPlayerOffertile");
            }
            this.offertilePlayerIsOn = board.getCopyTilePlayerIsOn(playerToPlay);
        }
    }

    /**
     * Calculates the winner based on the prestige points and on the amount of food in the case of a tie
     * a single player if there is a clear winner by prestige points
     * a single player if prestige points are tied but one has more food
     * multiple players if both prestige points and food are equal. At the end it notifies all the players
     * who is/are the winner/s
     * @return
     * a list of Players
     *
     *
     */
    public List<Player> checkWinner() {
        //questa è solo per completezza ma se il costruttore funziona non dovrebbe mai verificarsi
        if (this.players == null || this.players.isEmpty()) {
            throw new IllegalStateException("Nessun giocatore presente, errore nel costruttore");
        }

        List<Player> winners = players.values().stream()
                .sorted(Comparator.comparing(Player::getPrestigePoint).thenComparing(Player :: getFood).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        Player topWinner = winners.get(0);

        List<Player> winningPlayers = new ArrayList<>();
        if(winners.get(0).getPrestigePoint() == winners.get(1).getPrestigePoint()){
            if(winners.get(0).getFood() == winners.get(1).getFood()){
                winningPlayers = winners.stream()
                        .filter(player -> player.getPrestigePoint() == topWinner.getPrestigePoint()
                        && player.getFood() == topWinner.getFood())
                        .collect(Collectors.toCollection(ArrayList::new));
            }
            else{
                winningPlayers.add(topWinner);
            }
        }
        else{
            winningPlayers.add(topWinner);
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
     * @throws EndGameException when the game is finished
     */
    public void nextRoundIter() throws EndGameException{
        if(this.gamePhase!=GAME_PHASE.LAST_ROUND_RESOLVE_ACTION){
            // If the deck is exhausted, there is one final round before end-game scoring.
            board.returnOnDefaultTiles();
            players.values().forEach(Player::triggerEndRoundBuilding);
            try {
                market.endOfRoundMarketActions();
                this.gamePhase=GAME_PHASE.PLACING_PHASE;
                logServerEvent("Round ended. Advancing to PLACING_PHASE");
                notifyGamePhaseChanged();
            }catch (DeckFinishedException e) {
                this.gamePhase=GAME_PHASE.LAST_ROUND_PLACING_PHASE;
                logServerEvent("Deck exhausted. Advancing to LAST_ROUND_PLACING_PHASE");
                notifyGamePhaseChanged();
            }
        }else{
            throw new EndGameException("Game finished");
        }

    }

    /**
     * applies the building effect if any of the players has at least one in their tribe
     * solves the final events and the other events if there are any other
     * manages the  prestige points by checking the tribe each of the players have
     */

    public void endGameIter() {
        gamePhase=GAME_PHASE.END_GAME;
        notifyGamePhaseChanged();
        players.values().forEach(Player::triggerEndGameBuilding);
        market.solveFinalEvents();
        for(Player p : players.values()){
            p.managePP(p.checkpoints());
        }
    }

    /**
     * this method adds a card from the top list
     * @param toBuyCardType cardType to buy
     * @param position position of the card
     * @param player player buying the card
     * @throws IndexOutOfBoundsException in case the position is not valid
     * @throws NotEnoughFoodException in case the player does not have enough food
     * @throws NotSelectableCardException if the player tries to select an event card
     * @throws NoMoreActionToDo if the player has no remaining actions after this selection
     */
    public void selectGenericCardTopLists(CARD_TYPE toBuyCardType, int position, Player player) throws IndexOutOfBoundsException, NotSelectableCardException, NotEnoughFoodException, EmptyMarketException,NoMoreActionToDo {
        Player player1=players.get(player.getNickname());
        switch (toBuyCardType) {
            case BUILDING -> market.buyBuildingTopList(position, player1);
            case EVENT -> throw new NotSelectableCardException("cannot select an event");
            default -> market.selectCardFromTopList(position, player1);
        }
        logServerEvent("Player '" + player1.getNickname() + "' selected a " + toBuyCardType + " card from top list at position " + position);
        offertilePlayerIsOn.getActionAvailable().subtractOneTopAction();
        if(offertilePlayerIsOn.getActionAvailable().getDrawFromBottom()==0 && offertilePlayerIsOn.getActionAvailable().getDrawTop()==0){
            throw new NoMoreActionToDo();
        }
    }

    /**
     * Checks if the current player has any legal moves available.
     * * @return false if the player cannot perform any action (e.g., market is empty or only contains events,
     * and the player has no other options). Returns true if the player has at least one valid action.
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
     * @param toBuyCardType cardType to buy
     * @param position position of the card in the bottom list
     * @param player player buying the card
     * @throws IndexOutOfBoundsException in case the position is not valid
     * @throws NotEnoughFoodException in case the player does not have enough food
     * @throws NotSelectableCardException if the player tries to select an event card
     */
    public void selectGenericCardBottomLists(CARD_TYPE toBuyCardType, int position, Player player) throws IndexOutOfBoundsException, NotSelectableCardException, NotEnoughFoodException, EmptyMarketException, NoMoreActionToDo {
        Player player1=players.get(player.getNickname());
        switch (toBuyCardType) {
            case BUILDING -> market.buyBuildingBottomList(position, player1);
            case EVENT -> throw new NotSelectableCardException("cannot select an event");
            default -> market.selectCardFromBottomList(position, player1);
        }
        logServerEvent("Player '" + player1.getNickname() + "' selected a " + toBuyCardType + " card from bottom list at position " + position);
        offertilePlayerIsOn.getActionAvailable().subtractOneBotAction();
        if(offertilePlayerIsOn.getActionAvailable().getDrawFromBottom()==0 && offertilePlayerIsOn.getActionAvailable().getDrawTop()==0){
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
    public void addObserver(GameObserver observerToAdd){
        if(observerToAdd!=null && !observers.contains(observerToAdd)){
            observers.add(observerToAdd);
        }
    }

    /**
     * Unsubscribes an observer from game-state changes.
     *
     * @param observerToRemove observer to unsubscribe
     */
    public void removeObserver(GameObserver observerToRemove){
        observers.remove(observerToRemove);
    }

    /**
     * Notifies all subscribed observers with a snapshot of the current game state.
     */
    private void notifyGameChanged() {
        List<Player> playersSnapshot = List.copyOf(this.players.values());

        for (GameObserver observer : List.copyOf(observers)) {
            observer.onGameChanged(
                    this.currentEra,
                    playersSnapshot,
                    this.gamePhase,
                    this.playerToPlace,
                    this.playerToPlay
            );
        }
    }

    /**
     * this method notifies the winners
     * @param winners list of winners
     */
    private void notifyWinners(List<Player> winners){
        for(GameObserver observer:List.copyOf(observers)){
            observer.gameWinners(
                    winners
            );
        }
    }

    /**
     * method to notify that  {@code player} has been added
     * @param player player to add
     */
    private void notifyPlayerAdded(Player player){
        for(GameObserver observer:observers){
            observer.onPlayerAdded(player);
        }
    }

    private void notifyEraChanged(){
        for(GameObserver observer:observers){
            observer.onEraChanged(this.currentEra);
        }
    }

    private void notifyGamePhaseChanged(){
        for(GameObserver observer:observers){
            observer.onGamePhaseChanged(gamePhase);
        }
    }

    private void notifyPlayerToPlaceChanged(){
        for(GameObserver observer:observers){
            observer.onPlayerToPlaceChanged(playerToPlace);
        }
    }

    private void notifyPlayerToPlayChanged(){
        for(GameObserver observer:observers){
            observer.onPlayerToPlayChanged(playerToPlay);
        }
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
        notifyEraChanged();
    }

    public void linkObserver(ServerVirtualView vv){
        this.addObserver(vv);
        board.addObserver(vv);
        market.addObserver(vv);
    }

    public void notifyChanges(){
        this.market.notifyMarketChanged();
        this.board.notifyBoardChanged();
    }

    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }

}
