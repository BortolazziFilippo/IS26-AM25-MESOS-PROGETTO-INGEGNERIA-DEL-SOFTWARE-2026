package it.polimi.ingsw.am25.client.webLayer.Socket;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.io.ObjectInputStream;

public class ServerListener extends Thread{
    private final ObjectInputStream in;
    private final ClientRemoteInterface clientRemoteInterface;

    public ServerListener(ObjectInputStream in, ClientRemoteInterface clientRemoteInterface) {
        this.in = in;
        this.clientRemoteInterface = clientRemoteInterface;
    }
    /**
     * Continuously reads server-to-client messages from the socket stream and
     * dispatches each one to the {@link ClientRemoteInterface}. Terminates on
     * any I/O or deserialization error.
     */
    @Override
    public void run() {
        try {
            while (true) {
                ServerToClientMessage message = (ServerToClientMessage) in.readObject();
                message.execute(clientRemoteInterface);
            }
        } catch (Exception e) {
            System.err.println("Connessione persa col Server!");
            e.printStackTrace();
            System.exit(0); //TODO: gestire disconnesione
        }
    }

}

