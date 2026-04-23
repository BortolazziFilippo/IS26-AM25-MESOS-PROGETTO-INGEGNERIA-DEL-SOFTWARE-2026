package it.polimi.ingsw.am25.client.webLayer.RMI;

import it.polimi.ingsw.am25.client.TUI.ClientTUI;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

public class ClientApp {

    /**
     * Executes main.
     * @param args parameter args.
     */
    public static void main(String[] args) {
        //if no ip si written default sets loopback
        String serverIp = "127.0.0.1";

        if (args.length > 0) {
            serverIp = args[0]; //gets ip from terminal
        }

        try {
            //search client ip
            String myIp = getLocalIPv4();

            System.setProperty("java.rmi.server.hostname", myIp);
            System.out.println("🌐 Tentativo di connessione al Server [" + serverIp + "] dal Client [" + myIp + "]...");

            Registry registry = LocateRegistry.getRegistry(serverIp, 1099);
            ServerRemoteInterface serverStub = (ServerRemoteInterface) registry.lookup("MesosServer");
            ClientVirtualView clientHandler = new ClientVirtualView();

            // starts ui
            ClientTUI tui = new ClientTUI(serverStub, clientHandler);
            tui.start();

        } catch (Exception e) {
            System.err.println("❌ Errore critico di connessione. Il Server " + serverIp + " è acceso?");
            e.printStackTrace();
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
            System.err.println("Impossibile rilevare l'IP automaticamente.");
        }
        return "127.0.0.1";
    }
}