package it.polimi.ingsw.am25.client.webLayer.Socket;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.io.Serializable;

/**
 * Marker interface for all messages sent from a Socket client to the server.
 * Each implementation wraps a single {@link ServerRemoteInterface} call so that
 * the {@link it.polimi.ingsw.am25.server.webLayer.Socket.SocketClientHandler} can
 * dispatch it without knowing the concrete message type.
 */
public interface ClientToServerMessage extends Serializable {
    /**
     * Dispatches this message by calling the appropriate method on the server interface.
     *
     * @param serverRemoteInterface the server's remote interface.
     * @param clientRemoteInterface the calling client's remote interface (used to register the client).
     * @throws Exception if the underlying call fails.
     */
    void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception;
}
