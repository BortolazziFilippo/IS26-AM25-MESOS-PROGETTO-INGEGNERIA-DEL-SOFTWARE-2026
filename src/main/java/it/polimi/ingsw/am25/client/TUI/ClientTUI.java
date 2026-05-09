package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Main TUI orchestrator. Manages the lobby loop, the game loop,
 * and delegates all specific interactions to the appropriate sub-TUI classes:
 * <ul>
 *   <li>{@link LobbyTUI}     — create / join a game</li>
 *   <li>{@link PlacementTUI} — place the totem on the board</li>
 *   <li>{@link MarketTUI}    — draw cards, pass turn, extra-draw effect</li>
 *   <li>{@link TUIUtils}     — shared helpers (screen, input, colors)</li>
 * </ul>
 */
public class ClientTUI {

    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final Scanner scanner;
    private final TUIUtils utils;
    private PlayerDTO myPlayer = null;

    /**
     * Creates a new ClientTUI instance.
     * @param serverStub    the remote server interface.
     * @param clientHandler the client's virtual view.
     */
    public ClientTUI(ServerRemoteInterface serverStub, ClientVirtualView clientHandler) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.scanner = new Scanner(System.in);
        this.utils = new TUIUtils(scanner);
    }

    /**
     * Starts the TUI: runs the lobby loop until a game is joined, then enters
     * the main game loop.
     */
    public void start() {

        // ==========================================================
        // 1. LOBBY LOOP
        // ==========================================================
        LobbyTUI lobbyTUI = new LobbyTUI(serverStub, clientHandler, scanner, utils);

        while (myPlayer == null) {
            myPlayer = lobbyTUI.connect();
        }

        // ==========================================================
        // 2. PING THREAD
        // myPlayer is now known — start sending heartbeats every 3 s.
        // ==========================================================
        final PlayerDTO pingPlayer = myPlayer;
        Thread pingThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && !clientHandler.isServerDead()) {
                try {
                    serverStub.ping(pingPlayer);
                } catch (Exception e) {
                    // Server unreachable — wake up the TUI so it can exit cleanly.
                    clientHandler.handleServerDeath();
                    return;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        pingThread.setDaemon(true);
        pingThread.setName("heartbeat-ping");
        pingThread.start();

        // ==========================================================
        // 3. GAME LOOP
        // myPlayer is now known — build the game-phase helpers.
        // ==========================================================
        PlacementTUI    placementTUI    = new PlacementTUI(serverStub, clientHandler, scanner, utils, myPlayer);
        MarketTUI       marketTUI       = new MarketTUI(serverStub, clientHandler, scanner, utils, myPlayer);
        PlayerStatusTUI playerStatusTUI = new PlayerStatusTUI(clientHandler, scanner, utils, myPlayer);
        BoardTUI        boardTUI        = new BoardTUI(clientHandler, utils);

        while (true) {
            // Exit immediately if the server went away
            if (clientHandler.isServerDead()) {
                utils.clearScreen();
                System.err.println("Connessione al server persa. Il gioco si chiude.");
                return;
            }

            // Show a notification for every player that disconnected since last iteration
            List<String> disconnected = clientHandler.drainRecentDisconnections();
            if (!disconnected.isEmpty()) {
                utils.clearScreen();
                for (String dc : disconnected) {
                    System.out.println("⚠️  Il giocatore '" + dc + "' si è disconnesso dalla partita.");
                }
                utils.pauseAndClear();
            }

            // Recupera SOLVING_EVENTS perso: può accadere quando passTurn() esce
            // dal suo wait loop su gamePhaseChanged(SOLVING_EVENTS) e, prima che
            // il thread TUI arrivi a waitForMyTurn(), l'executor ha già consegnato
            // tutti gli eventResolved + gamePhaseChanged(PLACING_PHASE). In quel
            // caso lo switch qui sotto vedrebbe PLACING_PHASE e salterebbe
            // SOLVING_EVENTS. Gli eventResolved sono garantiti nella lista perché
            // l'executor li invia in ordine FIFO, tutti prima di PLACING_PHASE.
            if (!clientHandler.getResolvedEvents().isEmpty()) {
                new SolvingEventsTUI(clientHandler, utils).solveEvents();
                continue;
            }

            waitForMyTurn(playerStatusTUI, boardTUI);

            if (clientHandler.needsExtraDraw) {
                marketTUI.handleExtraDraw();
                continue;
            }

            utils.clearScreen();
            System.out.println("--- GIOCO (" + myPlayer.getNickName() + ") ---");
            System.out.println("Fase attuale: " + clientHandler.getGamePhase());
            System.out.println("\nScegli un'azione:");

            switch (clientHandler.getGamePhase()) {
                case PLACING_PHASE, LAST_ROUND_PLACING_PHASE:
                    System.out.println("[1] - Piazza Totem (Fase Piazzamento)");
                    System.out.println("[I] - Stato giocatori");
                    System.out.println("[B] - Visualizza board");
                    break;
                case RESOLVE_ACTION, LAST_ROUND_RESOLVE_ACTION:
                    System.out.println("Azioni disponibili:");
                    System.out.println("Pesca da sopra: " + clientHandler.getDrawTop());
                    System.out.println("Pesca da sotto: " + clientHandler.getDrawBot());
                    System.out.println("[2] - Pesca carta da sopra (Fase Azioni)");
                    System.out.println("[3] - Pesca carta da sotto (Fase Azioni)");
                    System.out.println("[4] - Passa il turno");
                    System.out.println("[I] - Stato giocatori");
                    System.out.println("[B] - Visualizza board");
                    marketTUI.printMarket();
                    break;
                case SOLVING_EVENTS:
                    new SolvingEventsTUI(clientHandler, utils).solveEvents();
                    continue; // salta il corpo del while e ricomincia da capo e invoca waitForMyTurns
                case END_GAME:
                    new EndGameTUI(clientHandler, utils, scanner, myPlayer, serverStub).finished(clientHandler.getWinners());
                    return;
                default:
                    System.out.println("In attesa del caricamento...");
                    break;
            }

            System.out.print("\nScelta: ");
            String move = scanner.nextLine().trim();

            if (move.equalsIgnoreCase("i")) {
                playerStatusTUI.printAllPlayersStatus();
                continue;
            }

            if (move.equalsIgnoreCase("b")) {
                utils.clearScreen();
                boardTUI.printBoard();
                utils.pauseAndClear();
                continue;
            }

            switch (move) {
                case "1":
                    if (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                        placementTUI.placePlayer();
                    } else {
                        System.err.println("\n❌ Azione non permessa: non siamo nella fase di piazzamento.");
                        utils.pauseAndClear();
                    }
                    break;
                case "2":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        marketTUI.drawTopCard();
                    } else {
                        System.err.println("\n❌ Azione non permessa in questa fase del gioco.");
                        utils.pauseAndClear();
                    }
                    break;
                case "3":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        marketTUI.drawBottomCard();
                    } else {
                        System.err.println("\n❌ Azione non permessa in questa fase del gioco.");
                        utils.pauseAndClear();
                    }
                    break;
                case "4":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        marketTUI.passTurn();
                    } else {
                        System.err.println("\n❌ Azione non permessa in questa fase del gioco.");
                        utils.pauseAndClear();
                    }
                    break;
                default:
                    System.err.println("\n❌ Scelta non valida. Inserisci un numero tra quelli proposti.");
                    utils.pauseAndClear();
                    break;
            }
        }
    }

    // ==========================================================
    // TURN MANAGEMENT
    // ==========================================================

    /**
     * Blocks the current thread until it is this player's turn.
     * Wakes every 300 ms to check for "i"/"b" input while waiting.
     */
    private void waitForMyTurn(PlayerStatusTUI playerStatusTUI, BoardTUI boardTUI) {
        if (isMyTurn()) return;

        printWaitingScreen();

        while (!isMyTurn()) {
            // Exit if the server went away while we were waiting
            if (clientHandler.isServerDead()) return;

            // Release the lock for up to 300 ms so the server can notify us.
            synchronized (clientHandler.turnLock) {
                if (!isMyTurn()) {
                    try {
                        clientHandler.turnLock.wait(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            if (isMyTurn()) break;

            // Show disconnection notices while waiting
            List<String> dcWhileWaiting = clientHandler.drainRecentDisconnections();
            if (!dcWhileWaiting.isEmpty()) {
                System.out.println();
                for (String dc : dcWhileWaiting) {
                    System.out.println("⚠️  Il giocatore '" + dc + "' si è disconnesso.");
                }
                if (!isMyTurn()) printWaitingScreen();
            }

            // Check for pending keyboard input without blocking.
            try {
                if (System.in.available() > 0) {
                    String input = scanner.nextLine().trim();
                    if (input.equalsIgnoreCase("i")) {
                        playerStatusTUI.printAllPlayersStatus();
                    } else if (input.equalsIgnoreCase("b")) {
                        utils.clearScreen();
                        boardTUI.printBoard();
                        utils.pauseAndClear();
                    }
                    if (!isMyTurn()) printWaitingScreen();
                }
            } catch (IOException e) {
                // ignore read errors on stdin
            }
        }

        utils.clearScreen();
        System.out.println("🔔 È IL TUO TURNO!");
        if (clientHandler.getGamePhase() != GAME_PHASE.SOLVING_EVENTS) {
            utils.pauseAndClear();
        }
    }

    /** Prints the static waiting screen with available commands. */
    private void printWaitingScreen() {
        utils.clearScreen();
        System.out.println("Sei in attesa del tuo turno...");
        System.out.println();

        String currentPlayer = "";
        GAME_PHASE phase = clientHandler.getGamePhase();
        if (phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            currentPlayer = clientHandler.getPlayerToPlace();
        } else if (phase == GAME_PHASE.RESOLVE_ACTION || phase == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            currentPlayer = clientHandler.getPlayerToPlay();
        }
        if (currentPlayer != null && !currentPlayer.isEmpty()) {
            System.out.println("Sta giocando: " + currentPlayer);
            System.out.println();
        }

        System.out.println("  [I] Informazioni giocatori");
        System.out.println("  [B] Informazioni board");
        System.out.print("\nScelta: ");
    }

    /**
     * Checks whether it is currently this player's turn.
     * @return {@code true} if the player should act now.
     */
    private boolean isMyTurn() {
        if (myPlayer == null) return false;
        if (clientHandler.needsExtraDraw) return true;

        GAME_PHASE phase = clientHandler.getGamePhase();
        if (phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            return myPlayer.getNickName().equals(clientHandler.getPlayerToPlace());
        } else if (phase == GAME_PHASE.RESOLVE_ACTION || phase == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            return myPlayer.getNickName().equals(clientHandler.getPlayerToPlay());
        } else if (phase == GAME_PHASE.SOLVING_EVENTS) {
            return true;
        }
        else if (phase == GAME_PHASE.END_GAME) {
            List<PlayerDTO> w = clientHandler.getWinners();
            return w != null && !w.isEmpty();
        }
        return false;
    }
}
