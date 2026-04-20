package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameFullException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameReadyToStartException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameStartedException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NameOrColorAlreadyTakenException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) {
        try {
            // Let's find the RMI Registry on the local machine (port 1099 is the default)
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            // Grab the server's remote stub so we can send commands to it
            ServerRemoteInterface serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");
            // Create our local receiver (the virtual view) that the server will use to push updates to us
            ClientVirtualView clientHandler = new ClientVirtualView();
            Scanner scanner = new Scanner(System.in);

            boolean inGame = false;

            // MAIN MENU LOOP: We'll stay trapped in this loop until we successfully enter a game
            while (!inGame) {
                clearScreen();
                System.out.println("--- MENU PRINCIPALE ---");
                System.out.println("Inserisci l'azione che vuoi compiere:");
                System.out.println("1 - Crea gioco");
                System.out.println("2 - Entra in una partita");
                System.out.print("Scelta: ");

                String scelta = scanner.nextLine();

                switch (scelta) {
                    case "1":
                        // If the game creation goes smoothly, we pause and wait for the lobby to fill up
                        if (createGame(serverStub, clientHandler)) {
                            waitForGameStart(clientHandler);
                            inGame = true; // Break out of the main menu loop
                        } else {
                            System.out.println("\nPremi INVIO per continuare...");
                            scanner.nextLine();
                        }
                        break;
                    case "2":
                        // Same here: if joining is successful, we wait for the host to start or the lobby to fill
                        if (addPlayer(serverStub, clientHandler)) {
                            waitForGameStart(clientHandler);
                            inGame = true; // Break out of the main menu loop
                        } else {
                            System.out.println("\nPremi INVIO per continuare...");
                            scanner.nextLine();
                        }
                        break;
                    default:
                        System.out.println("❌ Scelta non valida. Riprova.");
                        System.out.println("\nPremi INVIO per continuare...");
                        scanner.nextLine();
                        break;
                }
            }

            // ==========================================================
            // THE GAME IS ON!
            // From this point forward, we are actually playing.
            // ==========================================================
            while (true) {
                clearScreen();
                System.out.println("--- TURNO DI GIOCO ---");
                System.out.println("1 - Piazza Totem (Fase Piazzamento)");
                System.out.println("2 - Pesca carta da sopra (Fase Azioni)");
                System.out.println("3 - Pesca carta da sotto (Fase Azioni)");
                System.out.println("4 - Passa il turno");
                System.out.print("Scegli: ");
                // Keep the scanner open to read the actual moves later
                scanner.nextLine();
            }

        } catch (Exception e) {
            System.err.println("Errore di connessione:");
            e.printStackTrace();
        }
    }

    /**
     * Helper method: Pauses the client thread without burning CPU cycles until
     * the server fires the 'game started' event.
     */
    private static void waitForGameStart(ClientVirtualView clientHandler) {
        System.out.println("\n⏳ In attesa che si connettano gli altri giocatori...");

        synchronized (clientHandler.gameStartLock) {
            // Keep waiting as long as the game hasn't started.
            // The while loop protects us against spurious wakeups (a known Java thread quirk).
            while (!clientHandler.isGameStarted) {
                try {
                    // Put this thread to sleep. It will be awakened by the notifyAll() in the VirtualView
                    clientHandler.gameStartLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // At this point, the thread is awake and the game has started!
        clearScreen();
        System.out.println("🎉 GIOCO INIZIATO! 🎉");
        System.out.println("Tutti i giocatori sono connessi.");
        System.out.println("\nPremi INVIO per entrare nella plancia di gioco...");
        new Scanner(System.in).nextLine();
    }

    /**
     * Clears the console by printing empty lines.
     * It's a quick hack, but it works flawlessly across all OS terminals and IDEs.
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static int numberOfPlayer() {
        int numOfPlayers = 0;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Quanti giocatori (2-5)? ");
            String input = scanner.nextLine();

            try {
                numOfPlayers = Integer.parseInt(input);

                // Check bounds before proceeding
                if (numOfPlayers >= 2 && numOfPlayers <= 5) {
                    break;
                } else {
                    System.out.println("Errore: Il numero deve essere compreso tra 2 e 5. Riprova.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Errore: Devi inserire un NUMERO valido, non delle lettere. Riprova.");
            }
        }
        return numOfPlayers;
    }

    private static COLOR bindColor() {
        // Keeping the scanner outside the loop to avoid memory leaks or input stream issues
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nScegli colore totem:");
            System.out.println("1-ROSSO");
            System.out.println("2-BLUE");
            System.out.println("3-YELLOW");
            System.out.println("4-GREEN");
            System.out.println("5-PURPLE");
            System.out.print("Scelta: ");

            String color = scanner.nextLine();

            switch (color) {
                case "1": return COLOR.RED;
                case "2": return COLOR.BLUE;
                case "3": return COLOR.YELLOW;
                case "4": return COLOR.GREEN;
                case "5": return COLOR.PURPLE;
                default: System.out.println("Input non valido, riprova ");
            }
        }
    }

    private static boolean createGame(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) {
        clearScreen();
        System.out.println("--- CREAZIONE PARTITA ---");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();

        COLOR colorTotem = bindColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);

        System.out.println(); // Just some aesthetic spacing
        int playerNumber = numberOfPlayer();

        try {
            serverRemoteInterface.createGame(player, playerNumber, clientRemoteInterface);
            System.out.println("\n✅ Partita creata con successo!");
            return true; // We're good to go!
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore comunicazione con il Server.");
        } catch (IllegalStateException e) {
            System.err.println("\n❌ Errore: lobby già presente, usa l'opzione 'Entra in una partita'.");
        }
        return false; // Something went wrong, return to main menu
    }

    private static boolean addPlayer(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) {
        clearScreen();
        System.out.println("--- AGGIUNTA GIOCATORE ---");
        System.out.print("Inserisci nome giocatore: ");
        Scanner scanner = new Scanner(System.in);
        String nickname = scanner.nextLine();

        COLOR colorTotem = bindColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);

        System.out.println(); // Just some aesthetic spacing

        try {
            serverRemoteInterface.addPlayer(player, clientRemoteInterface);
            System.out.println("\n✅ Unito alla partita con successo");
            return true; // Successfully joined!
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore: comunicazione con server");
        } catch (GameFullException e) {
            System.err.println("\n❌ Errore: Lobby piena, non è possibile aggiungersi");
        } catch (NameOrColorAlreadyTakenException e) {
            System.err.println("\n❌ Errore: Nome o colore totem già preso");
        } catch (GameStartedException e) {
            System.err.println("\n❌ Errore: Partita già in corso");
        }
        return false; // Failed to join, go back to main menu
    }
}