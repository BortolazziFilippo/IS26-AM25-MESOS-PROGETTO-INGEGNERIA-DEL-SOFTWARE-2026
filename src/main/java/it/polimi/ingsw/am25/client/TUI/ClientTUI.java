package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

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
            utils.clearScreen();
            System.out.println("--- MENU PRINCIPALE ---");
            System.out.println("1 - Crea gioco");
            System.out.println("2 - Entra in una partita");
            System.out.print("Scelta: ");

            switch (scanner.nextLine()) {
                case "1": myPlayer = lobbyTUI.createGame(); break;
                case "2": myPlayer = lobbyTUI.addPlayer();  break;
                default:
                    System.err.println("❌ Scelta non valida.");
                    utils.pauseAndClear();
            }
        }

        // ==========================================================
        // 2. GAME LOOP
        // myPlayer is now known — build the game-phase helpers.
        // ==========================================================
        PlacementTUI   placementTUI   = new PlacementTUI(serverStub, clientHandler, scanner, utils, myPlayer);
        MarketTUI      marketTUI      = new MarketTUI(serverStub, clientHandler, scanner, utils, myPlayer);
        PlayerStatusTUI playerStatusTUI = new PlayerStatusTUI(clientHandler, scanner, utils, myPlayer);

        while (true) {
            waitForMyTurn();

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
                    System.out.println("1 - Piazza Totem (Fase Piazzamento)");
                    break;
                case RESOLVE_ACTION, LAST_ROUND_RESOLVE_ACTION:
                    System.out.println("Azioni disponibili:");
                    System.out.println("Pesca da sopra: " + clientHandler.getDrawTop());
                    System.out.println("Pesca da sotto: " + clientHandler.getDrawBot());
                    System.out.println("2 - Pesca carta da sopra (Fase Azioni)");
                    System.out.println("3 - Pesca carta da sotto (Fase Azioni)");
                    System.out.println("4 - Passa il turno");
                    marketTUI.printMarket();
                    break;
                default:
                    System.out.println("In attesa del caricamento...");
                    break;
            }
            System.out.println("i - Stato giocatori");

            System.out.print("\nScelta: ");
            String mossa = scanner.nextLine();

            // "i" is available in any game phase
            if (mossa.equalsIgnoreCase("i")) {
                playerStatusTUI.printAllPlayersStatus();
                continue;
            }

            switch (mossa) {
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
     * Prints a waiting message while blocked and a notification when the turn arrives.
     */
    private void waitForMyTurn() {
        boolean wasWaiting = false;

        synchronized (clientHandler.turnLock) {
            while (!isMyTurn()) {
                wasWaiting = true;
                utils.clearScreen();
                System.out.println("⏳ In attesa del tuo turno...");

                String currentPlayer = "";
                GAME_PHASE phase = clientHandler.getGamePhase();
                if (phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                    currentPlayer = clientHandler.getPlayerToPlace();
                } else if (phase == GAME_PHASE.RESOLVE_ACTION || phase == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                    currentPlayer = clientHandler.getPlayerToPlay();
                }

                if (currentPlayer != null && !currentPlayer.isEmpty()) {
                    System.out.println("Attualmente sta giocando: " + currentPlayer);
                }

                try {
                    clientHandler.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (wasWaiting) {
            utils.clearScreen();
            System.out.println("🔔 È IL TUO TURNO!");
            utils.pauseAndClear();
        }
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
        }
        return false;
    }
}
