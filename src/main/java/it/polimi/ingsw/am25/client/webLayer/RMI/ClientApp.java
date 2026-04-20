package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.client.TUI.ClientTUI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientApp {

    public static void main(String[] args) {
        try {
            // 1. Setup the RMI Connection
            Registry registry = LocateRegistry.getRegistry(args[0], 1099);
            ServerRemoteInterface serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");
            ClientVirtualView clientHandler = new ClientVirtualView();
            // 2. Initialize the Text User Interface (TUI) and start it!
            ClientTUI tui = new ClientTUI(serverStub, clientHandler);
            tui.start();
        } catch (Exception e) {
            System.err.println("❌ Errore critico di connessione:");
            e.printStackTrace();
        }
    }
}