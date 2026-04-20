package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.*;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.TileOccupiedException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.rmi.RemoteException;
import java.util.Scanner;

public class ClientTUI {

    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final Scanner scanner;

    private PlayerDTO myPlayer = null;

    /**
     * Constructor: receives the network stubs from the ClientApp
     */
    public ClientTUI(ServerRemoteInterface serverStub, ClientVirtualView clientHandler) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the TUI loop. First the Lobby, then the Game.
     */
    public void start() {
        boolean inGame = false;

        // ==========================================================
        // 1. LOBBY LOOP
        // ==========================================================
        while (!inGame) {
            clearScreen();
            System.out.println("--- MENU PRINCIPALE ---");
            System.out.println("1 - Crea gioco");
            System.out.println("2 - Entra in una partita");
            System.out.print("Scelta: ");

            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    if (createGame()) {
                        waitForGameStart();
                        inGame = true;
                    } else {
                        System.out.println("\nPremi INVIO per continuare...");
                        scanner.nextLine();
                    }
                    break;
                case "2":
                    if (addPlayer()) {
                        waitForGameStart();
                        inGame = true;
                    } else {
                        System.out.println("\nPremi INVIO per continuare...");
                        scanner.nextLine();
                    }
                    break;
                default:
                    System.out.println("❌ Scelta non valida.");
                    System.out.println("\nPremi INVIO per continuare...");
                    scanner.nextLine();
                    break;
            }
        }

        // ==========================================================
        // 2. GAME LOOP
        // ==========================================================
        while (true) {
            // Pause the UI completely until the server wakes us up
            waitForMyTurn();

            // We are awake! It's our turn to do something.
            clearScreen();
            System.out.println("--- GIOCO (" + myPlayer.getNickName() + ") ---");
            System.out.println("Fase attuale: " + clientHandler.getGamePhase());
            System.out.println("\nScegli un'azione:");

            // Display dynamic menu
            switch (clientHandler.getGamePhase()) {
                case PLACING_PHASE, LAST_ROUND_PLACING_PHASE:
                    System.out.println("1 - Piazza Totem (Fase Piazzamento)");
                    break;
                case RESOLVE_ACTION, LAST_ROUND_RESOLVE_ACTION:
                    System.out.println("2 - Pesca carta da sopra (Fase Azioni)");
                    System.out.println("3 - Pesca carta da sotto (Fase Azioni)");
                    System.out.println("4 - Non fare nulla / Passa il turno");
                    break;
                default:
                    System.out.println("In attesa del caricamento...");
                    break;
            }

            System.out.print("Scegli: ");
            String mossa = scanner.nextLine();

            // Handle choice
            switch (mossa) {
                case "1":
                    if (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                        placePlayer();
                    } else {
                        System.err.println("❌ Azione non permessa: non siamo nella fase di piazzamento.");
                    }
                    break;

                case "2":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        drawTopCard();
                    } else {
                        System.err.println("❌ Azione non permessa in questa fase del gioco.");
                    }
                    break;

                case "3":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        drawBottomCard();
                    } else {
                        System.err.println("❌ Azione non permessa in questa fase del gioco.");
                    }
                    break;

