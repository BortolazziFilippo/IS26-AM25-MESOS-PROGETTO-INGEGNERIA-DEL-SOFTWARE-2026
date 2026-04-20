package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameFullException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameReadyToStartException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameStartedException;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NameOrColorAlreadyTakenException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import javax.xml.transform.Source;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLOutput;
import java.util.Scanner;


public class ClientApp {

    public static void main(String[] args) {
        try {
            // Cerchiamo il Server (Registry) su "localhost" alla porta 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            //Prendiamo il "telecomando" del Server
            ServerRemoteInterface serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");
            // Creiamo il NOSTRO ricevitore (che faremo controllare al server)
            ClientVirtualView clientHandler = new ClientVirtualView();
            Scanner scanner = new Scanner(System.in);

            while (true) {
                // 1. PULISCE LO SCHERMO PRIMA DI STAMPARE IL MENU
                clearScreen();
                System.out.println("--- MENU PRINCIPALE ---");
                System.out.println("Inserisci l'azione che vuoi compiere:");
                System.out.println("1 - Crea gioco");
                System.out.println("2 - Entra in una partita");
                System.out.println("3 - Pesca una carta da sopra");
                System.out.println("4 - Pesca una carta da sotto");
                System.out.print("Scelta: ");

                String scelta = scanner.nextLine();

                switch (scelta) {
                    case "1":
                        createGame(serverStub, clientHandler);
                        break;
                    case "2":
                        clearScreen();
                        System.out.println("Hai scelto: Entra in una partita.");
                        addPlayer(serverStub,clientHandler);
                        break;
                    case "3":
                        clearScreen();
                        System.out.println("Hai scelto: Pesca una carta da sopra.");
                        // Chiedi il tipo di carta, la posizione e chiama serverStub.selectCardFromTopList(...)
                        break;
                    case "4":
                        clearScreen();
                        System.out.println("Hai scelto: Pesca una carta da sotto.");
                        // Chiedi il tipo di carta, la posizione e chiama serverStub.selectCardFromBottomList(...)
                        break;
                    default:
                        System.out.println("❌ Scelta non valida. Riprova.");
                        break;
                }

                // 2. PAUSA PRIMA DI RICOMINCIARE IL CICLO (E PULIRE DI NUOVO)
                System.out.println("\nPremi INVIO per continuare...");
                scanner.nextLine();
            }
        } catch (Exception e) {
            System.err.println("Errore di connessione:");
            e.printStackTrace();
        }
    }

    /**
     * this method clears the screen
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
        // Scanner portato fuori dal loop per maggiore sicurezza
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

    private static void createGame(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) {
        // Pulisce lo schermo per mostrare un'interfaccia di creazione pulita
        clearScreen();
        System.out.println("--- CREAZIONE PARTITA ---");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Inserisci nome giocatore: ");
        String nickname = scanner.nextLine();

        COLOR colorTotem = bindColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);

        System.out.println(); // Spazio vuoto per estetica
        int playerNumber = numberOfPlayer();

        try {
            serverRemoteInterface.createGame(player, playerNumber, clientRemoteInterface);
            System.out.println("\n✅ Partita creata con successo!");
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore comunicazione con il Server.");
        } catch (IllegalStateException e) {
            System.err.println("\n❌ Errore: lobby già presente, usa l'opzione 'Entra in una partita'.");
        }
    }

    private static void addPlayer(ServerRemoteInterface serverRemoteInterface,ClientRemoteInterface clientRemoteInterface){
        clearScreen();
        System.out.println("--- AGGIUNTA GIOCATORE ---");
        System.out.print("Inserisci nome giocatore: ");
        Scanner scanner = new Scanner(System.in);
        String nickname = scanner.nextLine();
        COLOR colorTotem = bindColor();
        PlayerDTO player = new PlayerDTO(nickname, 0, 0, colorTotem);
        System.out.println(); // Spazio vuoto per estetica
        try{
            serverRemoteInterface.addPlayer(player,clientRemoteInterface);
            System.out.println("\n✅ Unito alla partita con successo");
        } catch (RemoteException e) {
            System.err.println("\n❌ Errore: comunicazione con server");
        } catch (GameFullException e) {
            System.err.println("\n❌ Errore: Lobby piena, non possibile aggiungersi");
        }catch(NameOrColorAlreadyTakenException e){
            System.err.println("\n❌ Errore: Nome o colore totem gia preso");
        }catch (GameStartedException e){
            System.err.println("\n❌ Errore: Partita gia in corso");
        }
    }
}