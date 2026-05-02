package it.polimi.ingsw.am25.client.webLayer.Socket;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.io.ObjectInputStream;

/**
 * Background thread that continuously reads {@link it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage}
 * objects from the server socket stream and dispatches each one to the local {@link ClientRemoteInterface}.
 */
public class ServerListener extends Thread{
    private final ObjectInputStream in;
    private final ClientRemoteInterface clientRemoteInterface;

    /**
     * Creates a server listener that reads from the given stream and dispatches to the given handler.
     * @param in the input stream connected to the server socket.
     * @param clientRemoteInterface the local client handler that receives the dispatched messages.
     */
    public ServerListener(ObjectInputStream in, ClientRemoteInterface clientRemoteInterface) {
        this.in = in;
        this.clientRemoteInterface = clientRemoteInterface;
        setDaemon(true);
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
            if (clientRemoteInterface instanceof ClientVirtualView cv && cv.getGamePhase() == GAME_PHASE.END_GAME) {
                return;
            }
            //TODO GESTIRE DISCONNESSIONe
            System.err.println("Connessione persa col Server!");
            System.exit(0);
        }
    }
}

