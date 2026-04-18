package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        try {
            // 1. Chiediamo il nickname all'utente
            Scanner scanner = new Scanner(System.in);
            System.out.print("Inserisci il tuo Nickname: ");
            String nickname = scanner.nextLine();

            // 2. Cerchiamo il Server (Registry) su "localhost" alla porta 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // 3. Prendiamo il "telecomando" del Server
            ServerRemoteInterface serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");

            // 4. Creiamo il NOSTRO ricevitore (che faremo controllare al server)
            ClientNetworkHandler myClientHandler = new ClientNetworkHandler();

        } catch (Exception e) {
            System.err.println("❌ Errore di connessione:");
            e.printStackTrace();
        }
    }
}