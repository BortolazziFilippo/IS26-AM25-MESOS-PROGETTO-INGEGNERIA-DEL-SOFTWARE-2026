package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Controller.Controller;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerNetworkHandler extends UnicastRemoteObject implements ServerRemoteInterface {
    private final List<ServerVirtualView> waitingPlayers = new ArrayList<>();
    private List<PlayerDTO> playerDTOS=new ArrayList<>();
    private Controller controller;
    private int requiredPlayers = 0;
    private boolean isGameStarted = false;
    public ServerNetworkHandler() throws RemoteException{
        super();
    }

    @Override
    public void createGame(PlayerDTO playerHost, int playerNumber,ClientRemoteInterface clientRemoteInterface) throws RemoteException, IllegalStateException {
        if (requiredPlayers > 0) {
            throw new IllegalStateException("Game already started");
        }

        this.requiredPlayers = playerNumber;

        // Creo la VirtualView per l'Host e gli associo il suo telecomando!
        ServerVirtualView hostView = new ServerVirtualView(clientRemoteInterface, playerHost.getNickName());
        waitingPlayers.add(hostView);
        playerDTOS.add(playerHost);

        System.out.println(playerHost.getNickName() + " Game created for " + playerNumber);
    }

    @Override
    public void addPlayer(PlayerDTO playerDTO, ClientRemoteInterface clientRemoteInterface) throws RemoteException, GameFullException,GameReadyToStartException,NameOrColorAlreadyTakenException{
        if (requiredPlayers == 0) {
            throw new GameFullException("Nessuna partita creata!");
        }
        if (isGameStarted) {
            throw new GameStartedException("Partita già in corso!");
        }
        if (playerDTOS.stream().anyMatch(player -> Objects.equals(player.getNickName(), playerDTO.getNickName()) || player.getColorTotem() == playerDTO.getColorTotem())) {
            throw new NameOrColorAlreadyTakenException("Errore: Nickname o colore del Totem già in uso!");
        }

        // Creo la VirtualView per il nuovo giocatore
        ServerVirtualView playerView = new ServerVirtualView(clientRemoteInterface, playerDTO.getNickName());
        waitingPlayers.add(playerView);
        playerDTOS.add(playerDTO);
        System.out.println(playerDTO.getNickName() + " si è unito! (" + waitingPlayers.size() + "/" + requiredPlayers + ")");

        // Se siamo tutti, avviamo il gioco!
        if (waitingPlayers.size() == requiredPlayers) {
            isGameStarted = true;
            setupAndStartGame();
        }
    }
    private void setupAndStartGame() {
        System.out.println("Game starting! Creating Controller and Game...");
        Controller controller = new Controller();

        // CRUCIAL SAVE: We save the controller so other RMI methods
        // (like selectCard, placingPlayer, etc.) can use it to perform actions!
        this.controller = controller;

        // 1. Extract the Host data (they are always at position 0)
        PlayerDTO hostDTO = playerDTOS.getFirst();
        ServerVirtualView hostView = waitingPlayers.getFirst();

        // 2. Create the Host player and initialize the game in the Model
        Player playerHost = new Player(hostDTO.getNickName(), hostDTO.getColorTotem(), hostView);
        controller.createGame(playerHost, requiredPlayers);

        // 3. Link ALL VirtualViews (including the Host!) to the Controller's global observers
        waitingPlayers.forEach(controller::linkObserver);

        // 4. Add the other players.
        // We use a classic "for" loop STARTING FROM 1, so we skip the Host (who is at index 0)
        for (int i = 1; i < playerDTOS.size(); i++) {
            PlayerDTO currentDTO = playerDTOS.get(i);
            ServerVirtualView currentView = waitingPlayers.get(i);

            // Create and add the new player by pairing their DTO with their View
            Player newPlayer = new Player(currentDTO.getNickName(), currentDTO.getColorTotem(), currentView);
            controller.addPlayer(newPlayer);
        }

        System.out.println("All players successfully added to the Model!");
        // ----------------------------------------------------------------
        // 5. MASS INITIAL SYNCHRONIZATION ON CLIENTS
        // ----------------------------------------------------------------
        System.out.println("Synchronizing initial state on clients...");
        for (ServerVirtualView view : waitingPlayers) {
            // Pass the complete list of PlayerDTOs to EVERY VirtualView
            view.forceInitialPlayersSync(this.playerDTOS);
        }
        System.out.println("All clients are synced. The game is ready!");
        controller.controllerGameStar();
    }

    @Override
    public void placingPlayer(PlayerDTO playerToPlace, int position) throws RemoteException, IndexOutOfBoundsException, TileOccupiedException {
        Player playerTemp=new Player(playerToPlace.getNickName(),playerToPlace.getColorTotem());
        controller.placingPlayer(playerTemp,position);
    }

    @Override
    public void selectCardFromTopList(PlayerDTO player, CARD_TYPE cardType, int position) throws RemoteException, IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException {
        Player playerTemp=new Player(player);
        controller.selectCardFromTopList(playerTemp,cardType,position);
    }

    @Override
    public void selectCardFromBottomList(PlayerDTO player, CARD_TYPE cardType, int position) throws IndexOutOfBoundsException, NotEnoughFoodException, NotSelectableCardException, EmptyMarketException, RemoteException {
        Player playerTemp=new Player(player);
        controller.selectCardFromBottomList(playerTemp,cardType,position);
    }

    @Override
    public void playerDoNothing(PlayerDTO playerDTO) throws RuntimeException, Exception {
        Player playerTemp=new Player(playerDTO);
        controller.playerDoNothing(playerTemp);
    }
}
