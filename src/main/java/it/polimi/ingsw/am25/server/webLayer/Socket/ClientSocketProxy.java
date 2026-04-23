package it.polimi.ingsw.am25.server.webLayer.Socket;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

// NB: Usa il tuo IDE (es. Alt+Invio su IntelliJ) per importare in automatico tutte le classi dei messaggi!
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes.BoardInitializeMessages;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes.OrderOnDefaultTileMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes.PlayerPlacedOnOffertileMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.drawOneMoreCard.AskExtraDrawMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages.*;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages.*;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage.AddedCardToTribeMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage.PlayerUpdateFoodMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage.PlayerUpdatePPMessage;

import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.List;

public class ClientSocketProxy implements ClientRemoteInterface {
    private final String PREFIX = "[SERVER][SOCKET]";
    private final ObjectOutputStream out;

    public ClientSocketProxy(ObjectOutputStream out) {
        this.out = out;
    }

    @Override
    public void askExtraDraw() throws RemoteException {
        try {
            out.writeObject(new AskExtraDrawMessage());
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating extra draw");
        }
    }

    @Override
    public void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile) throws RemoteException {
        try {
            out.writeObject(new OrderOnDefaultTileMessage(orderOnDefaultTile));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating order on default tile");
        }
    }

    @Override
    public void playerPlacedOnOffertile(String PlayerNickname, int offertilePosition) throws RemoteException {
        try {
            out.writeObject(new PlayerPlacedOnOffertileMessage(PlayerNickname, offertilePosition));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player placed on offertile");
        }
    }

    @Override
    public void addedCardToTribe(String nickname, CardDTO cardDTO) throws RemoteException {
        try {
            out.writeObject(new AddedCardToTribeMessage(nickname, cardDTO));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating added card to tribe");
        }
    }

    @Override
    public void playerUpdatePP(String nickname, int PP) throws RemoteException {
        try {
            out.writeObject(new PlayerUpdatePPMessage(nickname, PP));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player update PP");
        }
    }

    @Override
    public void boardInitialize(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) throws RemoteException {
        try {
            out.writeObject(new BoardInitializeMessages(offerTileList, defaultTileList));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating board initialize");
        }
    }

    @Override
    public void playerUpdateFood(String nickname, int food) throws RemoteException {
        try {
            out.writeObject(new PlayerUpdateFoodMessage(nickname, food));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player update food");
        }
    }

    @Override
    public void topBuildingRefreshed(List<BuildingDTO> topBuildingCards) throws RemoteException {
        try {
            out.writeObject(new TopBuildingRefreshedMessage(topBuildingCards));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating top building refreshed");
        }
    }

    @Override
    public void topCardRefreshed(List<CardDTO> topCards) throws RemoteException {
        try {
            out.writeObject(new TopCardRefreshedMessage(topCards));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating top card refreshed");
        }
    }

    @Override
    public void bottomBuildRemoved(int position) throws RemoteException {
        try {
            out.writeObject(new BottomBuildRemovedMessage(position));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating bottom build removed");
        }
    }

    @Override
    public void bottomCardRemoved(int position) throws RemoteException {
        try {
            out.writeObject(new BottomCardRemovedMessage(position));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating bottom card removed");
        }
    }

    @Override
    public void topBuildRemoved(int position) throws RemoteException {
        try {
            out.writeObject(new TopBuildRemovedMessage(position));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating top build removed");
        }
    }

    @Override
    public void topCardRemoved(int position) throws RemoteException {
        try {
            out.writeObject(new TopCardRemovedMessage(position));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating top card removed");
        }
    }

    @Override
    public void initializeMarket(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings) throws RemoteException {
        try {
            out.writeObject(new InitializeMarketMessage(topCards, bottomCards, topBuildings));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating initialize market");
        }
    }

    @Override
    public void actionAvailableChanged(ActionDTO action) throws RemoteException {
        try {
            out.writeObject(new ActionAvailableChangedMessage(action));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating action available changed");
        }
    }

    @Override
    public void playerToPlayChanged(PlayerDTO playerChanged) throws RemoteException {
        try {
            out.writeObject(new PlayerToPlayChangedMessage(playerChanged));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player to play changed");
        }
    }

    @Override
    public void playerToPlaceChanged(PlayerDTO playerChanged) throws RemoteException {
        try {
            out.writeObject(new PlayerToPlaceChangedMessage(playerChanged));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player to place changed");
        }
    }

    @Override
    public void gamePhaseChanged(GAME_PHASE gamePhase) throws RemoteException {
        try {
            out.writeObject(new GamePhaseChangedMessage(gamePhase));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating game phase changed");
        }
    }

    @Override
    public void eraChanged(ERA newEra) throws RemoteException {
        try {
            out.writeObject(new EraChangedMessage(newEra));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating era changed");
        }
    }

    @Override
    public void playerAdded(PlayerDTO playerAdded) throws RemoteException {
        try {
            out.writeObject(new PlayerAddedMessage(playerAdded));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player added");
        }
    }

    @Override
    public void gameWinners(List<PlayerDTO> playerDTOSWinner) throws RemoteException {
        try {
            out.writeObject(new GameWinnersMessage(playerDTOSWinner));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating game winners");
        }
    }

    @Override
    public void initializeGame(ERA currentEra, GAME_PHASE gamePhase, String PlayerToPlace, String PlayerToPlay) throws RemoteException {
        try {
            out.writeObject(new InitializeGameMessage(currentEra, gamePhase, PlayerToPlace, PlayerToPlay));
            out.flush();
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating initialize game");
        }
    }
}