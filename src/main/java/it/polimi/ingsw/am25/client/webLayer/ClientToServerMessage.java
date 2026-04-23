package it.polimi.ingsw.am25.client.webLayer;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.io.Serializable;

public interface ClientToServerMessage extends Serializable {
    void execute(ServerRemoteInterface serverRemoteInterface, ClientRemoteInterface clientRemoteInterface) throws Exception;
}
