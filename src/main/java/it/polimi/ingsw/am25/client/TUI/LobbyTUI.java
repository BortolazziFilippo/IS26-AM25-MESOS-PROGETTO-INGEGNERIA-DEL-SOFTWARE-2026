package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameFullException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles all lobby-phase interactions.
 * Automatically determines whether to create a new game or join an existing one,
 * based on the server state, without requiring the user to choose.
 */
public class LobbyTUI {

    /**
     * Message sent by the server when no game lobby exists yet.
     */
    private static final String NO_LOBBY_MESSAGE = "Nessuna partita creata!";

    private final static String LOGO = """
            $$\\      $$\\ $$$$$$$$\\  $$$$$$\\   $$$$$$\\   $$$$$$\\ \s
            $$$\\    $$$ |$$  _____|$$  __$$\\ $$  __$$\\ $$  __$$\\\s
            $$$$\\  $$$$ |$$ |      $$ /  \\__|$$ /  $$ |$$ /  \\__|
            $$\\$$\\$$ $$ |$$$$$\\    \\$$$$$$\\  $$ |  $$ |\\$$$$$$\\ \s
            $$ \\$$$  $$ |$$  __|    \\____$$\\ $$ |  $$ | \\____$$\\\s
            $$ |\\$  /$$ |$$ |      $$\\   $$ |$$ |  $$ |$$\\   $$ |
            $$ | \\_/ $$ |$$$$$$$$\\ \\$$$$$$  | $$$$$$  |\\$$$$$$  |
            \\__|     \\__|\\________| \\______/  \\______/  \\______/\s
            
            """;
    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final Scanner scanner;
    private final TUIUtils utils;

    /**
     * Creates a new LobbyTUI instance.
     *
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
     * Connects the player to a game.
     * If a game is already in startup phase the player is automatically added to it.
     * If no game is in startup phase the player creates a new one and chooses the number of players.
     * Blocks until the game starts.
     *
     * @return the local {@link PlayerDTO}, or {@code null} if the thread was interrupted.
     */
    public PlayerDTO connect() {
        while (true) {
            utils.clearScreen();
            System.out.println(LOGO);
            System.out.println("[J] - Crea/Unisciti a una partita");
            System.out.println("[C] - Visualizza classifica");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.equals("C")) {
                showLeaderboard();
                continue;
            } else if (!choice.equals("J")) {
                continue;
            }

            utils.clearScreen();
            System.out.println(LOGO);
            System.out.println("--- CONNESSIONE AL GIOCO ---");
            System.out.println("(Q per tornare al menu)");
            System.out.print("Inserisci nome giocatore: ");
            String nickname = scanner.nextLine().trim();
            if (nickname.equalsIgnoreCase("q")) continue;
            if (nickname.isEmpty()) {
                System.err.println("\n❌ Il nickname non può essere vuoto.");
                utils.pauseAndClear();
                continue;
            }
            COLOR colorTotem = utils.bindTotemColor();
            PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);

            clientHandler.connectionError = false;
            clientHandler.isGameStarted = false;
            clientHandler.lastErrorMessage = null;

            // Try to join a game already in startup phase.
            boolean noLobbyExists = false;
            try {
                serverStub.addPlayer(player, clientHandler);
                // RMI: no exception means the request was accepted.
                // Socket: message sent; outcome arrives asynchronously below.
            } catch (GameFullException e) {
                // No lobby currently open — we must create one.
                noLobbyExists = true;
            } catch (Exception e) {
                System.err.println("\n❌ " + utils.extractCleanError(e));
                utils.pauseAndClear();
                continue;
            }

