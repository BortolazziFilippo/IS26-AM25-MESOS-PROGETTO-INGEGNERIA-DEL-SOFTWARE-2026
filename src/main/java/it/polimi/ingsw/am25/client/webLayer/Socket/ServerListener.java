package it.polimi.ingsw.am25.client.webLayer.Socket;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Background thread that continuously reads {@link ServerToClientMessage} objects from the
 * server socket stream and dispatches each one to the local {@link ClientVirtualView}.
 *
 * <p>The listener separates two categories of errors:
 * <ul>
 *   <li><b>Transport errors</b> ({@link IOException}, {@link ClassNotFoundException} during
 *       {@code readObject}) — indicate the server is gone; the listener calls
 *       {@link ClientVirtualView#handleServerDeath()} so the TUI can exit cleanly and returns.</li>
 *   <li><b>Application errors</b> (any exception thrown by {@code message.execute()}) — logged
 *       and silently ignored so the connection stays alive.</li>
 * </ul>
 */
public class ServerListener extends Thread {
    private static final String LOG_PREFIX = "[CLIENT][SOCKET_LISTENER]";
    private final ObjectInputStream in;
    private final ClientVirtualView clientHandler;

    /**
     * Creates a server listener that reads from the given stream and dispatches to the given view.
     *
     * @param in            the input stream connected to the server socket.
     * @param clientHandler the local client view that receives the dispatched messages.
     */
    public ServerListener(ObjectInputStream in, ClientVirtualView clientHandler) {
        this.in = in;
        this.clientHandler = clientHandler;
        setDaemon(true);
        setName("server-listener");
    }

    /**
     * Continuously reads server-to-client messages from the socket stream and dispatches each
     * to the {@link ClientVirtualView}. Terminates only when the transport layer fails.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ServerToClientMessage message;

            // --- Read phase: transport errors terminate the listener ---
            try {
                message = (ServerToClientMessage) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println(LOG_PREFIX + " Connessione persa col Server: " + e.getMessage());
                clientHandler.handleServerDeath();
                return;
            }

            // --- Dispatch phase: application errors are logged and ignored ---
            try {
                message.execute(clientHandler);
            } catch (Exception e) {
                System.err.println(LOG_PREFIX + " Errore nell'elaborazione del messaggio: " + e.getMessage());
                // Stay connected — this is a game-logic issue, not a network issue.
            }
        }
    }
}
