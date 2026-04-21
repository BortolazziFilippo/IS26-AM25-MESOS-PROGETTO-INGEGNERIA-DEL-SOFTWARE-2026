package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerApp {
    private static final String LOG_PREFIX = "[SERVER][APP]";

    public static void main(String[] args) {
        UtilitiesFunction.initLog();
        try {
            // Creating server object
            ServerNetworkHandler serverObject = new ServerNetworkHandler();
            // Turning on registry rmi
            Registry registry = LocateRegistry.createRegistry(1099);
            // publishing server
            registry.rebind("MesosServer", serverObject);
            clearScreen();
            logServerEvent("RMI server started and ready!");
            new java.util.Scanner(System.in).nextLine(); //this line keeps the server from shutting down
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }
}