            if (!noLobbyExists) {
                // Wait for the game to start or for an error response (socket path).
                utils.clearScreen();
                System.out.println("Richiesta inviata. In attesa che si connettano gli altri giocatori...");
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
                    if (NO_LOBBY_MESSAGE.equals(clientHandler.lastErrorMessage)) {
                        // Socket path: server confirmed no lobby exists.
                        noLobbyExists = true;
                    } else {
                        System.err.println("\n❌ " + (clientHandler.lastErrorMessage != null
                                ? clientHandler.lastErrorMessage : "Errore sconosciuto"));
                        utils.pauseAndClear();
                        continue;
                    }
                } else {
                    System.out.println("\n✅ Tutti i giocatori connessi! La partita inizia!");
                    return player;
                }
            }

            // No lobby found — create a new game.
            utils.clearScreen();
            System.out.println("Nessuna partita in attesa. Sei il primo giocatore!");
            int playerNumber = utils.numberOfPlayer();

            clientHandler.connectionError = false;
            clientHandler.isGameStarted = false;

            try {
                serverStub.createGame(player, playerNumber, clientHandler);
            } catch (RemoteException e) {
                System.err.println("\n❌ Errore di comunicazione col server.");
                utils.pauseAndClear();
                continue;
            } catch (IllegalStateException e) {
                // Another player created a lobby between our check and this call; retry.
                System.err.println("\n❌ Una lobby è appena stata creata da un altro giocatore. Riprova.");
                utils.pauseAndClear();
                continue;
            }

            utils.clearScreen();
            System.out.println("Partita creata! In attesa degli altri giocatori...");

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
                System.err.println("\n❌ Errore durante la creazione della partita.");
                utils.pauseAndClear();
                continue;
            }

            System.out.println("\n✅ Tutti i giocatori connessi! La partita inizia!");
            return player;
        }
    }

    private void showLeaderboard() {
        utils.clearScreen();
        System.out.println(LOGO);
        System.out.println("--- CLASSIFICA ---");
        System.out.print("Numero giocatori (2-5 o \"all\"), Q per uscire: ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.equalsIgnoreCase("q")) return;

        String serverParam;
        boolean showAll;
        if (input.equals("all")) {
            serverParam = "5";
            showAll = true;
        } else {
            try {
                int n = Integer.parseInt(input);
                if (n < 2 || n > 5) {
                    System.err.println("\n❌ Inserisci un numero tra 2 e 5.");
                    utils.pauseAndClear();
                    return;
                }
                serverParam = input;
                showAll = false;
            } catch (NumberFormatException e) {
                System.err.println("\n❌ Input non valido.");
                utils.pauseAndClear();
                return;
            }
        }

        clientHandler.clearLeaderboards();
        clientHandler.connectionError = false;
        clientHandler.lastErrorMessage = null;
        try {
            serverStub.askForRank(serverParam, clientHandler);
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore di comunicazione col server.");
            utils.pauseAndClear();
            return;
        }

        synchronized (clientHandler.turnLock) {
            while (clientHandler.getLeaderboards() == null && !clientHandler.connectionError) {
                try {
                    clientHandler.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (clientHandler.connectionError) {
            System.err.println("\n❌ " + (clientHandler.lastErrorMessage != null
                    ? clientHandler.lastErrorMessage : "Errore nel recupero della classifica."));
            utils.pauseAndClear();
            return;
        }

        Map<Integer, List<String>> leaderboards = clientHandler.getLeaderboards();
        utils.clearScreen();
        System.out.println(LOGO);
        System.out.println("--- CLASSIFICA ---\n");

        if (showAll) {
            for (int i = 2; i <= 5; i++) {
                printLeaderboardSection(leaderboards, i);
            }
        } else {
            printLeaderboardSection(leaderboards, Integer.parseInt(serverParam));
        }

        utils.pauseAndClear();
    }


    private void printLeaderboardSection(Map<Integer, List<String>> leaderboards, int playerCount) {
        System.out.println("  Partite da " + playerCount + " giocatori:");
        List<String> entries = leaderboards.get(playerCount);
        if (entries == null || entries.isEmpty()) {
            System.out.println("    Nessun dato disponibile.");
        } else {
            entries.forEach(e -> System.out.println("    " + e));
        }
        System.out.println();
    }
}
