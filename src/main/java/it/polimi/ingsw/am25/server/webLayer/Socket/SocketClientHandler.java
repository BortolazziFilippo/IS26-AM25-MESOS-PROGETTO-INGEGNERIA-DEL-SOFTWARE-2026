package it.polimi.ingsw.am25.server.webLayer.Socket;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.client.webLayer.Socket.ClientToServerMessage;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.RMI.ServerNetworkHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Server-side thread that handles a single Socket client connection.
 * Reads serialized {@link ClientToServerMessage} objects from the socket stream
 * and dispatches each to the shared {@link ServerRemoteInterface}, then writes
 * back a {@link it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface} Socket proxy.
 */
public class SocketClientHandler extends Thread{
    private static final String LOG_PREFIX = "[SERVER][SOCKET]";
    private final Socket socket;
    private final ServerRemoteInterface serverLogic;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * Creates a new handler for the given client socket.
     * @param socket      the accepted client socket.
     * @param serverLogic the server logic that processes incoming messages.
     */
    public SocketClientHandler(Socket socket,ServerRemoteInterface serverLogic){
        this.socket=socket;
        this.serverLogic=serverLogic;
    }

    /**
     * Reads messages from the client socket in a loop, dispatches them to the server logic,
     * and forwards any game errors back to the client without closing the connection.
     * The loop exits when the socket is closed or the network drops.
     */
    @Override
    public void run() {
        // Declared here so the catch block can reference it for disconnection handling.
        ClientSocketProxy clientSocketProxy = null;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            clientSocketProxy = new ClientSocketProxy(out);

            while (true) {
                ClientToServerMessage message;
                try {
                    // If the network drops, readObject throws and closes the socket.
                    message = (ClientToServerMessage) in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw e; // Re-throw to be caught by the outer handler and disconnect.
                }

                try {
                    // Game logic errors do NOT close the connection.
                    message.execute(serverLogic, clientSocketProxy);
                } catch (Exception e) {
                    UtilitiesFunction.logError(LOG_PREFIX, "Game logic error: " + e.getMessage());
                    // Notify the client of the error via the proxy.
                    clientSocketProxy.showErrorMessage(e.getMessage());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            UtilitiesFunction.logInfo(LOG_PREFIX, "A client disconnected (socket drop).");
            // Delegate disconnection handling to the network handler so the game
            // can skip this player's turns and notify remaining clients.
            if (clientSocketProxy != null && serverLogic instanceof ServerNetworkHandler handler) {
                handler.handleSocketClientDisconnection(clientSocketProxy);
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                UtilitiesFunction.logError(LOG_PREFIX, "Socket error on close: " + e.getMessage());
            }
        }
    }
}