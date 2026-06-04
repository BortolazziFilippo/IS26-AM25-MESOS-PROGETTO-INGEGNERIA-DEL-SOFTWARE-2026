package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.Utilities.ClientUtilitiesFunction;
import it.polimi.ingsw.am25.client.webLayer.PongWatchdog;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
     *
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
        // Wait for the game to start (gamePhase != null), then start
        // heartbeat and PongWatchdog in sync with the server.
        // ==========================================================
        synchronized (clientHandler.turnLock) {
            while (clientHandler.getGamePhase() == null && !clientHandler.isServerDead()) {
                try { clientHandler.turnLock.wait(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }

        final PlayerDTO pingPlayer = myPlayer;
        clientHandler.heartbeatActive = true;
        clientHandler.startPongWatchdog();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-ping");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            if (clientHandler.isServerDead()) {
                scheduler.shutdownNow();
                return;
            }
            try {
                serverStub.ping(pingPlayer);
            } catch (Exception e) {
                clientHandler.handleServerDeath();
                scheduler.shutdownNow();
            }
        }, 0, PongWatchdog.INTERVAL_S, TimeUnit.SECONDS);

        // ==========================================================
        // 3. GAME LOOP
        // myPlayer is now known — build the game-phase helpers.
        // ==========================================================
        PlacementTUI placementTUI = new PlacementTUI(serverStub, clientHandler, scanner, utils, myPlayer);
        MarketTUI marketTUI = new MarketTUI(serverStub, clientHandler, scanner, utils, myPlayer);
        PlayerStatusTUI playerStatusTUI = new PlayerStatusTUI(clientHandler, scanner, utils, myPlayer);
        BoardTUI boardTUI = new BoardTUI(clientHandler, utils);

        while (true) {
            // Exit immediately if the server went away
            if (clientHandler.isServerDead()) {
                utils.clearScreen();
                System.err.println("Connessione al server persa. Il gioco si chiude.");
                return;
            }

            // Show a notification for every player that disconnected/reconnected since last iteration
            List<String> disconnected = clientHandler.drainRecentDisconnections();
            List<String> reconnected = clientHandler.drainRecentReconnections();
            if (!disconnected.isEmpty() || !reconnected.isEmpty()) {
                utils.clearScreen();
                for (String dc : disconnected) {
                    System.out.println(TUIUtils.RED + dc + "' si è disconnesso dalla partita." + TUIUtils.RESET);
                }
                for (String rc : reconnected) {
                    System.out.println(TUIUtils.GREEN + rc + "' si è riconnesso alla partita."+TUIUtils.RESET);
                }
                utils.pauseAndClear();
            }

            // Recover a missed SOLVING_EVENTS phase: this can happen when passTurn() exits
            // its wait loop on gamePhaseChanged(SOLVING_EVENTS) and, before the TUI thread
            // reaches waitForMyTurn(), the executor has already delivered all eventResolved
            // notifications + gamePhaseChanged(PLACING_PHASE). In that case the switch below
            // would see PLACING_PHASE and skip SOLVING_EVENTS entirely.
            // The eventResolved list is guaranteed to be populated in FIFO order,
            // all entries arriving before PLACING_PHASE.
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
                    continue; // skip the rest of the loop body and re-enter from the top, invoking waitForMyTurn
                case END_GAME:
                    new EndGameTUI(clientHandler, utils, scanner, myPlayer, serverStub).finished(clientHandler.getWinners());
                    return;
                default:
                    System.out.println("In attesa del caricamento...");
                    break;
            }

            System.out.print("\nScelta: ");
            String move = scanner.nextLine().trim();

            if (clientHandler.isServerDead()) continue;

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
                        System.err.println( TUIUtils.RED + "\nAzione non permessa: non siamo nella fase di piazzamento."+TUIUtils.RESET);
                        utils.pauseAndClear();
                    }
                    break;
                case "2":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        marketTUI.drawTopCard();
                    } else {
                        System.err.println(TUIUtils.RED+ "\n Azione non permessa in questa fase del gioco."+TUIUtils.RESET);
                        utils.pauseAndClear();
                    }
                    break;
                case "3":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        marketTUI.drawBottomCard();
                    } else {
                        System.err.println(TUIUtils.RED+ "\nAzione non permessa in questa fase del gioco."+TUIUtils.RESET);
                        utils.pauseAndClear();
                    }
                    break;
                case "4":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        marketTUI.passTurn();
                    } else {
                        System.err.println(TUIUtils.RED+"\n Azione non permessa in questa fase del gioco."+TUIUtils.RESET);
                        utils.pauseAndClear();
                    }
                    break;
                default:
                    System.err.println(TUIUtils.RED+"\nScelta non valida. Inserisci un numero tra quelli proposti." + TUIUtils.RESET);
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

            // Show disconnection/reconnection notices while waiting
            List<String> dcWhileWaiting = clientHandler.drainRecentDisconnections();
            List<String> rcWhileWaiting = clientHandler.drainRecentReconnections();
            if (!dcWhileWaiting.isEmpty() || !rcWhileWaiting.isEmpty()) {
                utils.clearScreen();
                for (String dc : dcWhileWaiting) {
                    System.out.println(TUIUtils.RED+" Il giocatore '" + dc + "' si è disconnesso."+TUIUtils.RESET);
                }
                for (String rc : rcWhileWaiting) {
                    System.out.println(TUIUtils.GREEN+"Il giocatore '" + rc + "' si è riconnesso."+TUIUtils.RESET);
                }
                if (!isMyTurn()) {
                    utils.pauseAndClear();
                    printWaitingScreen();
                }
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
        System.out.println(TUIUtils.YELLOW+" È IL TUO TURNO!"+TUIUtils.RESET);
        if (clientHandler.getGamePhase() != GAME_PHASE.SOLVING_EVENTS) {
            utils.pauseAndClear();
        }
    }

    /**
     * Prints the static waiting screen with available commands.
     */
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
     *
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
        } else if (phase == GAME_PHASE.END_GAME) {
            List<PlayerDTO> w = clientHandler.getWinners();
            return w != null && !w.isEmpty();
        }
        return false;
    }
}
