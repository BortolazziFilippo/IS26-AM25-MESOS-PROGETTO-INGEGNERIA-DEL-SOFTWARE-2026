package it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages;

import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.ServerToClientMessage;

import java.util.List;

public class GameWinnersMessage implements ServerToClientMessage {
    private final List<PlayerDTO> playerDTOList;

    public GameWinnersMessage(List<PlayerDTO> playerDTOList) {
        this.playerDTOList = playerDTOList;
    }

    @Override
    public void execute( ClientRemoteInterface clientRemoteInterface) throws Exception {
        clientRemoteInterface.gameWinners(playerDTOList);
    }
}