                case "4":
                    if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                            clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                        passTurn();
                    } else {
                        System.err.println("❌ Azione non permessa in questa fase del gioco.");
                    }
                    break;

                default:
                    System.err.println("❌ Scelta non valida. Inserisci un numero tra quelli proposti.");
                    break;
            }

            System.out.println("\n(Premi INVIO per continuare...)");
            scanner.nextLine();
        }
    }


    // ==========================================================
    // --- GAME LOBBY METHODS ---
    // ==========================================================

    private boolean createGame() {
        clearScreen();
        System.out.println("--- CREAZIONE PARTITA ---");
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();
        COLOR colorTotem = bindColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);
        System.out.println();
        int playerNumber = numberOfPlayer();

        try {
            serverStub.createGame(player, playerNumber, clientHandler);
            System.out.println("\n✅ Partita creata con successo!");
            this.myPlayer = player;
            return true;
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore: comunicazione con il Server.");
        } catch (IllegalStateException e) {
            System.err.println("\n❌ Errore: lobby già presente.");
        }
        return false;
    }

    private boolean addPlayer() {
        clearScreen();
        System.out.println("--- AGGIUNTA GIOCATORE ---");
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();
        COLOR colorTotem = bindColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);

        try {
            serverStub.addPlayer(player, clientHandler);
            System.out.println("\n✅ Unito alla partita con successo!");
            this.myPlayer = player;
            return true;
        } catch (Exception e) {
            System.err.println("\n❌ Impossibile unirsi: " + e.getMessage());
        }
        return false;
    }

    private void waitForGameStart() {
        System.out.println("\n⏳ In attesa che si connettano gli altri giocatori...");
        synchronized (clientHandler.gameStartLock) {
            while (!clientHandler.isGameStarted) {
                try {
                    clientHandler.gameStartLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        clearScreen();
        System.out.println("🎉 GIOCO INIZIATO! 🎉");
        System.out.println("Tutti i giocatori sono connessi.");
        System.out.println("\nPremi INVIO per entrare nella plancia di gioco...");
        scanner.nextLine();
    }


    // ==========================================================
    // --- TURN MANAGEMENT ---
    // ==========================================================

    private void waitForMyTurn() {
        boolean wasWaiting = false;

        synchronized (clientHandler.turnLock) {
            while (!isMyTurn()) {
                wasWaiting = true;
                clearScreen();
                System.out.println("⏳ In attesa del tuo turno...");

                String currentPlayer = "";
                if (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                    currentPlayer = clientHandler.getPlayerToPlace();
                } else if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
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
            clearScreen();
            System.out.println("🔔 È IL TUO TURNO!");
            System.out.println("\nPremi INVIO per procedere...");
            scanner.nextLine();
        }
    }

    private boolean isMyTurn() {
        if (myPlayer == null) return false;

        if (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            return myPlayer.getNickName().equals(clientHandler.getPlayerToPlace());
        } else if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            return myPlayer.getNickName().equals(clientHandler.getPlayerToPlay());
        }
        return false;
    }


    // ==========================================================
    // --- ACTIONS ---
    // ==========================================================

    private void placePlayer() {
        System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
        boolean isPlaced = false;

        while (!isPlaced) {
            int position = getPlacingIndex();
            try {
                serverStub.placingPlayer(myPlayer, position);
                System.out.println("\n✅ Totem posizionato con successo nella casella " + (position + 1) + "!");
                isPlaced = true;
            } catch (TileOccupiedException e) {
                System.err.println("\n❌ Errore: La casella " + (position + 1) + " è già occupata. Scegline un'altra.");
            } catch (IndexOutOfBoundsException e) {
                System.err.println("\n❌ Errore: L'indice " + (position + 1) + " non è valido per questa partita. Riprova.");
            } catch (RemoteException e) {
                System.err.println("\n❌ Errore di rete: Impossibile comunicare con il Server.");
                break;
            }
        }
    }

    private void drawTopCard() {
        System.out.println("--- PESCA CARTA (SOPRA) ---");
        // TODO: Da implementare
    }

    private void drawBottomCard() {
        System.out.println("--- PESCA CARTA (SOTTO) ---");
        // TODO: Da implementare
    }

    private void passTurn() {
        System.out.println("--- PASSA TURNO ---");
        // TODO: Da implementare
    }


    // ==========================================================
    // --- HELPER METHODS ---
    // ==========================================================

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private int numberOfPlayer() {
        int numOfPlayers = 0;
        while (true) {
            System.out.print("Quanti giocatori (2-5)? ");
            try {
                numOfPlayers = Integer.parseInt(scanner.nextLine());
                if (numOfPlayers >= 2 && numOfPlayers <= 5) break;
                System.out.println("Errore: Il numero deve essere compreso tra 2 e 5.");
            } catch (NumberFormatException e) {
                System.out.println("Errore: Devi inserire un NUMERO.");
            }
        }
        return numOfPlayers;
    }

    private COLOR bindColor() {
        while (true) {
            System.out.println("\nScegli colore totem:");
            System.out.println("1-ROSSO\n2-BLUE\n3-YELLOW\n4-GREEN\n5-PURPLE");
            System.out.print("Scelta: ");
            switch (scanner.nextLine()) {
                case "1": return COLOR.RED;
                case "2": return COLOR.BLUE;
                case "3": return COLOR.YELLOW;
                case "4": return COLOR.GREEN;
                case "5": return COLOR.PURPLE;
                default: System.out.println("Input non valido");
            }
        }
    }

    private int getPlacingIndex() {
        int index = -1;
        while (true) {
            System.out.print("\n📍 Inserisci l'indice della casella in cui piazzare il Totem 1-" + clientHandler.getOfferTileSize() + ": ");
            String input = scanner.nextLine();
            try {
                index = Integer.parseInt(input);
                index -= 1; // Conversione da base 1 a base 0 per il Server

                if (index >= 0 && index < clientHandler.getOfferTileSize()) {
                    break;
                } else {
                    System.out.println("❌ Errore: L'indice deve essere tra 1 e " + clientHandler.getOfferTileSize() + ". Riprova.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Errore: Devi inserire un NUMERO intero valido. Riprova.");
            }
        }
        return index;
    }
}