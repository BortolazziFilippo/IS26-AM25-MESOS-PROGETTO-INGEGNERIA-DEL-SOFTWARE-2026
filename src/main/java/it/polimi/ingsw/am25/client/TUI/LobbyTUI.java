package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Handles all lobby-phase interactions: creating a game and joining one.
 * Returns a {@link PlayerDTO} on success, or {@code null} on failure.
 */
public class LobbyTUI {

    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final Scanner scanner;
    private final TUIUtils utils;

    /**
     * Creates a new LobbyTUI instance.
     * @param serverStub    the remote server interface.
     * @param clientHandler the client's virtual view.
     * @param scanner       the shared input scanner.
     * @param utils         the shared TUI utilities.
     */
    public LobbyTUI(ServerRemoteInterface serverStub, ClientVirtualView clientHandler,
                    Scanner scanner, TUIUtils utils) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.scanner = scanner;
        this.utils = utils;
    }

    /**
     * Guides the user through creating a new game.
     * Blocks until all players have connected.
     * @return the local {@link PlayerDTO}, or {@code null} if the operation failed.
     */
    public PlayerDTO createGame() {
        utils.clearScreen();
        System.out.println("--- CREAZIONE PARTITA ---");
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();
        COLOR colorTotem = utils.bindTotemColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);
        System.out.println();
        int playerNumber = utils.numberOfPlayer();

        clientHandler.connectionError = false;
        clientHandler.isGameStarted = false;

        try {
            serverStub.createGame(player, playerNumber, clientHandler);
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore: comunicazione con il Server fallita.");
            utils.pauseAndClear();
            return null;
        } catch (IllegalStateException e) {
            System.err.println("\n❌ Errore: lobby già presente.");
            utils.pauseAndClear();
            return null;
        }

        utils.clearScreen();
        System.out.println("\nRichiesta inviata. In attesa che si connettano gli altri giocatori...");

        synchronized (clientHandler.gameStartLock) {
            while (!clientHandler.isGameStarted && !clientHandler.connectionError) {
                try {
                    clientHandler.gameStartLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        if (clientHandler.connectionError) {
            clientHandler.connectionError = false;
            System.err.println("❌ Gioco già creato, unisciti tramite 'Aggiungi Giocatore'.");
            utils.pauseAndClear();
            return null;
        }

        System.out.println("\n✅ Tutti i giocatori sono connessi! La partita inizia!");
        utils.pauseAndClear();
        return player;
    }

    /**
     * Guides the user through joining an existing game.
     * Blocks until all players have connected.
     * @return the local {@link PlayerDTO}, or {@code null} if the operation failed.
     */
    public PlayerDTO addPlayer() {
        utils.clearScreen();
        System.out.println("--- AGGIUNTA GIOCATORE ---");
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();
        COLOR colorTotem = utils.bindTotemColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);

        clientHandler.connectionError = false;
        clientHandler.isGameStarted = false;

        try {
            serverStub.addPlayer(player, clientHandler);
        } catch (Exception e) {
            System.err.println("\n❌ Impossibile unirsi: " + utils.extractCleanError(e));
            utils.pauseAndClear();
            return null;
        }

        utils.clearScreen();
        System.out.println("\nRichiesta inviata. In attesa che si connettano gli altri giocatori...");

        synchronized (clientHandler.gameStartLock) {
            while (!clientHandler.isGameStarted && !clientHandler.connectionError) {
                try {
                    clientHandler.gameStartLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        if (clientHandler.connectionError) {
            clientHandler.connectionError = false;
            System.err.println("❌ Unione alla partita annullata.");
            utils.pauseAndClear();
            return null;
        }

        System.out.println("\n✅ Tutti i giocatori sono connessi! La partita inizia!");
        utils.pauseAndClear();
        return player;
    }
}
