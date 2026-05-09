package it.polimi.ingsw.am25.server.webLayer.Socket;

import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.io.Serializable;

/**
 * Marker interface for all messages sent from the server to a Socket client.
 * Each implementation wraps a single {@link ClientRemoteInterface} call so that
 * the {@link it.polimi.ingsw.am25.server.webLayer.Socket.SocketClientHandler} can
 * dispatch it without knowing the concrete message type.
 */
public interface ServerToClientMessage extends Serializable {
    /**
     * Dispatches this message by calling the appropriate method on the client interface.
     *
     * @param clientRemoteInterface the target client's remote interface.
     * @throws Exception if the underlying call fails.
     */
    void execute(ClientRemoteInterface clientRemoteInterface) throws Exception;
}
