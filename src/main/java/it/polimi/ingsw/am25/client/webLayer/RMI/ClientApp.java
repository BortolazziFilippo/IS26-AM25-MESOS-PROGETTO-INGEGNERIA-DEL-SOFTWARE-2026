package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.*;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import static it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE.PLACING_PHASE;

public class ClientApp {

    // local player's nickname to easily check if it's our turn
    private static PlayerDTO myPlayer= null;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServerRemoteInterface serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");
            ClientVirtualView clientHandler = new ClientVirtualView();
            Scanner scanner = new Scanner(System.in);

            boolean inGame = false;

            // --- LOBBY LOOP ---
            while (!inGame) {
                clearScreen();
                System.out.println("--- MENU PRINCIPALE ---");
                System.out.println("1 - Crea gioco");
                System.out.println("2 - Entra in una partita");
                System.out.print("Scelta: ");

                String scelta = scanner.nextLine();

                switch (scelta) {
                    case "1":
                        if (createGame(serverStub, clientHandler)) {
                            waitForGameStart(clientHandler);
                            inGame = true;
                        } else {
                            System.out.println("\nPremi INVIO per continuare...");
                            scanner.nextLine();
                        }
                        break;
                    case "2":
                        if (addPlayer(serverStub, clientHandler)) {
                            waitForGameStart(clientHandler);
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
            // ALRIGHT, THE GAME IS ON!
            // ==========================================================
            while (true) {

                // Pause the UI completely until the server wakes us up
                waitForMyTurn(clientHandler);

                // We are awake! It's our turn to do something.
                clearScreen();
                System.out.println("--- GIOCO (" + myPlayer.getNickName() + ") ---");
                System.out.println("Fase attuale: " + clientHandler.getGamePhase());
                System.out.println("\nScegli un'azione:");

                // 1. Display the dynamic menu based on the current phase
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

                // 2. Handle the user's choice, double-checking the game phase for security
                //TODO:FINIRE METODI
                switch (mossa) {
                    case "1":
                        // Make sure we are actually in the placing phase
                        if (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {

                            placePlayer(serverStub, clientHandler);

                        } else {
                            System.err.println("❌ Azione non permessa: non siamo nella fase di piazzamento.");
                        }
                        break;

                    case "2":
                        // Make sure we are in the action resolution phase
                        if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
//                            drawTopCard(serverStub, clientHandler);
                        } else {
                            System.err.println("❌ Azione non permessa in questa fase del gioco.");
                        }
                        break;

                    case "3":
                        if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {

//                            drawBottomCard(serverStub, clientHandler);

                        } else {
                            System.err.println("❌ Azione non permessa in questa fase del gioco.");
                        }
                        break;

                    case "4":
                        if (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {

                            //passTurn(serverStub, clientHandler);

                        } else {
                            System.err.println("❌ Azione non permessa in questa fase del gioco.");
                        }
                        break;

                    default:
                        System.err.println("❌ Scelta non valida. Inserisci un numero tra quelli proposti.");
                        break;
                }

                // Pause a moment so the user can read the result of their action before the screen clears
                System.out.println("\n(Premi INVIO per continuare...)");
                scanner.nextLine();
            }

        } catch (Exception e) {
            System.err.println("Errore di connessione:");
            e.printStackTrace();
        }
    }

    // --- TURN MANAGEMENT LOGIC ---

    /**
     * Pauses the client thread until the server declares it's this player's turn.
     * Keeps the screen clean by showing whose turn it currently is.
     */
    private static void waitForMyTurn(ClientVirtualView cv) {
        boolean wasWaiting = false;

        synchronized (cv.turnLock) {
            while (!isMyTurn(cv)) {
                wasWaiting = true;
                clearScreen();
                System.out.println("⏳ In attesa del tuo turno...");

                // Display who is currently playing so the user isn't totally blind
                String currentPlayer = "";
                if (cv.getGamePhase() == PLACING_PHASE || cv.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                    currentPlayer = cv.getPlayerToPlace();
                } else if (cv.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || cv.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
                    currentPlayer = cv.getPlayerToPlay();
                }

                if (currentPlayer != null && !currentPlayer.isEmpty()) {
                    System.out.println("Attualmente sta giocando: " + currentPlayer);
                }

                try {
                    // Go to sleep until the ServerVirtualView wakes us up via turnLock.notifyAll()
                    cv.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // If we actually waited, do a quick UI celebration when it's finally our turn
        if (wasWaiting) {
            clearScreen();
            System.out.println("🔔 È IL TUO TURNO!");
            System.out.println("\nPremi INVIO per procedere...");
            new Scanner(System.in).nextLine();
        }
    }

    /**
     * Checks the current game phase and compares the local player's nickname
     * against the active player designated by the server.
     */
    private static boolean isMyTurn(ClientVirtualView cv) {
        if (cv.getGamePhase() == PLACING_PHASE || cv.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
            return myPlayer.getNickName().equals(cv.getPlayerToPlace());
        } else if (cv.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || cv.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) {
            return myPlayer.getNickName().equals(cv.getPlayerToPlay());
        }
        // If we are in SETUP or END_GAME, it's nobody's turn
        return false;
    }

    // --- GAME LOBBY LOGIC ---

    private static void waitForGameStart(ClientVirtualView clientHandler) {
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
        new Scanner(System.in).nextLine();
    }

    /**
     * Actually clears the terminal screen using ANSI escape codes.
     * \033[H moves the cursor to the top left.
     * \033[2J clears the entire screen.
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush(); // Force the terminal to draw the clear immediately
    }

    private static int numberOfPlayer() {
        int numOfPlayers = 0;
        Scanner scanner = new Scanner(System.in);
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

    private static COLOR bindColor() {
        Scanner scanner = new Scanner(System.in);
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

    private static boolean createGame(ServerRemoteInterface serverStub, ClientRemoteInterface clientHandler) {
        clearScreen();
        System.out.println("--- CREAZIONE PARTITA ---");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();
        COLOR colorTotem = bindColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);
        System.out.println();
        int playerNumber = numberOfPlayer();

        try {
            serverStub.createGame(player, playerNumber, clientHandler);
            System.out.println("\n✅ Partita creata con successo!");
            myPlayer= player; // Save our identity!
            return true;
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore: comunicazione con il Server.");
        } catch (IllegalStateException e) {
            System.err.println("\n❌ Errore: lobby già presente.");
        }
        return false;
    }

    private static boolean addPlayer(ServerRemoteInterface serverStub, ClientRemoteInterface clientHandler) {
        clearScreen();
        System.out.println("--- AGGIUNTA GIOCATORE ---");
        System.out.print("Inserisci nome giocatore: ");
        Scanner scanner = new Scanner(System.in);
        String nickname = scanner.nextLine();
        COLOR colorTotem = bindColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);

        try {
            serverStub.addPlayer(player, clientHandler);
            System.out.println("\n✅ Unito alla partita con successo!");
            myPlayer= player;  // Save our identity!
            return true;
        } catch (Exception e) {
            System.err.println("\n❌ Impossibile unirsi: " + e.getMessage());
        }
        return false;
    }


    //PLACING PHASE
    private static void placePlayer(ServerRemoteInterface serverStub, ClientVirtualView clientHandler) {
        System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
        boolean isPlaced = false; // Flag per capire se abbiamo finito
        while (!isPlaced) {
            int position = getPlacingIndex(clientHandler);
            try {
                // Proviamo a fare la mossa
                serverStub.placingPlayer(myPlayer, position);
                System.out.println("\n✅ Totem posizionato con successo nella casella " + position + "!");
                isPlaced = true; // Mossa valida! Il ciclo si interrompe e il turno finisce.

            } catch (TileOccupiedException e) {
                // ERRORE DI GIOCO: Il ciclo riparte e gli fa scegliere un'altra casella
                System.err.println("\n❌ Errore: La casella " + position + " è già occupata. Scegline un'altra.");

            } catch (IndexOutOfBoundsException e) {
                // ERRORE DI GIOCO: Il ciclo riparte
                System.err.println("\n❌ Errore: L'indice " + position + " non è valido per questa partita. Riprova.");

            } catch (RemoteException e) {
                // ERRORE CRITICO: Se cade la connessione, è inutile riprovare all'infinito
                System.err.println("\n❌ Errore di rete: Impossibile comunicare con il Server.");
                break; // Usciamo dal ciclo per evitare un loop infinito di errori di rete
            }
        }
    }
    /**
     * Prompts the user to enter a valid index for placing their Totem.
     * Keeps asking until a valid non-negative integer is provided.
     * * @return The chosen tile index.
     */
    private static int getPlacingIndex(ClientVirtualView clientVirtualView ) {
        Scanner scanner = new Scanner(System.in);
        int index = -1;

        while (true) {
            System.out.print("\n📍 Inserisci l'indice della casella in cui piazzare il Totem 1-"+clientVirtualView.getOfferTileSize());
            String input = scanner.nextLine();
            try {
                // 1. Try to parse the input into an integer
                index = Integer.parseInt(input);
                index-=1;
                // 2. Ensure it's at least 0. The Server will handle the upper bounds!
                if (index >= 0 && index< clientVirtualView.getOfferTileSize()) {
                    break; // Valid number, break the loop!
                } else {
                    System.out.println("❌ Errore: L'indice deve essere maggiore o uguale a 1 fino a"+ clientVirtualView.getOfferTileSize() +" Riprova.");
                }
            } catch (NumberFormatException e) {
                // 3. Catch the exception if the user types letters like "ciao"
                System.out.println("❌ Errore: Devi inserire un NUMERO intero valido. Riprova.");
            }
        }
        return index;
    }
}