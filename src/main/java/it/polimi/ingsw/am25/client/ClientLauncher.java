package it.polimi.ingsw.am25.client;

import it.polimi.ingsw.am25.client.GUI.GUIapp;
import it.polimi.ingsw.am25.client.TUI.ClientTUI;
import it.polimi.ingsw.am25.client.Utilities.ClientUtilitiesFunction;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ServerListener;
import it.polimi.ingsw.am25.client.webLayer.Socket.ServerSocketProxy;
import javafx.application.Application;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientLauncher {

    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String CYAN   = "\033[36m";
    private static final String YELLOW = "\033[33m";
    private static final String GREEN  = "\033[32m";
    private static final String RED    = "\033[31m";

    /**
     * Main entry point for the client. Initialises the logger, asks the user for the
     * network protocol (RMI/Socket), the server IP address, and the UI type (GUI/TUI),
     * then launches the chosen mode.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        ClientUtilitiesFunction.initLog();
        Scanner scanner = new Scanner(System.in);

        printBanner();

        String protocol = askProtocol(scanner);
        String ip       = askIp(scanner);
        String view     = askView(scanner);

        System.out.println();
        System.out.println(GREEN + "  Avvio " + view + " con " + protocol + " su " + ip + "..." + RESET);
        System.out.println();

        if (view.equals("GUI")) {
            Application.launch(GUIapp.class, ip, protocol);
        } else {
            launchTUI(ip, protocol);
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(CYAN + BOLD + "  ███╗   ███╗███████╗███████╗ ██████╗ ███████╗" + RESET);
        System.out.println(CYAN + BOLD + "  ████╗ ████║██╔════╝██╔════╝██╔═══██╗██╔════╝" + RESET);
        System.out.println(CYAN + BOLD + "  ██╔████╔██║█████╗  ███████╗██║   ██║███████╗" + RESET);
        System.out.println(CYAN + BOLD + "  ██║╚██╔╝██║██╔══╝  ╚════██║██║   ██║╚════██║" + RESET);
        System.out.println(CYAN + BOLD + "  ██║ ╚═╝ ██║███████╗███████║╚██████╔╝███████║" + RESET);
        System.out.println(CYAN + BOLD + "  ╚═╝     ╚═╝╚══════╝╚══════╝ ╚═════╝ ╚══════╝" + RESET);
        System.out.println();
        System.out.println(YELLOW + "  IS26-AM25  —  Client Launcher" + RESET);
        System.out.println("  " + "─".repeat(44));
        System.out.println();
    }

    private static String askProtocol(Scanner scanner) {
        while (true) {
            System.out.print(BOLD + "  Protocollo " + RESET + "[RMI/SOCKET] (default RMI): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return "RMI";
            if (input.equalsIgnoreCase("RMI"))    return "RMI";
            if (input.equalsIgnoreCase("SOCKET")) return "SOCKET";
            System.out.println(RED + "  Valore non valido. Scrivi RMI o SOCKET." + RESET);
        }
    }

    private static String askIp(Scanner scanner) {
        System.out.print(BOLD + "  IP server   " + RESET + "(default 127.0.0.1): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? "127.0.0.1" : input;
    }

    private static String askView(Scanner scanner) {
        while (true) {
            System.out.print(BOLD + "  Interfaccia " + RESET + "[GUI/TUI] (default GUI): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty())                return "GUI";
            if (input.equalsIgnoreCase("GUI"))  return "GUI";
            if (input.equalsIgnoreCase("TUI"))  return "TUI";
            System.out.println(RED + "  Valore non valido. Scrivi GUI o TUI." + RESET);
        }
    }

    private static void launchTUI(String serverIp, String method) {
        try {
            ClientVirtualView clientHandler = new ClientVirtualView();
            ServerRemoteInterface serverStub;

            if (method.equals("RMI")) {
                String myIp = ClientApp.getLocalIPv4();
                System.setProperty("java.rmi.server.hostname", myIp);
                Registry registry = LocateRegistry.getRegistry(serverIp, 1099);
                serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");
            } else {
                Socket socket = new Socket(serverIp, 6969);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in   = new ObjectInputStream(socket.getInputStream());
                serverStub = new ServerSocketProxy(out, clientHandler);
                new ServerListener(in, clientHandler).start();
            }

            ClientUtilitiesFunction.logInfo("[CLIENT][LAUNCHER]", "Connessione completata, avvio TUI.");
            new ClientTUI(serverStub, clientHandler).start();
            System.exit(0);

        } catch (Exception e) {
            System.out.println(RED + "  Errore di connessione: " + e.getMessage() + RESET);
            System.out.println(RED + "  Il server " + serverIp + " e' acceso?" + RESET);
        }
    }
}
