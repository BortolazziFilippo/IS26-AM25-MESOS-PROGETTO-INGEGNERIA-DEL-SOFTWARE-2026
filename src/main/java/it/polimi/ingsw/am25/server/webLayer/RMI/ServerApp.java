package it.polimi.ingsw.am25.server.webLayer.RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerApp {
    public static void main(String[] args) {
        try {
            // Creating server object
            ServerNetworkHandler serverObject = new ServerNetworkHandler();
            // Turning on registry rmi
            Registry registry = LocateRegistry.createRegistry(1099);
            // publishing server
            registry.rebind("MesosServer", serverObject);
            clearScreen();
            System.out.println("Server RMI avviato e pronto!");
            new java.util.Scanner(System.in).nextLine(); //this line keeps the server from shutting down
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
