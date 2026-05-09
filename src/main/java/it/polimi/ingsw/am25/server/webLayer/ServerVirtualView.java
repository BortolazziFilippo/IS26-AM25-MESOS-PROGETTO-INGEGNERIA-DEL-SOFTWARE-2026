package it.polimi.ingsw.am25.server.webLayer;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Observers.BoardObserver;
import it.polimi.ingsw.am25.server.model.Observers.GameObserver;
import it.polimi.ingsw.am25.server.model.Observers.MarketObserver;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Player.Totem;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server-side per-client view. Listens to all model observers and forwards each event
 * to the client's {@link ClientRemoteInterface} stub (RMI or Socket proxy).
 * Maintains a local snapshot of the game state so it can send full DTOs when needed.
 */
public class ServerVirtualView implements BoardObserver, GameObserver, MarketObserver, PlayerObserver {
    private static final String LOG_PREFIX = "[SERVER][VIEW]";
    private final String nickname;
    private final ClientRemoteInterface clientStub;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /** Called when an RMI error is detected in a notification task, triggering disconnection. */
    private final Runnable disconnectCallback;
    // --- HEARTBEAT ---
    private final AtomicInteger missedPings = new AtomicInteger(0);
    private volatile boolean connected = true;
    //_________________________________________________________________________________________
    private List<PlayerDTO> winners;
    private ERA currentEra;
    private GAME_PHASE currentGamePhase;
    private String playerToPlace;
    private String playerToPlay;
    //_________________________________________________________________________________________
    Map<String, PlayerDTO> playersMap = new HashMap<>();
    //_________________________________________________________________________________________
    //MARKET DTO
    private List<CardDTO> topCards;
    private List<CardDTO> bottomCards;
    private List<BuildingDTO> topBuildings;
    private List<BuildingDTO> bottomBuildings;
    private List<CardDTO> extraDrawSnapshotCards = new ArrayList<>();
    private List<BuildingDTO> extraDrawSnapshotBuildings = new ArrayList<>();
    //_________________________________________________________________________________________
    //BOARD DTO
    private List<OffertileDTO> offerTileList;
    private List<DefaultTileDTO> defaultTileList;

    /**
     * Creates a new server virtual view instance.
     * @param clientStub         the RMI stub or Socket proxy to push notifications to.
     * @param nickname           the nickname of the player this view represents.
     * @param disconnectCallback called when an RMI error is detected, to trigger disconnection handling.
     */
    public ServerVirtualView(ClientRemoteInterface clientStub, String nickname, Runnable disconnectCallback) {
        this.clientStub = clientStub;
        this.nickname = nickname;
        this.disconnectCallback = disconnectCallback;
    }

    // --- HEARTBEAT API ---

    public String getNickname() { return nickname; }

    public ClientRemoteInterface getClientStub() { return clientStub; }

    public boolean isConnected() { return connected; }

    public void receivePing() { missedPings.set(0); }

    public int incrementMissedPings() { return missedPings.incrementAndGet(); }

    public void markDisconnected() {
        connected = false;
        executor.shutdownNow();
    }

    public void notifyPlayerDisconnected(String disconnectedNickname) {
        if (!connected) return;
        submitTask(() -> clientStub.playerDisconnected(disconnectedNickname));
    }

    @Override
    public void notifyPPChanged(String nickname, int newPP) {
        PlayerDTO pl = playersMap.get(nickname);
        pl.setPrestigePoint(newPP);
        playersMap.put(nickname, pl);
        submitTask(() -> clientStub.playerUpdatePP(nickname, newPP));
    }

    @Override
    public void notifyFoodChanged(String nickname, int newFood) {
        PlayerDTO pl = playersMap.get(nickname);
        pl.setFood(newFood);
        playersMap.put(nickname, pl);
        submitTask(() -> clientStub.playerUpdateFood(nickname, newFood));
    }

    public void forceInitialPlayersSync(List<PlayerDTO> allPlayers) {
        for (PlayerDTO player : allPlayers) {
            this.playersMap.put(player.getNickName(), player);
            try {
                clientStub.playerAdded(player);
            } catch (Exception e) {
                logServerError("Failed to sync player '" + nickname + "'");
            }
        }
    }

    @Override
    @Deprecated
    public void onPlayerChanged(String nickname, Totem totem, int food, int prestigePoint, List<Card> tribe, List<BuildingCard> buildingCards) {
        playersMap.put(nickname, new PlayerDTO(nickname, food, prestigePoint, totem.color()));
    }

