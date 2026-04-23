package it.polimi.ingsw.am25.client;

import it.polimi.ingsw.am25.client.TUI.ClientTUI;
import it.polimi.ingsw.am25.client.Utilities.ClientUtilitiesFunction;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

public class ClientApp {
    private static final String LOG_PREFIX = "[CLIENT][APP]";

    /**
     * Executes main.
     * @param args parameter args.
     */
    public static void main(String[] args) {
        ClientUtilitiesFunction.initLog();
        //if no ip si written default sets loopback
        String serverIp = "127.0.0.1";

        if (args.length > 0) {
            serverIp = args[0]; //gets ip from terminal
        }

        try {
            //search client ip
            String myIp = getLocalIPv4();

            System.setProperty("java.rmi.server.hostname", myIp);
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Tentativo di connessione al Server [" + serverIp + "] dal Client [" + myIp + "]...");

            Registry registry = LocateRegistry.getRegistry(serverIp, 1099);
            ServerRemoteInterface serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");
            ClientVirtualView clientHandler = new ClientVirtualView();
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Connessione al server completata con successo.");

            // starts ui
            ClientTUI tui = new ClientTUI(serverStub, clientHandler);
            ClientUtilitiesFunction.logInfo(LOG_PREFIX, "Avvio dell'interfaccia TUI client.");
            tui.start();

        } catch (Exception e) {
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Errore critico di connessione. Il Server " + serverIp + " è acceso? Dettaglio: " + e.getMessage());
        }
    }

    // ---  AUTOMATIC BINDER---
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
            ClientUtilitiesFunction.logError(LOG_PREFIX, "Impossibile rilevare l'IP automaticamente.");
        }
        return "127.0.0.1";
    }
}