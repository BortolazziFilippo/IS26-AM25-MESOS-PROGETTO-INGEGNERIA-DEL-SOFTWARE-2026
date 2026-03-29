package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Board.BoardView;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Enums.ERA;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game implements GameView {
    private ERA currentEra = ERA.ERA_I;
    private  Board board;
    private  BoardView boardView;
    private  Market market;
    private  TurnManager turnManager;
    private  List<Player> players;
    private  Player playerHost;
    private  int playerNumber;
    //manca Gamestate, non so se vogliamo metterlo

    /**
     * default constructor of game, this method when called manage to create the Deck anc the building By launching the factories
     *
     * @param playerHost   player who created the game
     * @param playerNumber number of player
     */
    public Game(Player playerHost, int playerNumber) {
        this.playerNumber = playerNumber;
        this.board = new Board(this);
        this.boardView= board;
        this.market = new Market(this,board);
        this.turnManager = new TurnManager(board);
        this.playerHost = playerHost;
        this.players = new ArrayList<>();
        players.add(playerHost);
        //solita eccezione per gli argoementi passati
        if (playerHost == null) {
            throw new IllegalArgumentException("playerHost nullo");
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public Player checkWinner() {
        //questa è solo per completezza ma se il costruttore funziona non dovrebbe mai verificarsi
        if (this.players == null || this.players.isEmpty()) {
            throw new IllegalStateException("Nessun giocatore presente, errore nle costruttore");
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

    public Market getMarket() { return this.market; } // da aggiungere in UML

    //da aggiungere in UML volendo
    public Board getBoard(){
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
        } else {
            // You are already at the last ERA! Here you can throw an exception,
            // print a message, or perhaps trigger the end of the game.
            System.err.println("We are already at the last Era!");
        }
    }


}


