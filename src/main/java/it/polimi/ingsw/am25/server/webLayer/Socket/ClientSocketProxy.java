package it.polimi.ingsw.am25.server.webLayer.Socket;

import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.SendRankMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes.BoardInitializeMessages;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes.OrderOnDefaultTileMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.boardMessaes.PlayerPlacedOnOffertileMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.drawOneMoreCard.AskExtraDrawMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.gameMessages.*;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.marketMessages.*;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage.AddedCardToTribeMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage.PlayerDisconnectedMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage.PlayerUpdateFoodMessage;
import it.polimi.ingsw.am25.server.webLayer.Socket.messages.playerMessage.PlayerUpdatePPMessage;

import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Proxy for {@link ClientRemoteInterface} over a Socket connection,
 * allowing the server to treat a Socket client as an RMI stub.
 */

public class ClientSocketProxy implements ClientRemoteInterface {
    private static final String LOG_PREFIX = "[SERVER][SOCKET]";
    private final ObjectOutputStream out;

    /**
     * Creates a new proxy that forwards calls to the client via the given output stream.
     *
     * @param out the output stream connected to the client socket.
     */
    public ClientSocketProxy(ObjectOutputStream out) {
        this.out = out;
    }

    /**
     * Sends an ask-extra-draw notification to the client, including the end-of-round market snapshot.
     */
    @Override
    public void askExtraDraw(List<CardDTO> snapshotCards, List<BuildingDTO> snapshotBuildings) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new AskExtraDrawMessage(snapshotCards, snapshotBuildings));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating extra draw");
            e.printStackTrace();
        }
    }

    /**
     * Sends the player order on the default tile to the client.
     *
     * @param orderOnDefaultTile ordered list of players on the default tile.
     */
    @Override
    public void orderOnDefaultTile(List<PlayerDTO> orderOnDefaultTile) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new OrderOnDefaultTileMessage(orderOnDefaultTile));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating order on default tile");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that a player was placed on an offer tile.
     *
     * @param PlayerNickname    the player's nickname.
     * @param offertilePosition the tile position chosen.
     */
    @Override
    public void playerPlacedOnOffertile(String PlayerNickname, int offertilePosition) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerPlacedOnOffertileMessage(PlayerNickname, offertilePosition));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating player placed on offertile");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that a card was added to a player's tribe.
     *
     * @param nickname the player's nickname.
     * @param cardDTO  the card that was added.
     */
    @Override
    public void addedCardToTribe(String nickname, CardDTO cardDTO) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new AddedCardToTribeMessage(nickname, cardDTO));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating added card to tribe");
            e.printStackTrace();
        }
    }

    /**
     * Sends an updated prestige-point value for a player to the client.
     *
     * @param nickname the player's nickname.
     * @param PP       the new prestige-point total.
     */
    @Override
    public void playerUpdatePP(String nickname, int PP) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerUpdatePPMessage(nickname, PP));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating player update PP");
            e.printStackTrace();
        }
    }

    /**
     * Sends the initial board state to the client.
     *
     * @param offerTileList   the offer tiles on the board.
     * @param defaultTileList the default tiles on the board.
     */
    @Override
    public void boardInitialize(List<OffertileDTO> offerTileList, List<DefaultTileDTO> defaultTileList) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new BoardInitializeMessages(offerTileList, defaultTileList));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating board initialize");
            e.printStackTrace();
        }
    }

    /**
     * Sends an updated food value for a player to the client.
     *
     * @param nickname the player's nickname.
     * @param food     the new food total.
     */
    @Override
    public void playerUpdateFood(String nickname, int food) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerUpdateFoodMessage(nickname, food));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating player update food");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that the top building row was refreshed.
     *
     * @param topBuildingCards the new top building row.
     */
    @Override
    public void topBuildingRefreshed(List<BuildingDTO> topBuildingCards) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new TopBuildingRefreshedMessage(topBuildingCards));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating top building refreshed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that the top card row was refreshed.
     *
     * @param topCards the new top card row.
     */
    @Override
    public void topCardRefreshed(List<CardDTO> topCards) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new TopCardRefreshedMessage(topCards));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating top card refreshed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that a building was removed from the bottom row.
     *
     * @param position the index of the removed building.
     */
    @Override
    public void bottomBuildRemoved(int position) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new BottomBuildRemovedMessage(position));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating bottom build removed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that a card was removed from the bottom row.
     *
     * @param position the index of the removed card.
     */
    @Override
    public void bottomCardRemoved(int position) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new BottomCardRemovedMessage(position));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating bottom card removed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that a building was removed from the top row.
     *
     * @param position the index of the removed building.
     */
    @Override
    public void topBuildRemoved(int position) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new TopBuildRemovedMessage(position));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating top build removed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that a card was removed from the top row.
     *
     * @param position the index of the removed card.
     */
    @Override
    public void topCardRemoved(int position) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new TopCardRemovedMessage(position));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating top card removed");
            e.printStackTrace();
        }
    }

    /**
     * Sends the initial market state to the client.
     *
     * @param topCards     top row of cards.
     * @param bottomCards  bottom row of cards.
     * @param topBuildings top row of buildings.
     */
    @Override
    public void initializeMarket(List<CardDTO> topCards, List<CardDTO> bottomCards, List<BuildingDTO> topBuildings) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new InitializeMarketMessage(topCards, bottomCards, topBuildings));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating initialize market");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client about updated available actions for the current turn.
     *
     * @param action the new action availability descriptor.
     */
    @Override
    public void actionAvailableChanged(ActionDTO action) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new ActionAvailableChangedMessage(action));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating action available changed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client which player must act next.
     *
     * @param playerChanged the player whose turn it now is.
     */
    @Override
    public void playerToPlayChanged(PlayerDTO playerChanged) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerToPlayChangedMessage(playerChanged));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating player to play changed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client which player must place next.
     *
     * @param playerChanged the player who must place.
     */
    @Override
    public void playerToPlaceChanged(PlayerDTO playerChanged) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerToPlaceChangedMessage(playerChanged));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating player to place changed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that the game phase has changed.
     *
     * @param gamePhase the new game phase.
     */
    @Override
    public void gamePhaseChanged(GAME_PHASE gamePhase) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new GamePhaseChangedMessage(gamePhase));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating game phase changed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that the era has changed.
     *
     * @param newEra the new era.
     */
    @Override
    public void eraChanged(ERA newEra) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new EraChangedMessage(newEra));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating era changed");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that a new player joined the lobby.
     *
     * @param playerAdded the player who joined.
     */
    @Override
    public void playerAdded(PlayerDTO playerAdded) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerAddedMessage(playerAdded));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating player added");
            e.printStackTrace();
        }
    }

    /**
     * Sends the list of game winners to the client.
     *
     * @param playerDTOSWinner the winning players.
     */
    @Override
    public void gameWinners(List<PlayerDTO> playerDTOSWinner) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new GameWinnersMessage(playerDTOSWinner));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating game winners");
            e.printStackTrace();
        }
    }

    /**
     * Sends the initial game state to the client.
     *
     * @param currentEra    the starting era.
     * @param gamePhase     the starting game phase.
     * @param PlayerToPlace the first player to place.
     * @param PlayerToPlay  the first player to play.
     */
    @Override
    public void initializeGame(ERA currentEra, GAME_PHASE gamePhase, String PlayerToPlace, String PlayerToPlay) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new InitializeGameMessage(currentEra, gamePhase, PlayerToPlace, PlayerToPlay));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating initialize game");
            e.printStackTrace();
        }
    }

    /**
     * Sends an error message to the client.
     *
     * @param errorMessage a human-readable description of the error.
     */
    @Override
    public void showErrorMessage(String errorMessage) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new it.polimi.ingsw.am25.client.webLayer.Socket.messages.ErrorMessage(errorMessage));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error sending error message to client");
            e.printStackTrace();
        }
    }

    @Override
    public void eventResolved(int eventID, EVENT_TYPE eventType) {
        try {
            synchronized (out) {
                out.writeObject(new ResolvedEventMessage(eventID, eventType));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating event resolved");
            e.printStackTrace();
        }
    }

    public void sendRank(Map<Integer, List<String>> leaderboards) throws
            RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new SendRankMessage(leaderboards));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error sending rank to client");
            e.printStackTrace();
        }
    }

    /**
     * Notifies the client that a previously-disconnected player has reconnected.
     *
     * @param nickname the nickname of the reconnected player.
     */
    @Override
    public void playerReconnected(String nickname) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerReconnectedMessage(nickname));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error sending playerReconnected for '" + nickname + "'");
        }
    }

    /**
     * Notifies the client that a player has disconnected from the game.
     *
     * @param nickname the nickname of the disconnected player.
     */
    @Override
    public void playerDisconnected(String nickname) throws RemoteException {
        try {
            synchronized (out) {
                out.writeObject(new PlayerDisconnectedMessage(nickname));
                out.flush();
                out.reset();
            }
        } catch (java.io.IOException e) {
            UtilitiesFunction.logError(LOG_PREFIX, "Error sending playerDisconnected for '" + nickname + "'");
        }
    }
}