package it.polimi.ingsw.am25.server.webLayer.RMI;

import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.Socket.SocketClientHandler;

import java.io.IOException;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

/**
 * Entry point for the Mesos server. Starts the RMI registry on port 1099 and a Socket
 * listener on port 6969, then delegates all client connections to {@link ServerNetworkHandler}.
 */
public class ServerApp {
    private static final String LOG_PREFIX = "[SERVER][APP]";

    /**
     * Creates a new server app instance.
     */
    public ServerApp() {
    }

    /**
     * Executes main.
     *
     * @param args parameter args.
     */
    public static void main(String[] args) {
        UtilitiesFunction.initLog();
        try {
            String myIp = getLocalIPv4();
            System.setProperty("java.rmi.server.hostname", myIp);
            ServerNetworkHandler serverNetworkHandler = new ServerNetworkHandler();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MesosServer", serverNetworkHandler);
            clearScreen();
            logServerEvent("Server started at IP " + myIp);
            new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(6969)) {
                    logServerEvent("Socket server listening on port 6969");
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        logServerEvent("New socket client connected. IP: " + clientSocket.getInetAddress());
                        SocketClientHandler handler = new SocketClientHandler(clientSocket, serverNetworkHandler);
                        handler.start();
                    }
                } catch (IOException e) {
                    UtilitiesFunction.logError(LOG_PREFIX, "Fatal socket server error: " + e.getMessage());
                }
            }).start();
            logServerEvent("RMI server listening on port 1099");
        } catch (Exception e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Critical startup error. If 'Port already in use', close existing server instances. Detail: " + e.getMessage());
        }

    }

    /**
     * Returns local ipv4.
     *
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
            UtilitiesFunction.logError(LOG_PREFIX, "Cannot detect local IP automatically, falling back to 127.0.0.1.");
        }
        return "127.0.0.1"; // safe fallback
    }

    /**
     * Executes clear screen.
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }
}
