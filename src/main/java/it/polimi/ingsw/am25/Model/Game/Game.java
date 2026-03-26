package it.polimi.ingsw.am25.Model.Game;

import it.polimi.ingsw.am25.Model.Board.Board;
import it.polimi.ingsw.am25.Model.Card.Card;
import it.polimi.ingsw.am25.Model.Player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game implements GameView {
    private final Board board;
    private final Market market;
    private final TurnManager turnManager;
    private final List<Player> players;
    private final Player playerHost;
    private final int playerNumber;
    /**
     * default constructor of game, this method when called manage to create the Deck anc the building By launching the factories
     *
     * @param playerHost player who created the game
     * @param playerNumber number of player
     */
    public Game(Player playerHost, int playerNumber) {
        this.board=new Board(this);
        this.market=new Market(this);
        this.turnManager=new TurnManager(board);
        this.playerHost=playerHost;
        this.playerNumber=playerNumber;
        this.players= new ArrayList<>();
        players.add(playerHost);
        //solita eccezione per gli argoementi passati
        if (playerHost == null) {
            throw new IllegalArgumentException("playerHost nullo");
        }
    }
    public void addPlayer(Player player){
        players.add(player);
    }

    public Player checkWinner(){
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
    @Override
    public int getPlayerNumber() {
        return this.playerNumber;
    }


    @Override
    public List<Player> getPlayerList() {
        return this.players;
    }
}


