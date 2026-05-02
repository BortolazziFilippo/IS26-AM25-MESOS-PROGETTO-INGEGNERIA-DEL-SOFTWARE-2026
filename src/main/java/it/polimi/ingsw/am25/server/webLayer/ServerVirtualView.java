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
    // --- HEARTBEAT ---
    /** Counter of consecutive pings missed by this client. Reset to 0 on each ping received. */
    private final AtomicInteger missedPings = new AtomicInteger(0);
    /** {@code false} once the player has been declared disconnected; stops further notifications. */
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
     * @param clientStub parameter clientStub.
     * @param nickname parameter nickname.
     */
    public ServerVirtualView(ClientRemoteInterface clientStub, String nickname) {
        this.clientStub = clientStub;
        this.nickname = nickname;
    }

    // --- HEARTBEAT API ---

    /** Returns the nickname of the player this view represents. */
    public String getNickname() {
        return nickname;
    }

    /** Returns the client stub (RMI or Socket proxy) this view writes to. */
    public ClientRemoteInterface getClientStub() {
        return clientStub;
    }

    /** Returns {@code true} while this player is considered connected. */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Called when a ping is received from this player.
     * Resets the missed-ping counter to zero.
     */
    public void receivePing() {
        missedPings.set(0);
    }

    /**
     * Called by the watchdog every tick to count one more missed heartbeat.
     * @return the new missed-ping count after incrementing.
     */
    public int incrementMissedPings() {
        return missedPings.incrementAndGet();
    }

    /**
     * Marks this view as disconnected and shuts down the executor so no further
     * notifications are sent to a dead client.
     */
    public void markDisconnected() {
        connected = false;
        executor.shutdownNow();
    }

    /**
     * Sends a {@code playerDisconnected} notification to the client represented by
     * this view, informing it that the given player has left the game.
     * Uses the executor so the message is FIFO-ordered with other notifications.
     * @param disconnectedNickname the nickname of the player who disconnected.
     */
    public void notifyPlayerDisconnected(String disconnectedNickname) {
        if (!connected) return;
        submitTask(() -> {
            try {
                clientStub.playerDisconnected(disconnectedNickname);
            } catch (RemoteException e) {
                logServerError("Failed to notify playerDisconnected('" + disconnectedNickname + "') to '" + nickname + "'");
            }
        });
    }

    /**
     * Executes notify ppchanged.
     * @param nickname parameter nickname.
     * @param newPP parameter newPP.
     */
    @Override
    public void notifyPPChanged(String nickname, int newPP) {
        PlayerDTO pl = playersMap.get(nickname);
        pl.setPrestigePoint(newPP);
        playersMap.put(nickname, pl);
        submitTask(() -> {
            try {
                clientStub.playerUpdatePP(nickname, newPP);
            } catch (java.rmi.RemoteException e) {
                throw new RuntimeException(e);
            }
        });


    }

    /**
     * Executes notify food changed.
     * @param nickname parameter nickname.
     * @param newFood parameter newFood.
     */
    @Override
    public void notifyFoodChanged(String nickname, int newFood) {
        PlayerDTO pl = playersMap.get(nickname);
        pl.setFood(newFood);
        playersMap.put(nickname, pl);
        submitTask(() -> {
            try {
                clientStub.playerUpdateFood(nickname, newFood);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to update food for player '" + nickname + "'");
            }
        });

    }

    /**
     * Executes force initial players sync.
     * @param allPlayers parameter allPlayers.
     */
    public void forceInitialPlayersSync(List<PlayerDTO> allPlayers) {
        for (PlayerDTO player : allPlayers) {
            // Keep the local virtual-view cache aligned with the current player snapshot.
            this.playersMap.put(player.getNickName(), player);
            // Push the player snapshot to the client.
            try {
                clientStub.playerAdded(player);
            } catch (Exception e) {
                logServerError("Failed to sync player '" + nickname + "'");
            }
        }
    }

    /**
     * Executes on player changed.
     * @param nickname parameter nickname.
     * @param totem parameter totem.
     * @param food parameter food.
     * @param prestigePoint parameter prestigePoint.
     * @param tribe parameter tribe.
     * @param buildingCards parameter buildingCards.
     */
    @Override
    @Deprecated
    public void onPlayerChanged(String nickname, Totem totem, int food, int prestigePoint, List<Card> tribe, List<BuildingCard> buildingCards) {
        playersMap.put(nickname, new PlayerDTO(nickname, food, prestigePoint, totem.color()));

    }

    /**
     * Executes on market changed.
     * @param topCards parameter topCards.
     * @param bottomCards parameter bottomCards.
     * @param topBuildings parameter topBuildings.
     * @param bottomBuildings parameter bottomBuildings.
     */
    @Override
    public void onMarketChanged(List<Card> topCards, List<Card> bottomCards, List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {
        // FIX: Avvolgiamo tutto in new ArrayList<>() per renderli modificabili dal .remove()
        this.topCards = new ArrayList<>(topCards.stream().map(Card::toDTO).toList());
        this.bottomCards = new ArrayList<>(bottomCards.stream().map(Card::toDTO).toList());
        this.topBuildings = new ArrayList<>(topBuildings.stream().map(b -> (BuildingDTO) b.toDTO()).toList());

        submitTask(() -> {
            try {
                clientStub.initializeMarket(this.topCards, this.bottomCards, this.topBuildings);
            } catch (RemoteException e) {
                logServerError("Connection error: initializeMarket");
            }
        });


    }

    /**
     * Executes on top card refreshed.
     * @param topCards parameter topCards.
     */
    @Override
    public void onTopCardRefreshed(List<Card> topCards) {
        if (this.topCards != null) {
            this.bottomCards = new ArrayList<>(this.topCards);
        }
        this.topCards = new ArrayList<>(topCards.stream().map(Card::toDTO).toList());
        submitTask(() -> {
            try {
                clientStub.topCardRefreshed(new ArrayList<>(this.topCards));
            } catch (RemoteException e) {
                logServerError("Connection error: topCardRefreshed");
            }
        });
    }

    /**
     * Executes on board changed.
     * @param offerTileList parameter offerTileList.
     * @param defaultTileList parameter defaultTileList.
     */
    @Override
    public void onBoardChanged(List<OfferTile> offerTileList, List<DefaultTile> defaultTileList) {
        this.offerTileList = offerTileList.stream().map(OffertileDTO::new).toList();
        this.defaultTileList = defaultTileList.stream().map(DefaultTileDTO::new).toList();
        submitTask(() -> {
            try {
                clientStub.boardInitialize(this.offerTileList, this.defaultTileList);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to sync board for player '" + nickname + "'");
            }
        });
    }

    /**
     * Executes player to default tile.
     * @param playerOrder parameter playerOrder.
     */
    @Override
    public void playerToDefaultTile(List<Player> playerOrder) {
        List<PlayerDTO> order = playerOrder.stream().map(PlayerDTO::new).toList();
        submitTask(() -> {
            try {
                clientStub.orderOnDefaultTile(order);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to send default tile order for player '" + nickname + "'");
            }
        });

    }

    /**
     * Executes player placed on offertile.
     * @param player parameter player.
     * @param tilePosition parameter tilePosition.
     */
    @Override
    public void playerPlacedOnOffertile(Player player, int tilePosition) {
        submitTask(() -> {
            try {
                clientStub.playerPlacedOnOffertile(player.getNickname(), tilePosition);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify player placed on offer tile for player '" + nickname + "'");
            }
        });


    }

    /**
     * Executes game winners.
     * @param winners parameter winners.
     */
    @Override
    public void gameWinners(List<Player> winners) {
        this.winners = winners.stream().map(PlayerDTO::new).toList();

        submitTask(() -> {
            try {
                clientStub.gameWinners(this.winners);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify game winners");
            }
        });


    }

    /**
     * Executes on game changed.
     * @param currentEra parameter currentEra.
     * @param players parameter players.
     * @param gamePhase parameter gamePhase.
     * @param playerToPlace parameter playerToPlace.
     * @param playerToPlay parameter playerToPlay.
     */
    @Override
    public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase, Player playerToPlace, Player playerToPlay) {
        this.currentEra = currentEra;
        this.currentGamePhase = gamePhase;
        this.playerToPlace = null;
        this.playerToPlay = null;
        submitTask(() -> {
            try {
                clientStub.initializeGame(this.currentEra, this.currentGamePhase, null, null);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to send game initialization for player '" + nickname + "'");
            }
        });

    }

    /**
     * Executes on player added.
     * @param playerAdded parameter playerAdded.
     */
    @Override
    public void onPlayerAdded(Player playerAdded) {
        PlayerDTO player = new PlayerDTO(playerAdded);
        playersMap.put(playerAdded.getNickname(), player);
        submitTask(()->{
            try {
                clientStub.playerAdded(player);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify player added for player '" + nickname + "'");
            }
        });


    }

    /**
     * Executes on era changed.
     * @param currentEra parameter currentEra.
     */
    @Override
    public void onEraChanged(ERA currentEra) {
        this.currentEra = currentEra;
        submitTask(()->{
            try {
                clientStub.eraChanged(this.currentEra);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify era change for player '" + nickname + "'");
            }
        });


    }

    /**
     * Executes on game phase changed.
     * @param gamePhase parameter gamePhase.
     */
    @Override
    public void onGamePhaseChanged(GAME_PHASE gamePhase) {
        this.currentGamePhase = gamePhase;
        // Sempre asincrono tramite il single-thread executor: garantisce ordine
        // FIFO con tutte le altre notifiche (eventResolved, playerToPlayChanged,
        // playerToPlaceChanged, ecc.) ed evita race condition dovute a chiamate
        // RMI emesse da thread diversi del server.
        submitTask(() -> {
            try {
                clientStub.gamePhaseChanged(currentGamePhase);
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify game phase change for player '" + nickname + "'");
            }
        });
    }

    /**
     * Executes on player to place changed.
     * @param newPlayerToPlace parameter newPlayerToPlace.
     */
    @Override
    public void onPlayerToPlaceChanged(Player newPlayerToPlace) {
        this.playerToPlay = null;
        this.playerToPlace = newPlayerToPlace.getNickname();
        submitTask(()->{
            try {
                clientStub.playerToPlaceChanged(new PlayerDTO(newPlayerToPlace));
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify player-to-place change for player '" + nickname + "'");
            }
        });

    }

    /**
     * Executes on player to play changed.
     * @param newPlayerToPlay parameter newPlayerToPlay.
     */
    @Override
    public void onPlayerToPlayChanged(Player newPlayerToPlay) {
        this.playerToPlace = null;
        this.playerToPlay = newPlayerToPlay.getNickname();
        submitTask(()->{
            try {
                clientStub.playerToPlayChanged(new PlayerDTO(newPlayerToPlay));
            } catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify player-to-play change for player '" + nickname + "'");
            }
        });


    }

    /**
     * Stores the end-of-round market snapshot so it can be forwarded to the client
     * together with the {@code askExtraDraw} notification.
     */
    @Override
    public void onExtraDrawSnapshotReady(List<Card> snapshotCards, List<BuildingCard> snapshotBuildings) {
        this.extraDrawSnapshotCards = new ArrayList<>(snapshotCards.stream().map(Card::toDTO).toList());
        this.extraDrawSnapshotBuildings = new ArrayList<>(snapshotBuildings.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
    }

    /**
     * Executes on top building refreshed.
     * @param topBuildingCards parameter topBuildingCards.
     */
    @Override
    public void onTopBuildingRefreshed(List<BuildingCard> topBuildingCards) {
        if (this.topBuildings != null) {
            this.bottomBuildings = new ArrayList<>(this.topBuildings);
        }

        // FIX: Cast esplicito a BuildingDTO aggiunto!
        this.topBuildings = new ArrayList<>(topBuildingCards.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
        submitTask(()->{
            try {
                clientStub.topBuildingRefreshed(this.topBuildings);
            } catch (RemoteException e) {
                logServerError("Connection error: topBuildingRefreshed");
            }
        });

    }

    /**
     * Executes on card removed from top.
     * @param position parameter position.
     * @param cardType parameter cardType.
     */
    @Override
    public void onCardRemovedFromTop(int position, CARD_TYPE cardType) {
        if (cardType == CARD_TYPE.BUILDING) {
            this.topBuildings.remove(position);
            submitTask(()->{
                try {
                    clientStub.topBuildRemoved(position);
                } catch (java.rmi.RemoteException e) {
                    logServerError("Failed to notify top building removed at position " + position + " for player '" + nickname + "'");
                }
            });


        } else {
            this.topCards.remove(position);
            submitTask(()->{
                try {
                    clientStub.topCardRemoved(position);
                } catch (java.rmi.RemoteException e) {
                    logServerError("Failed to notify top card removed at position " + position + " for player '" + nickname + "'");
                }
            });

        }

    }

    /**
     * Executes on card removed from bottom.
     * @param position parameter position.
     * @param cardType parameter cardType.
     */
    @Override
    public void onCardRemovedFromBottom(int position, CARD_TYPE cardType) {
        if (cardType == CARD_TYPE.BUILDING) {
            this.bottomBuildings.remove(position);
            submitTask(()->{
                try {
                    clientStub.bottomBuildRemoved(position);
                } catch (java.rmi.RemoteException e) {
                    logServerError("Failed to notify bottom building removed at position " + position + " for player '" + nickname + "'");
                }
            });

        } else {
            this.bottomCards.remove(position);
            submitTask(()->{
                try {
                    clientStub.bottomCardRemoved(position);
                } catch (java.rmi.RemoteException e) {
                    logServerError("Failed to notify bottom card removed at position " + position + " for player '" + nickname + "'");
                }
            });
        }
    }

    /**
     * Executes notify card added to tribe.
     * @param playername parameter playername.
     * @param cardAdded parameter cardAdded.
     */
    @Override
    public void notifyCardAddedToTribe(String playername, Card cardAdded) {
        submitTask(()->{
            try {
                clientStub.addedCardToTribe(playername, cardAdded.toDTO());
            } catch (RemoteException e) {
                logServerError("Failed to notify card added to tribe for player '" + playername + "'");
            }
        });


    }


    /**
     * Executes request extra draw.
     * Only notifies the client that owns this view (i.e. whose nickname matches).
     * Does not block: the client will respond asynchronously via selectExtraCard or skipExtraDraw.
     * @param nickname parameter nickname.
     */
    @Override
    public void requestExtraDraw(String nickname) {
        if (!this.nickname.equals(nickname)) {
            return;
        }
        List<CardDTO> cards = new ArrayList<>(extraDrawSnapshotCards);
        List<BuildingDTO> buildings = new ArrayList<>(extraDrawSnapshotBuildings);
        submitTask(() -> {
            try {
                clientStub.askExtraDraw(cards, buildings);
            } catch (RemoteException e) {
                logServerError("Client disconnected during extra draw request.");
            }
        });
    }

    /**
     * Executes action offer tile changed.
     * @param drawTop parameter drawTop.
     * @param drawBottom parameter drawBottom.
     */
    @Override
    public void actionOfferTileChanged(int drawTop, int drawBottom) {
        submitTask(()->{
            try {
                clientStub.actionAvailableChanged(new ActionDTO(drawTop, drawBottom));
            } catch (RemoteException e) {
                logServerError("Error comunicating offertile changed");
                UtilitiesFunction.logError(LOG_PREFIX, "Error comunicating offertile changed");
            }
        });


    }

    /**
     * Submits a notification task to the single-thread executor.
     * If the executor has already been shut down (player disconnected) the task is
     * silently discarded — the client is gone and no notification is needed.
     * This prevents {@link java.util.concurrent.RejectedExecutionException} from
     * propagating up to the model when a disconnected player's view still receives
     * model events (e.g. playerToPlaceChanged fired right after markDisconnected()).
     *
     * @param task the notification runnable to enqueue.
     */
    private void submitTask(Runnable task) {
        if (!connected) return;
        try {
            executor.submit(task);
        } catch (java.util.concurrent.RejectedExecutionException e) {
            // Executor was already shut down (player disconnected) — nothing to do.
        }
    }

    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }

    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }

    @Override
    public void eventSolved (int eventID, EVENT_TYPE eventType) {
        final String description = "Evento #" + eventID + " (" + eventType + ") risolto";
        // Passa attraverso il single-thread executor per preservare l'ordine FIFO
        // rispetto a notifyPP/Food, gamePhaseChanged, playerToPlay/PlaceChanged.
        // In precedenza era sincrono e bypassava la coda, generando una race
        // condition con i task asincroni già in coda.
        submitTask(() -> {
            try {
                clientStub.eventResolved(description);
            } catch (RemoteException e) {
                logServerError("Failed to notify event resolved: " + description);
            }
        });
    }
}
