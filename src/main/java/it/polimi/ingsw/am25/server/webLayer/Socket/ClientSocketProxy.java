package it.polimi.ingsw.am25.server.webLayer.Socket;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

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
            synchronized (out) {
                out.writeObject(new AskExtraDrawMessage());
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating extra draw");
            e.printStackTrace();
        }
    }

    @Override
    public void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new OrderOnDefaultTileMessage(orderOnDefaultTile));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating order on default tile");
            e.printStackTrace();
        }
    }

    @Override
    public void playerPlacedOnOffertile(String PlayerNickname, int offertilePosition) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerPlacedOnOffertileMessage(PlayerNickname, offertilePosition));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player placed on offertile");
            e.printStackTrace();
        }
    }

    @Override
    public void addedCardToTribe(String nickname, CardDTO cardDTO) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new AddedCardToTribeMessage(nickname, cardDTO));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating added card to tribe");
            e.printStackTrace();
        }
    }

    @Override
    public void playerUpdatePP(String nickname, int PP) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerUpdatePPMessage(nickname, PP));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player update PP");
            e.printStackTrace();
        }
    }

    @Override
    public void boardInitialize(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new BoardInitializeMessages(offerTileList, defaultTileList));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating board initialize");
            e.printStackTrace();
        }
    }

    @Override
    public void playerUpdateFood(String nickname, int food) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerUpdateFoodMessage(nickname, food));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player update food");
            e.printStackTrace();
        }
    }

    @Override
    public void topBuildingRefreshed(List<BuildingDTO> topBuildingCards) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new TopBuildingRefreshedMessage(topBuildingCards));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating top building refreshed");
            e.printStackTrace();
        }
    }

    @Override
    public void topCardRefreshed(List<CardDTO> topCards) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new TopCardRefreshedMessage(topCards));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating top card refreshed");
            e.printStackTrace();
        }
    }

    @Override
    public void bottomBuildRemoved(int position) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new BottomBuildRemovedMessage(position));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating bottom build removed");
            e.printStackTrace();
        }
    }

    @Override
    public void bottomCardRemoved(int position) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new BottomCardRemovedMessage(position));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating bottom card removed");
            e.printStackTrace();
        }
    }

    @Override
    public void topBuildRemoved(int position) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new TopBuildRemovedMessage(position));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating top build removed");
            e.printStackTrace();
        }
    }

    @Override
    public void topCardRemoved(int position) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new TopCardRemovedMessage(position));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating top card removed");
            e.printStackTrace();
        }
    }

    @Override
    public void initializeMarket(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new InitializeMarketMessage(topCards, bottomCards, topBuildings));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating initialize market");
            e.printStackTrace();
        }
    }

    @Override
    public void actionAvailableChanged(ActionDTO action) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new ActionAvailableChangedMessage(action));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating action available changed");
            e.printStackTrace();
        }
    }

    @Override
    public void playerToPlayChanged(PlayerDTO playerChanged) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerToPlayChangedMessage(playerChanged));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player to play changed");
            e.printStackTrace();
        }
    }

    @Override
    public void playerToPlaceChanged(PlayerDTO playerChanged) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerToPlaceChangedMessage(playerChanged));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player to place changed");
            e.printStackTrace();
        }
    }

    @Override
    public void gamePhaseChanged(GAME_PHASE gamePhase) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new GamePhaseChangedMessage(gamePhase));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating game phase changed");
            e.printStackTrace();
        }
    }

    @Override
    public void eraChanged(ERA newEra) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new EraChangedMessage(newEra));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating era changed");
            e.printStackTrace();
        }
    }

    @Override
    public void playerAdded(PlayerDTO playerAdded) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerAddedMessage(playerAdded));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating player added");
            e.printStackTrace();
        }
    }

    @Override
    public void gameWinners(List<PlayerDTO> playerDTOSWinner) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new GameWinnersMessage(playerDTOSWinner));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating game winners");
            e.printStackTrace();
        }
    }

    @Override
    public void initializeGame(ERA currentEra, GAME_PHASE gamePhase, String PlayerToPlace, String PlayerToPlay) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new InitializeGameMessage(currentEra, gamePhase, PlayerToPlace, PlayerToPlay));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Error comunicating initialize game");
            e.printStackTrace();
        }
    }
    @Override
    public void showErrorMessage(String errorMessage) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new it.polimi.ingsw.am25.client.webLayer.Socket.messages.ErrorMessage(errorMessage));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(PREFIX, "Errore nella comunicazione del messaggio di errore");
            e.printStackTrace();
        }
    }

    @Override
    public void eventResolved(String message){
        try {
            synchronized (out) {
                out.writeObject(new ResolvedEventMessage(message));
                out.flush();
                out.reset();
            }
        }catch (java.io.IOException e){
            UtilitiesFunction.logError(PREFIX, "Error comunicating event resolved");
            e.printStackTrace();
        }
    }
}