    @Override
    public void onMarketChanged(List<Card> topCards, List<Card> bottomCards, List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {
        this.topCards = new ArrayList<>(topCards.stream().map(Card::toDTO).toList());
        this.bottomCards = new ArrayList<>(bottomCards.stream().map(Card::toDTO).toList());
        this.topBuildings = new ArrayList<>(topBuildings.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
        submitTask(() -> clientStub.initializeMarket(this.topCards, this.bottomCards, this.topBuildings));
    }

    @Override
    public void onTopCardRefreshed(List<Card> topCards) {
        if (this.topCards != null) {
            this.bottomCards = new ArrayList<>(this.topCards);
        }
        this.topCards = new ArrayList<>(topCards.stream().map(Card::toDTO).toList());
        submitTask(() -> clientStub.topCardRefreshed(new ArrayList<>(this.topCards)));
    }

    @Override
    public void onBoardChanged(List<OfferTile> offerTileList, List<DefaultTile> defaultTileList) {
        this.offerTileList = offerTileList.stream().map(OffertileDTO::new).toList();
        this.defaultTileList = defaultTileList.stream().map(DefaultTileDTO::new).toList();
        submitTask(() -> clientStub.boardInitialize(this.offerTileList, this.defaultTileList));
    }

    @Override
    public void playerToDefaultTile(List<Player> playerOrder) {
        List<PlayerDTO> order = playerOrder.stream().map(PlayerDTO::new).toList();
        submitTask(() -> clientStub.orderOnDefaultTile(order));
    }

    @Override
    public void playerPlacedOnOffertile(Player player, int tilePosition) {
        submitTask(() -> clientStub.playerPlacedOnOffertile(player.getNickname(), tilePosition));
    }

    @Override
    public void gameWinners(List<Player> winners) {
        this.winners = winners.stream().map(PlayerDTO::new).toList();
        submitTask(() -> clientStub.gameWinners(this.winners));
    }

    @Override
    public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase, Player playerToPlace, Player playerToPlay) {
        this.currentEra = currentEra;
        this.currentGamePhase = gamePhase;
        this.playerToPlace = null;
        this.playerToPlay = null;
        submitTask(() -> clientStub.initializeGame(this.currentEra, this.currentGamePhase, null, null));
    }

    @Override
    public void onPlayerAdded(Player playerAdded) {
        PlayerDTO player = new PlayerDTO(playerAdded);
        playersMap.put(playerAdded.getNickname(), player);
        submitTask(() -> clientStub.playerAdded(player));
    }

    @Override
    public void onEraChanged(ERA currentEra) {
        this.currentEra = currentEra;
        submitTask(() -> clientStub.eraChanged(this.currentEra));
    }

    @Override
    public void onGamePhaseChanged(GAME_PHASE gamePhase) {
        this.currentGamePhase = gamePhase;
        submitTask(() -> clientStub.gamePhaseChanged(currentGamePhase));
    }

    @Override
    public void onPlayerToPlaceChanged(Player newPlayerToPlace) {
        this.playerToPlay = null;
        this.playerToPlace = newPlayerToPlace.getNickname();
        submitTask(() -> clientStub.playerToPlaceChanged(new PlayerDTO(newPlayerToPlace)));
    }

    @Override
    public void onPlayerToPlayChanged(Player newPlayerToPlay) {
        this.playerToPlace = null;
        this.playerToPlay = newPlayerToPlay.getNickname();
        submitTask(() -> clientStub.playerToPlayChanged(new PlayerDTO(newPlayerToPlay)));
    }

    @Override
    public void onExtraDrawSnapshotReady(List<Card> snapshotCards, List<BuildingCard> snapshotBuildings) {
        this.extraDrawSnapshotCards = new ArrayList<>(snapshotCards.stream().map(Card::toDTO).toList());
        this.extraDrawSnapshotBuildings = new ArrayList<>(snapshotBuildings.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
    }

    @Override
    public void onTopBuildingRefreshed(List<BuildingCard> topBuildingCards) {
        if (this.topBuildings != null) {
            this.bottomBuildings = new ArrayList<>(this.topBuildings);
        }
        this.topBuildings = new ArrayList<>(topBuildingCards.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
        submitTask(() -> clientStub.topBuildingRefreshed(this.topBuildings));
    }

    @Override
    public void onCardRemovedFromTop(int position, CARD_TYPE cardType) {
        if (cardType == CARD_TYPE.BUILDING) {
            this.topBuildings.remove(position);
            submitTask(() -> clientStub.topBuildRemoved(position));
        } else {
            this.topCards.remove(position);
            submitTask(() -> clientStub.topCardRemoved(position));
        }
    }

    @Override
    public void onCardRemovedFromBottom(int position, CARD_TYPE cardType) {
        if (cardType == CARD_TYPE.BUILDING) {
            this.bottomBuildings.remove(position);
            submitTask(() -> clientStub.bottomBuildRemoved(position));
        } else {
            this.bottomCards.remove(position);
            submitTask(() -> clientStub.bottomCardRemoved(position));
        }
    }

    @Override
    public void notifyCardAddedToTribe(String playername, Card cardAdded) {
        submitTask(() -> clientStub.addedCardToTribe(playername, cardAdded.toDTO()));
    }

    @Override
    public void requestExtraDraw(String nickname) {
        if (!this.nickname.equals(nickname)) return;
        List<CardDTO> cards = new ArrayList<>(extraDrawSnapshotCards);
        List<BuildingDTO> buildings = new ArrayList<>(extraDrawSnapshotBuildings);
        submitTask(() -> clientStub.askExtraDraw(cards, buildings));
    }

    @Override
    public void actionOfferTileChanged(int drawTop, int drawBottom) {
        submitTask(() -> clientStub.actionAvailableChanged(new ActionDTO(drawTop, drawBottom)));
    }

    /**
     * Submits a notification task. If the client's RMI call throws {@link RemoteException},
     * the error is logged and the disconnect callback is invoked to trigger proper
     * disconnection handling. Tasks submitted after {@link #markDisconnected()} are silently
     * discarded.
     */
    private void submitTask(ThrowingRunnable task) {
        if (!connected) return;
        try {
            executor.submit(() -> {
                try {
                    task.run();
                } catch (RemoteException e) {
                    logServerError("RMI error for '" + nickname + "': " + e.getMessage() + " — triggering disconnection.");
                    disconnectCallback.run();
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
            // Executor was already shut down (player disconnected) — nothing to do.
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws RemoteException;
    }

    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }

    @Override
    public void eventSolved(int eventID, EVENT_TYPE eventType) {
        submitTask(() -> clientStub.eventResolved(eventID, eventType));
    }
}
