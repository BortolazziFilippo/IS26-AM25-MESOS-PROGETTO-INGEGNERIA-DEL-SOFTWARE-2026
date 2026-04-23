package it.polimi.ingsw.am25.server.webLayer.Socket;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.io.Serializable;

public interface ServerToClientMessage extends Serializable {
    void execute(ClientRemoteInterface clientRemoteInterface) throws Exception;
}
