package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

public class ServerApp {
    private static final String LOG_PREFIX = "[SERVER][APP]";

    /**
     * Executes main.
     * @param args parameter args.
     */
    static void main(String[] args) {
        UtilitiesFunction.initLog();
        try {
            String myIp = getLocalIPv4();
            System.setProperty("java.rmi.server.hostname", myIp);
            ServerNetworkHandler serverObject = new ServerNetworkHandler();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MesosServer", serverObject);
            clearScreen();
            logServerEvent("Creato server all'IP "+ myIp);
            new java.util.Scanner(System.in).nextLine(); //this line keeps the server from shutting down
        } catch (Exception e) {
            UtilitiesFunction.logError(LOG_PREFIX +" "+e);
        }

    }

    /**
     * Returns local ipv4.
     * @return the result of the operation.
     */
    public static String getLocalIPv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            UtilitiesFunction.logError(LOG_PREFIX +" Impossibile rilevare l'IP automaticamente.");
        }
        return "127.0.0.1"; // Fallback sicuro
    }

    /**
     * Executes clear screen.
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Executes log server event.
     * @param message parameter message.
     */
    private static void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }
}
