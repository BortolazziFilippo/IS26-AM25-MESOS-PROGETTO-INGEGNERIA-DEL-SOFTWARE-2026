package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.Utilities.ClientUtilitiesFunction;
import it.polimi.ingsw.am25.client.webLayer.PongWatchdog;
import it.polimi.ingsw.am25.client.GUI.GUIObserver;
import it.polimi.ingsw.am25.client.GUI.popup.DisconnectPopup;
import it.polimi.ingsw.am25.client.GUI.popup.EventPopup;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MarketController implements GUIObserver {

    // --- data buffered when observer callbacks arrive before the FXML is ready ---
    private volatile List<CardDTO> pendingTopTribeCard;
    private volatile List<BuildingDTO> pendingTopBuildingCard;
    private volatile List<CardDTO> pendingBottomTribeCard;
    private volatile List<BuildingDTO> pendingBottomBuildingCard;
    private volatile List<DefaultTileDTO> pendingDefs;
    private volatile List<OffertileDTO> pendingOffertiles;

    private final ClientVirtualView clientHandler;
    private final ServerRemoteInterface serverRemoteInterface;
    private final PlayerDTO playerDTO;
    private final EventPopup eventPopup = new EventPopup();

    private double cardFitHeight = 220;

    // --- totem positions expressed as fractions of tile width / height ---
    private static final double OFFER_TOTEM_X = 0.50;
    private static final double OFFER_TOTEM_Y = 0.27;
    /**
     * Y-fraction slots on the default tile for each possible player count.
     */
    private static final Map<Integer, double[]> DEFAULT_SLOT_Y = Map.of(
            2, new double[]{0.30, 0.48},
            3, new double[]{0.27, 0.43, 0.61},
            4, new double[]{0.21, 0.39, 0.57, 0.76},
            5, new double[]{0.15, 0.31, 0.49, 0.68, 0.86}
    );

    // --- guard flag: tiles are rendered only once per session ---
    private boolean tilesRendered = false;

    // --- tile selection state ---
    private int selectedTilePosition = -1;
    private javafx.scene.layout.StackPane selectedTilePane = null;
    /**
     * Position of the tile currently showing the local player's totem preview (-1 = none).
     */
    private int previewTilePosition = -1;
    /**
     * True while a totem preview animation is in flight; prevents re-entrant tile selection.
     */
    private boolean previewAnimating = false;

    // --- transparent overlay panes that host totem images on top of tile images ---
    private Pane defaultTileOverlay = null;
    private double defaultTileWidth = 0;
    private final Map<Integer, Pane> offerTileOverlays = new HashMap<>();
    private final Map<Integer, Double> offerTileWidths = new HashMap<>();

    // --- card selection state ---
    private ImageView selectedCardView = null;
    /**
     * Tracks active tooltips on building nodes so they can be cleanly uninstalled.
     */
    private final WeakHashMap<Node, Tooltip> activeTooltips = new WeakHashMap<>();
    /**
     * Number of tribe cards (non-building) currently in the top row.
     */
    private int topTribeCount = 0;
    /**
     * Number of tribe cards (non-building) currently in the bottom row.
     */
    private int bottomTribeCount = 0;
    /**
     * True while the player must pick an extra card from the end-of-round snapshot.
     */
    private boolean extraDrawActive = false;
    /**
     * True while a server response is pending; prevents re-submitting an action before confirmation arrives.
     */
    private boolean pendingRequest = false;
    /**
     * Number of tribe cards in the extra draw snapshot currently shown in the top row.
     */
    private int extraDrawTribeCount = 0;
    /**
     * Non-blocking banner shown while extra draw is active.
     */
    private javafx.stage.Popup extraDrawPopup;
    /**
     * UI actions deferred until the player resolves the extra draw (event popups).
     */
    private final List<Runnable> deferredUiActions = new ArrayList<>();
    /**
     * Snapshot of the bottom tribe cards captured at round-end, applied after extra draw is resolved.
     */
    private List<CardDTO> pendingBottomCards = null;
    /**
     * Snapshot of the bottom buildings captured at round-end, applied after extra draw is resolved.
     */
    private List<BuildingDTO> pendingBottomBuildings = null;
    private final DisconnectPopup disconnectPopup = new DisconnectPopup();
    private it.polimi.ingsw.am25.client.GUI.EndGameController endGameController;

    @FXML
    private HBox topCardHbox;
    @FXML
    private HBox tileHbox;
    @FXML
    private HBox bottomCardHbox;
    @FXML
    private Button selectCardButton;
    @FXML
    private Button placeTotemButton;
    @FXML
    private Button skipTurnButton;
    @FXML
    private Button playerStatusButton;
    @FXML
    private Label phaseLabel;
    @FXML
    private Label currentPlayerLabel;
    @FXML
    private Label drawTopLabel;
    @FXML
    private Label drawBotLabel;
    @FXML
    private Label prestigePointLabel;
    @FXML
    private Label foodLabel;
    @FXML
    private Label shamanStarLabel;
    @FXML
    private Label builderDiscountLabel;
    @FXML
    private HBox headerHBox;
    @FXML
    private javafx.scene.control.SplitPane splitPane;

    /**
     * Creates a new market controller and registers this observer with the client view.
     *
     * @param clientHandler          the local client view that receives notifications from the server.
     * @param serverRemoteInterface  the remote server interface used to send game actions.
     * @param playerDTO              the local player DTO associated with this session.
     */
    public MarketController(ClientVirtualView clientHandler, ServerRemoteInterface serverRemoteInterface, PlayerDTO playerDTO) {
        this.clientHandler = clientHandler;
        this.serverRemoteInterface = serverRemoteInterface;
        this.playerDTO = playerDTO;
        clientHandler.addGUIObserver(this);
    }

    // =========================================================
    // FXML
    // =========================================================

    /**
     * Called by FXMLLoader after all @FXML fields are injected. Flushes any pending data.
     */
    @FXML
    public void initialize() {
        double availableH = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        cardFitHeight = Math.max(100, (availableH - 80) / 3 - 30);

        // Keep SplitPane top-anchor in sync with whatever height the header actually needs.
        headerHBox.heightProperty().addListener((obs, old, newH) ->
                AnchorPane.setTopAnchor(splitPane, newH.doubleValue()));

        Thread preloader = new Thread(CardImageFactory::preloadEventImages, "event-image-preloader");
        preloader.setDaemon(true);
        preloader.start();
        startHeartbeat();
        if (placeTotemButton != null) placeTotemButton.setDisable(true);
        if (selectCardButton != null) selectCardButton.setDisable(true);
        if (skipTurnButton != null) skipTurnButton.setDisable(true);
        if (pendingOffertiles != null) {
            renderTiles(pendingOffertiles, pendingDefs);
            pendingOffertiles = null;
            pendingDefs = null;
        }
        if (pendingTopTribeCard != null) {
            renderCards(pendingTopTribeCard, pendingBottomTribeCard, pendingTopBuildingCard, pendingBottomBuildingCard);
            pendingTopTribeCard = null;
            pendingBottomTribeCard = null;
            pendingTopBuildingCard = null;
            pendingBottomBuildingCard = null;
        }
        updatePlayerStatsLabels();
        if (foodLabel != null) foodLabel.setText("Cibo: " + playerDTO.getFood());
        if (prestigePointLabel != null) prestigePointLabel.setText("Punti Prestigio: " + 0);
        updateInteractionState();
    }

    /**
     * Sends the selected card to the server (top or bottom row, tribe or building).
     */
    @FXML
    private void handleSelectCard() {
        if (selectedCardView == null || pendingRequest) return;
        try {
            CardDTO dto = (CardDTO) selectedCardView.getUserData();
            CARD_TYPE type = dto.getCardType();
            if (extraDrawActive) {
                int idx = topCardHbox.getChildren().indexOf(selectedCardView);
                int pos = idx < extraDrawTribeCount ? idx : idx - extraDrawTribeCount;
                pendingRequest = true;
                serverRemoteInterface.selectExtraCard(playerDTO, type, pos);
                pendingRequest = false;
                clearCardSelection();
                clearExtraDraw();
            } else if (topCardHbox.getChildren().contains(selectedCardView)) {
                int idx = topCardHbox.getChildren().indexOf(selectedCardView);
                int pos = idx < topTribeCount ? idx : idx - topTribeCount;
                pendingRequest = true;
                serverRemoteInterface.selectCardFromTopList(playerDTO, type, pos);
                clearCardSelection();
            } else {
                int idx = bottomCardHbox.getChildren().indexOf(selectedCardView);
                int pos = idx < bottomTribeCount ? idx : idx - bottomTribeCount;
                pendingRequest = true;
                serverRemoteInterface.selectCardFromBottomList(playerDTO, type, pos);
                clearCardSelection();
            }
        } catch (Exception e) {
            pendingRequest = false;
            updateInteractionState();
            GUIEffects.showError(e.getMessage());
        }
    }

    /**
     * Sends the totem placement request to the server for the currently selected tile.
     */
    @FXML
    private void handlePlaceTotem() {
        if (selectedTilePosition == -1 || pendingRequest) return;
        try {
            pendingRequest = true;
            placeTotemButton.setDisable(true);
            serverRemoteInterface.placingPlayer(playerDTO, selectedTilePosition);
        } catch (Exception e) {
            pendingRequest = false;
            updateInteractionState();
            GUIEffects.showError(e.getMessage());
        }
    }

    /**
     * Notifies the server that the local player passes (or skips the extra draw).
     */
    @FXML
    private void handleSkipTurn() {
        if (pendingRequest) return;
        try {
            pendingRequest = true;
            if (extraDrawActive) {
                serverRemoteInterface.skipExtraDraw(playerDTO);
                pendingRequest = false;
                clearExtraDraw();
            } else {
                serverRemoteInterface.playerDoNothing(playerDTO);
            }
        } catch (Exception e) {
            pendingRequest = false;
            updateInteractionState();
            GUIEffects.showError(e.getMessage());
        }
    }

    @FXML
    private void showPlayerStatus() {
        try {
            PlayerStatusController controller = new PlayerStatusController();
            controller.init(clientHandler, playerDTO);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PlayerStatus.fxml"));
            loader.setController(controller);
            Parent root = loader.load();

            Stage stage = new Stage();
            GUIEffects.applyIcon(stage);
            stage.setTitle("Stato dei giocatori");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> controller.unregister());
            stage.show();
        } catch (Exception e) {
            GUIEffects.showError("Impossibile aprire lo stato giocatori: " + e.getMessage());
        }
    }

    // =========================================================
    // OBSERVER
    // =========================================================

    /**
     * Called when the game phase changes. Updates the phase label
     * and clears totems from offer tiles at the start of each placing phase.
     *
     * @param phase the new game phase.
     */
    @Override
    public void onGamePhaseChanged(GAME_PHASE phase) {
        Platform.runLater(() -> {
            if (phaseLabel != null) phaseLabel.setText("Fase corrente: " + phase);
            if (phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                offerTileOverlays.values().forEach(p -> p.getChildren().clear());
            }
            requestInteractionUpdate();
        });
    }

    /**
     * Called when the active player for the placing phase changes.
     * Updates the current-player label and recalculates the interaction state.
     *
     * @param nickname the nickname of the player who must now place their totem.
     */
    @Override
    public void onPlayerToPlaceChanged(String nickname) {
        Platform.runLater(() -> {
            pendingRequest = false;
            if (currentPlayerLabel != null) currentPlayerLabel.setText("Giocatore di turno: " + nickname);
            if (playerDTO.getNickName().equals(nickname)) showTurnNotification();
            requestInteractionUpdate();
        });
    }

    /**
     * Called when the active player for the action phase (card selection) changes.
     * Updates the current-player label and recalculates the interaction state.
     *
     * @param nickname the nickname of the player who must now perform their action.
     */
    @Override
    public void onPlayerToPlayChanged(String nickname) {
        Platform.runLater(() -> {
            pendingRequest = false;
            if (currentPlayerLabel != null) currentPlayerLabel.setText("Giocatore di turno: " + nickname);
            if (playerDTO.getNickName().equals(nickname)) showTurnNotification();
            requestInteractionUpdate();
        });
    }

    /**
     * Called when the market is initialised with the initial card lists.
     * If the FXML component is not yet ready, the data is buffered.
     *
     * @param top    initial tribe cards for the top row.
     * @param bot    initial tribe cards for the bottom row.
     * @param topBld initial buildings for the top row.
     * @param botBld initial buildings for the bottom row.
     */
    @Override
    public void onMarketInitialized(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld, List<BuildingDTO> botBld) {
        if (topCardHbox == null) {
            // FXML not ready yet — buffer the data for initialize()
            this.pendingTopTribeCard = top;
            this.pendingBottomTribeCard = bot;
            this.pendingTopBuildingCard = topBld;
            this.pendingBottomBuildingCard = botBld;
        } else {
            // Pre-build ImageViews on the calling thread to keep the FX thread free from I/O
            List<ImageView> topViews = top.stream().map(c -> CardImageFactory.cardImageView(c, cardFitHeight)).toList();
            List<ImageView> topBldViews = topBld.stream().map(b -> CardImageFactory.buildingImageView(b, cardFitHeight)).toList();
            List<ImageView> botViews = bot.stream().map(c -> CardImageFactory.cardImageView(c, cardFitHeight)).toList();
            List<ImageView> botBldViews = botBld != null ? botBld.stream().map(b -> CardImageFactory.buildingImageView(b, cardFitHeight)).toList() : List.of();
            Platform.runLater(() -> renderCardViews(topViews, botViews, topBldViews, botBldViews));
        }
    }

    /**
     * Called when a tribe card is removed from the top market row.
     * Animates the removal and updates the interaction state.
     *
     * @param position the zero-based index of the removed card in the top row.
     */
    @Override
    public void onTopCardRemoved(int position) {
        Platform.runLater(() -> {
            pendingRequest = false;
            if (topCardHbox == null || position >= topTribeCount) return;
            Node node = topCardHbox.getChildren().get(position);
            if (node == selectedCardView) clearCardSelection();
            topTribeCount--;
            // Mark as non-interactive immediately; the node is removed after the animation
            node.setMouseTransparent(true);
            animateRemove(node, topCardHbox, this::requestInteractionUpdate);
        });
    }

    /**
     * Called when a tribe card is removed from the bottom market row.
     * Animates the removal and updates the interaction state.
     *
     * @param position the zero-based index of the removed card in the bottom row.
     */
    @Override
    public void onBottomCardRemoved(int position) {
        Platform.runLater(() -> {
            pendingRequest = false;
            if (bottomCardHbox == null || position >= bottomTribeCount) return;
            Node node = bottomCardHbox.getChildren().get(position);
            if (node == selectedCardView) clearCardSelection();
            bottomTribeCount--;
            node.setMouseTransparent(true);
            animateRemove(node, bottomCardHbox, this::requestInteractionUpdate);
        });
    }

    /**
     * Called when a building is removed from the top market row.
     * Animates the removal of the corresponding node in the top HBox.
     *
     * @param position the zero-based index of the removed building among the top-row buildings.
     */
    @Override
    public void onTopBuildRemoved(int position) {
        Platform.runLater(() -> {
            pendingRequest = false;
            if (topCardHbox == null) return;
            int idx = topTribeCount + position;
            if (idx >= topCardHbox.getChildren().size()) return;
            Node node = topCardHbox.getChildren().get(idx);
            if (node == selectedCardView) clearCardSelection();
            node.setMouseTransparent(true);
            animateRemove(node, topCardHbox, null);
        });
    }

    /**
     * Called when a building is removed from the bottom market row.
     * Animates the removal of the corresponding node in the bottom HBox.
     *
     * @param position the zero-based index of the removed building among the bottom-row buildings.
     */
    @Override
    public void onBottomBuildRemoved(int position) {
        Platform.runLater(() -> {
            pendingRequest = false;
            if (bottomCardHbox == null) return;
            int idx = bottomTribeCount + position;
            if (idx >= bottomCardHbox.getChildren().size()) return;
            Node node = bottomCardHbox.getChildren().get(idx);
            if (node == selectedCardView) clearCardSelection();
            node.setMouseTransparent(true);
            animateRemove(node, bottomCardHbox, null);
        });
    }

    /**
     * Called at end of round when the top-row tribe cards are refreshed.
     * Old cards fly downward with an animation before the new ones appear.
     * If an extra-draw is active, the bottom-row update is deferred.
     *
     * @param top the new list of tribe cards for the top row.
     */
    @Override
    public void onTopCardRefreshed(List<CardDTO> top) {
        // Snapshot the bottom list now, before any scene-graph changes
        List<CardDTO> bottomSnap = new ArrayList<>(clientHandler.getBottomCards());
        // Pre-build ImageViews on the calling thread to keep the FX thread free from I/O
        List<ImageView> topViews = top.stream().map(c -> CardImageFactory.cardImageView(c, cardFitHeight)).toList();
        List<ImageView> botViews = bottomSnap.stream().map(c -> CardImageFactory.cardImageView(c, cardFitHeight)).toList();
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            clearCardSelection();
            if (extraDrawActive) {
                topTribeCount = top.size();
                pendingBottomCards = bottomSnap; // captured before any concurrent modification
                return;
            }
            javafx.scene.Scene scene = topCardHbox.getScene();
            if (scene == null) {
                doTopCardRefreshViews(topViews, botViews);
                return;
            }
            Pane root = (Pane) scene.getRoot();

            // Collect all visible tribe cards (no buildings, no already-fading nodes)
            List<Node> topTribeNodes = topCardHbox.getChildren().stream()
                    .filter(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO))
                    .toList();
            List<ImageView> floaters = snapshotFloaters(root, topTribeNodes);

            // Replace top tribe cards with the pre-built views
            topCardHbox.getChildren().removeIf(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO));
            topTribeCount = 0;
            for (ImageView iv : topViews) {
                topCardHbox.getChildren().add(topTribeCount, iv);
                topTribeCount++;
            }
            // Clear old bottom tribe cards; the new ones are added after the fly animation
            bottomCardHbox.getChildren().removeIf(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO));
            bottomTribeCount = 0;
            requestInteractionUpdate();
            for (int i = 0; i < topTribeCount; i++) fadeInNode(topCardHbox.getChildren().get(i));

            flyToBottomThenFadeIn(root, floaters, () -> {
                for (int i = 0; i < botViews.size(); i++) {
                    bottomCardHbox.getChildren().add(i, botViews.get(i));
                }
                bottomTribeCount = botViews.size();
                requestInteractionUpdate();
                botViews.forEach(this::fadeInNode);
            });
        });
    }

    /**
     * Fallback for when the scene is not yet attached: replaces both rows without animations.
     */
    private void doTopCardRefreshViews(List<ImageView> topViews, List<ImageView> botViews) {
        topCardHbox.getChildren().removeIf(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO));
        topTribeCount = 0;
        for (ImageView iv : topViews) {
            topCardHbox.getChildren().add(topTribeCount, iv);
            topTribeCount++;
        }
        bottomCardHbox.getChildren().removeIf(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO));
        bottomTribeCount = 0;
        for (int i = 0; i < botViews.size(); i++) {
            bottomCardHbox.getChildren().add(i, botViews.get(i));
            bottomTribeCount++;
        }
        requestInteractionUpdate();
    }

    /**
     * Called at end of round when the top-row buildings are refreshed.
     * Old buildings fly downward with an animation before the new ones appear.
     * If an extra-draw is active, the bottom-row update is deferred.
     *
     * @param topBld the new list of buildings for the top row.
     */
    @Override
    public void onTopBuildingRefreshed(List<BuildingDTO> topBld) {
        List<BuildingDTO> botSnap = new ArrayList<>(clientHandler.getBottomBuildings());
        // Pre-build ImageViews on the calling thread to keep the FX thread free from I/O
        List<ImageView> topBldViews = topBld.stream().map(b -> CardImageFactory.buildingImageView(b, cardFitHeight)).toList();
        List<ImageView> botBldViews = botSnap.stream().map(b -> CardImageFactory.buildingImageView(b, cardFitHeight)).toList();
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            clearCardSelection();
            if (extraDrawActive) {
                pendingBottomBuildings = botSnap; // captured before any concurrent modification
                return;
            }
            javafx.scene.Scene scene = topCardHbox.getScene();
            if (scene == null) {
                doTopBuildingRefreshViews(topBldViews, botBldViews);
                return;
            }
            Pane root = (Pane) scene.getRoot();

            // Collect all visible building nodes from the top row
            List<Node> topBldNodes = topCardHbox.getChildren().stream()
                    .filter(n -> n.getUserData() instanceof BuildingDTO)
                    .toList();
            List<ImageView> floaters = snapshotFloaters(root, topBldNodes);

            // Replace top buildings with pre-built views
            if (topCardHbox.getChildren().size() > topTribeCount)
                topCardHbox.getChildren().remove(topTribeCount, topCardHbox.getChildren().size());
            topCardHbox.getChildren().addAll(topBldViews);

            // Clear old bottom buildings; the new ones are added after the fly animation
            if (bottomCardHbox.getChildren().size() > bottomTribeCount)
                bottomCardHbox.getChildren().remove(bottomTribeCount, bottomCardHbox.getChildren().size());
            requestInteractionUpdate();
            for (int i = topTribeCount; i < topCardHbox.getChildren().size(); i++)
                fadeInNode(topCardHbox.getChildren().get(i));

            flyToBottomThenFadeIn(root, floaters, () -> {
                bottomCardHbox.getChildren().addAll(botBldViews);
                requestInteractionUpdate();
                botBldViews.forEach(this::fadeInNode);
            });
        });
    }

    /**
     * Fallback for when the scene is not yet attached: replaces both rows without animations.
     */
    private void doTopBuildingRefreshViews(List<ImageView> topBldViews, List<ImageView> botBldViews) {
        if (topCardHbox.getChildren().size() > topTribeCount)
            topCardHbox.getChildren().remove(topTribeCount, topCardHbox.getChildren().size());
        topCardHbox.getChildren().addAll(topBldViews);
        if (bottomCardHbox.getChildren().size() > bottomTribeCount)
            bottomCardHbox.getChildren().remove(bottomTribeCount, bottomCardHbox.getChildren().size());
        bottomCardHbox.getChildren().addAll(botBldViews);
        requestInteractionUpdate();
    }

    /**
     * Called when the board is initialised with offer tiles and default tiles.
     * Tiles are rendered only once per session; subsequent calls are ignored.
     *
     * @param tiles the list of offer tiles on the board.
     * @param defs  the list of default tiles (one per player).
     */
    @Override
    public void onBoardInitialized(List<OffertileDTO> tiles, List<DefaultTileDTO> defs) {
        if (tilesRendered) return;
        if (tileHbox == null) {
            this.pendingDefs = defs;
            this.pendingOffertiles = tiles;
        } else {
            Platform.runLater(() -> renderTiles(tiles, defs));
        }
    }

    /**
     * Called when a player places their totem on an offer tile.
     * Animates the totem from the default tile to the chosen offer tile, or
     * places it directly if the source overlay is not available.
     *
     * @param nickname     the nickname of the player who placed the totem.
     * @param tilePosition the zero-based position of the occupied offer tile.
     * @param fromSlot     the slot on the default tile from which the totem originates (-1 if unavailable).
     */
    @Override
    public void onPlayerPlacedOnOfferTile(String nickname, int tilePosition, int fromSlot) {
        Platform.runLater(() -> {
            Pane offerOverlay = offerTileOverlays.get(tilePosition);
            if (offerOverlay == null) return;
            boolean isMe = nickname.equals(playerDTO.getNickName());
            if (isMe && previewTilePosition == tilePosition) {
                // The server confirmed our own preview placement — just clear the preview flag
                previewTilePosition = -1;
                return;
            }
            if (isMe) previewTilePosition = -1;
            if (fromSlot < 0 || defaultTileOverlay == null) {
                // No animation source available: place totem directly
                double w = offerTileWidths.getOrDefault(tilePosition, cardFitHeight);
                offerOverlay.getChildren().clear();
                placeTotemOnOverlay(offerOverlay, nickname, OFFER_TOTEM_X, OFFER_TOTEM_Y, w);
                return;
            }
            animateTotem(defaultSlotScene(fromSlot), offerSlotScene(tilePosition),
                    colorOf(nickname), () -> {
                        double w = offerTileWidths.getOrDefault(tilePosition, cardFitHeight);
                        offerOverlay.getChildren().clear();
                        placeTotemOnOverlay(offerOverlay, nickname, OFFER_TOTEM_X, OFFER_TOTEM_Y, w);
                    });
        });
    }

    /**
     * Called when the turn order of players on the default tile changes.
     * Redraws the totems on the default tile according to the new turn order.
     *
     * @param order the ordered list of players on the default tile (index = slot).
     */
    @Override
    public void onDefaultTileOrderChanged(List<PlayerDTO> order) {
        Platform.runLater(() -> refreshDefaultTileOverlay(order));
    }

    /**
     * Redraws totem icons on the default tile according to the given turn order.
     */
    private void refreshDefaultTileOverlay(List<PlayerDTO> order) {
        if (defaultTileOverlay == null) return;
        defaultTileOverlay.getChildren().clear();
        int totalPlayers = clientHandler.getPlayers().size();
        double[] ySlots = DEFAULT_SLOT_Y.getOrDefault(totalPlayers, new double[]{0.5});
        for (int i = 0; i < order.size(); i++) {
            PlayerDTO p = order.get(i);
            if (p == null) continue;
            // Skip the local player while a preview is shown on an offer tile
            if (previewTilePosition >= 0 && p.getNickName().equals(playerDTO.getNickName())) continue;
            double yFrac = i < ySlots.length ? ySlots[i] : ySlots[ySlots.length - 1];
            placeTotemOnOverlay(defaultTileOverlay, p.getNickName(), 0.50, yFrac, defaultTileWidth);
        }
    }

    /**
     * Called when the available actions for the current player change
     * (number of draws from the top and bottom rows).
     *
     * @param drawTop number of draws available from the top market row.
     * @param drawBot number of draws available from the bottom market row.
     */
    @Override
    public void onActionAvailableChanged(int drawTop, int drawBot) {
        Platform.runLater(() -> {
            pendingRequest = false;
            if (isMyPlayingTurn()) {
                if (drawTopLabel != null) drawTopLabel.setText("Pesca da sopra: " + drawTop);
                if (drawBotLabel != null) drawBotLabel.setText("Pesca da sotto: " + drawBot);
            } else {
                if (drawTopLabel != null) drawTopLabel.setText("Pesca da sopra: -");
                if (drawBotLabel != null) drawBotLabel.setText("Pesca da sotto: -");
            }
            requestInteractionUpdate();
        });
    }

    /**
     * Called when a new player joins the game.
     * Delegates to the default interface implementation.
     *
     * @param player the DTO of the added player.
     */
    @Override
    public void onPlayerAdded(PlayerDTO player) {
        GUIObserver.super.onPlayerAdded(player);
    }

    /**
     * Called when the server sends an error message.
     * Shows the error dialog on the JavaFX thread.
     *
     * @param message the error text received from the server.
     */
    @Override
    public void onError(String message) {
        // Ignore lobby-phase errors (no game created, colour taken, etc.):
        // the game screen is not visible yet and LobbyController already handles them.
        if (!clientHandler.isGameStarted) return;
        Platform.runLater(() -> {
            pendingRequest = false;
            updateInteractionState();
            GUIEffects.showError(message);
        });
    }

    /**
     * Called when a player disconnects from the game.
     * Adds a disconnection entry to the player-status popup.
     *
     * @param nickname the nickname of the disconnected player.
     */
    @Override
    public void onPlayerDisconnected(String nickname) {
        Platform.runLater(() -> disconnectPopup.addDisconnection(nickname, getSceneRoot()));
    }

    /**
     * Called when a player reconnects to the game.
     * Adds a reconnection entry to the player-status popup.
     *
     * @param nickname the nickname of the reconnected player.
     */
    @Override
    public void onPlayerReconnected(String nickname) {
        Platform.runLater(() -> disconnectPopup.addReconnection(nickname, getSceneRoot()));
    }

    /**
     * Called when loss of connection to the server is detected.
     * Shows an error dialog and exits the application on close.
     */
    @Override
    public void onServerDead() {
        Platform.runLater(() -> {
            Pane root = getSceneRoot();
            if (root == null) { System.exit(0); return; }

            javafx.scene.control.Label title = new javafx.scene.control.Label("CONNESSIONE PERSA");
            title.setStyle(
                    "-fx-font-size: 30px; -fx-font-weight: bold;" +
                    "-fx-text-fill: #e74c3c; -fx-padding: 0 0 8 0;");

            javafx.scene.layout.Region divider = new javafx.scene.layout.Region();
            divider.setPrefHeight(2);
            divider.setStyle("-fx-background-color: #e74c3c;");

            javafx.scene.control.Label body = new javafx.scene.control.Label(
                    "La connessione al server è stata persa.");
            body.setStyle("-fx-font-size: 16px; -fx-text-fill: #bdc3c7;");

            javafx.scene.control.Label hint = new javafx.scene.control.Label(
                    "Clicca o premi ESC per uscire");
            hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(
                    14, title, divider, body, hint);
            content.setAlignment(javafx.geometry.Pos.CENTER);
            content.setPadding(new javafx.geometry.Insets(32, 48, 32, 48));
            content.setStyle(
                    "-fx-background-color: #0e0e1a;" +
                    "-fx-background-radius: 10;");

            javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane(content);
            overlay.setStyle("-fx-background-color: rgba(5,0,0,0.88);");
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setPickOnBounds(true);

            if (root instanceof javafx.scene.layout.AnchorPane) {
                javafx.scene.layout.AnchorPane.setTopAnchor(overlay, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(overlay, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(overlay, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(overlay, 0.0);
            } else {
                overlay.prefWidthProperty().bind(root.widthProperty());
                overlay.prefHeightProperty().bind(root.heightProperty());
            }

            overlay.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> System.exit(0));
            root.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) System.exit(0);
            });

            root.getChildren().add(overlay);
        });
    }

    /**
     * Called when a player's Prestige Points change.
     * Updates the PP label only if the player is the local one.
     *
     * @param n the nickname of the player whose PP changed.
     * @param p the new Prestige Points value.
     */
    @Override
    public void onPlayerPPChanged(String n, int p) {
        if (playerDTO.getNickName().equals(n))
            Platform.runLater(() -> {
                if (prestigePointLabel != null) prestigePointLabel.setText("Punti Prestigio: " + p);
            });
    }

    /**
     * Called when a player's food reserve changes.
     * Updates the food label and recalculates interactions only for the local player.
     *
     * @param n the nickname of the player whose food changed.
     * @param f the new food reserve value.
     */
    @Override
    public void onPlayerFoodChanged(String n, int f) {
        if (playerDTO.getNickName().equals(n)) {
            playerDTO.setFood(f);
            Platform.runLater(() -> {
                if (foodLabel != null) foodLabel.setText("Cibo: " + f);
                requestInteractionUpdate();
            });
        }
    }

    /**
     * Called when a card is added to a player's tribe.
     * Updates the local player's stat labels if they are the affected player.
     *
     * @param nickname the nickname of the player who received the card.
     * @param card     the DTO of the card added to the tribe.
     */
    @Override
    public void onCardAddedToTribe(String nickname, CardDTO card) {
        if (playerDTO.getNickName().equals(nickname))
            Platform.runLater(this::updatePlayerStatsLabels);
    }

    /**
     * Called when a game event is resolved.
     * If an extra-draw is active, the notification is deferred; otherwise it appears immediately in the event popup.
     *
     * @param eventID   the unique ID of the resolved event.
     * @param eventType the type of the resolved event.
     */
    @Override
    public void onEventResolved(int eventID, EVENT_TYPE eventType) {
        Platform.runLater(() -> {
            Pane root = getSceneRoot();
            if (root == null) return;
            if (extraDrawActive) {
                deferredUiActions.add(() -> eventPopup.addEvent(eventID, eventType, cardFitHeight, root));
            } else {
                eventPopup.addEvent(eventID, eventType, cardFitHeight, root);
            }
        });
    }

    private Pane getSceneRoot() {
        if (tileHbox == null) return null;
        Scene scene = tileHbox.getScene();
        if (scene == null) return null;
        return (Pane) scene.getRoot();
    }

    /**
     * Called when the server asks the local player to draw an extra card at end of round.
     * Replaces the top row with a snapshot of the available cards and shows the extra-draw banner.
     *
     * @param cards     the tribe cards available for the extra selection.
     * @param buildings the buildings available for the extra selection.
     */
    @Override
    public void onAskExtraDraw(List<CardDTO> cards, List<BuildingDTO> buildings) {
        // Pre-build ImageViews on the calling thread to keep the FX thread free from I/O
        List<ImageView> cardViews = cards.stream().map(c -> CardImageFactory.cardImageView(c, cardFitHeight)).toList();
        List<ImageView> bldViews = buildings.stream().map(b -> CardImageFactory.buildingImageView(b, cardFitHeight)).toList();
        Platform.runLater(() -> {
            clearCardSelection();
            extraDrawActive = true;
            topCardHbox.getChildren().clear();
            extraDrawTribeCount = cardViews.size();
            topCardHbox.getChildren().addAll(cardViews);
            topCardHbox.getChildren().addAll(bldViews);
            requestInteractionUpdate();
            showExtraDrawPopup();
        });
    }

    /**
     * Called when the game ends with the list of winners.
     * Loads and shows the end-game screen and requests the global leaderboard from the server.
     *
     * @param w the list of winning players.
     */
    @Override
    public void onWinners(List<PlayerDTO> w) {
        List<PlayerDTO> allPlayers = new java.util.ArrayList<>(clientHandler.getPlayers());
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/FXML/EndGame.fxml"));
                javafx.scene.Parent root = loader.load();
                it.polimi.ingsw.am25.client.GUI.EndGameController ctrl = loader.getController();
                ctrl.setData(w, allPlayers);
                endGameController = ctrl;
                javafx.stage.Stage stage = (javafx.stage.Stage) tileHbox.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("IS26-AM25 — Fine Partita");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            serverRemoteInterface.askForRank(String.valueOf(clientHandler.getPlayers().size()), clientHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the server returns the global leaderboard.
     * Filters entries for the current player count and passes them to the end-game controller.
     *
     * @param leaderboards map from player count to the corresponding leaderboard rows.
     */
    @Override
    public void onRankReceived(java.util.Map<Integer, java.util.List<String>> leaderboards) {
        if (endGameController == null) return;
        int playerCount = clientHandler.getPlayers().size();
        java.util.List<String> entries = leaderboards.get(playerCount);
        Platform.runLater(() -> endGameController.setGlobalLeaderboard(entries));
    }


    // =========================================================
    // RENDER
    // =========================================================

    /**
     * Populates both card rows from scratch using pre-built ImageViews (called on market initialization).
     */
    private void renderCardViews(List<ImageView> topViews, List<ImageView> botViews,
                                 List<ImageView> topBldViews, List<ImageView> botBldViews) {
        topCardHbox.getChildren().clear();
        bottomCardHbox.getChildren().clear();
        selectedCardView = null;
        topTribeCount = topViews.size();
        topCardHbox.getChildren().addAll(topViews);
        topCardHbox.getChildren().addAll(topBldViews);
        bottomTribeCount = botViews.size();
        bottomCardHbox.getChildren().addAll(botViews);
        bottomCardHbox.getChildren().addAll(botBldViews);
        requestInteractionUpdate();
    }

    /** Called from initialize() when data was buffered before FXML was ready (already on FX thread; cache keeps it fast). */
    private void renderCards(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld, List<BuildingDTO> botBld) {
        List<ImageView> topViews = top.stream().map(c -> CardImageFactory.cardImageView(c, cardFitHeight)).toList();
        List<ImageView> topBldViews = topBld.stream().map(b -> CardImageFactory.buildingImageView(b, cardFitHeight)).toList();
        List<ImageView> botViews = bot.stream().map(c -> CardImageFactory.cardImageView(c, cardFitHeight)).toList();
        List<ImageView> botBldViews = botBld != null ? botBld.stream().map(b -> CardImageFactory.buildingImageView(b, cardFitHeight)).toList() : List.of();
        renderCardViews(topViews, botViews, topBldViews, botBldViews);
    }

    /**
     * Builds the tile row once: one default tile followed by the offer tiles.
     * Creates transparent overlay panes for totem rendering on top of each tile image.
     */
    private void renderTiles(List<OffertileDTO> tiles, List<DefaultTileDTO> defs) {
        if (tilesRendered) return;
        tilesRendered = true;
        tileHbox.getChildren().clear();
        clearTileSelection();
        offerTileOverlays.clear();
        offerTileWidths.clear();

        ImageView defIv = CardImageFactory.defaultTileImageView(defs.size(), cardFitHeight);
        double defW = cardFitHeight * defIv.getImage().getWidth() / defIv.getImage().getHeight();
        defaultTileOverlay = new Pane();
        defaultTileOverlay.setPrefSize(defW, cardFitHeight);
        defaultTileOverlay.setMouseTransparent(true);
        defaultTileWidth = defW;
        javafx.scene.layout.StackPane defStack = new javafx.scene.layout.StackPane(defIv, defaultTileOverlay);
        defStack.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        tileHbox.getChildren().add(defStack);

        for (int i = 0; i < tiles.size(); i++) {
            ImageView iv = CardImageFactory.offerTileImageView(tiles.get(i).getOfferTileID(), cardFitHeight);
            double w = cardFitHeight * iv.getImage().getWidth() / iv.getImage().getHeight();
            Pane overlay = new Pane();
            overlay.setPrefSize(w, cardFitHeight);
            overlay.setMouseTransparent(true);
            offerTileOverlays.put(i, overlay);
            offerTileWidths.put(i, w);
            javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(iv, overlay);
            stack.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            tileHbox.getChildren().add(stack);
        }
        List<PlayerDTO> initialOrder = clientHandler.getDefaultTileOrder();
        if (!initialOrder.isEmpty()) refreshDefaultTileOverlay(initialOrder);
        requestInteractionUpdate();
    }

    // =========================================================
    // INTERACTION STATE
    // =========================================================

    /**
     * Returns true when the current phase is a placing phase (either normal or last round).
     */
    private boolean isPlacingPhase() {
        GAME_PHASE phase = clientHandler.getGamePhase();
        return phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE;
    }

    /**
     * Returns true when it is this player's turn to place their totem.
     */
    private boolean isMyPlacingTurn() {
        return isPlacingPhase() && playerDTO.getNickName().equals(clientHandler.getPlayerToPlace());
    }

    /**
     * Returns true when it is this player's turn to pick a card.
     */
    private boolean isMyPlayingTurn() {
        return !isPlacingPhase() && playerDTO.getNickName().equals(clientHandler.getPlayerToPlay());
    }

    /**
     * Central method that recalculates the enabled/disabled and visual state of every
     * interactive element (tiles, cards, buttons) based on the current game phase and turn.
     */
    private boolean interactionUpdatePending = false;

    private void requestInteractionUpdate() {
        if (!interactionUpdatePending) {
            interactionUpdatePending = true;
            Platform.runLater(() -> {
                interactionUpdatePending = false;
                updateInteractionState();
            });
        }
    }

    private void updateInteractionState() {
        if (placeTotemButton == null || tileHbox == null) return;

        boolean myPlacing = isMyPlacingTurn();
        boolean myPlaying = isMyPlayingTurn();

        Map<Integer, String> occupants = clientHandler.getOfferTileOccupants();
        for (int i = 0; i < tileHbox.getChildren().size(); i++) {
            javafx.scene.layout.StackPane sp = (javafx.scene.layout.StackPane) tileHbox.getChildren().get(i);
            // Index 0 is the default tile (not selectable); offer tiles start at index 1
            boolean clickable = myPlacing && i > 0 && !occupants.containsKey(i - 1);
            if (clickable) {
                sp.setEffect(sp == selectedTilePane ? GUIEffects.goldGlow() : null);
                sp.setOpacity(1.0);
                final int pos = i - 1;
                sp.setOnMouseClicked(e -> selectTile(sp, pos));
                sp.setStyle("-fx-cursor: hand;");
            } else {
                if (sp != selectedTilePane) sp.setEffect(myPlacing ? null : GUIEffects.GRAY);
                sp.setOpacity(myPlacing ? 1.0 : 0.70);
                sp.setOnMouseClicked(null);
                sp.setStyle("");
            }
        }
        if (!myPlacing) clearTileSelection();

        applyCardRowState(topCardHbox, extraDrawActive || (myPlaying && clientHandler.getDrawTop() > 0));
        applyCardRowState(bottomCardHbox, !extraDrawActive && myPlaying && clientHandler.getDrawBot() > 0);
        if (!myPlaying && !extraDrawActive) clearCardSelection();

        placeTotemButton.setDisable(!myPlacing || selectedTilePosition == -1 || pendingRequest);
        selectCardButton.setDisable((!myPlaying && !extraDrawActive) || selectedCardView == null || pendingRequest);
        if (extraDrawActive) {
            skipTurnButton.setDisable(pendingRequest);
        } else {
            boolean hasActions = clientHandler.getDrawTop() > 0 || clientHandler.getDrawBot() > 0;
            skipTurnButton.setDisable(!myPlaying || !hasActions || hasSelectableTribeCard() || pendingRequest);
        }
    }

    /**
     * Returns true if there is at least one non-event tribe card available to pick
     * in a row that has remaining draw actions.
     */
    private boolean hasSelectableTribeCard() {
        if (clientHandler.getDrawTop() > 0 && tribeCardExistsInRow(topCardHbox, topTribeCount)) return true;
        return clientHandler.getDrawBot() > 0 && tribeCardExistsInRow(bottomCardHbox, bottomTribeCount);
    }

    /**
     * Checks whether a non-event tribe card exists among the first {@code tribeCount} children of the row.
     */
    private boolean tribeCardExistsInRow(HBox row, int tribeCount) {
        for (int i = 0; i < tribeCount && i < row.getChildren().size(); i++) {
            Node node = row.getChildren().get(i);
            if (node.getUserData() instanceof CardDTO dto && dto.getCardType() != CARD_TYPE.EVENT) return true;
        }
        return false;
    }

    /**
     * Refreshes the shaman-star and builder-discount labels from the live player data.
     */
    private void updatePlayerStatsLabels() {
        int discount = totalBuilderDiscount();
        int stars = totalShamanStars();
        if (builderDiscountLabel != null) builderDiscountLabel.setText("Sconto Costruttori: " + discount);
        if (shamanStarLabel != null) shamanStarLabel.setText("Stelle sciamano: " + stars);
        requestInteractionUpdate();
    }

    /**
     * Sums the star values of all shaman cards in the local player's tribe.
     */
    private int totalShamanStars() {
        return clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(playerDTO.getNickName()))
                .findFirst()
                .map(p -> p.getCardDtoList().stream()
                        .filter(c -> c.getCardType() == CARD_TYPE.SHAMAN)
                        .mapToInt(c -> switch (c.getStarNumber()) {
                            case ONE -> 1;
                            case TWO -> 2;
                            case THREE -> 3;
                        })
                        .sum())
                .orElse(0);
    }

    /**
     * Sums the food-discount values of all builder cards in the local player's tribe.
     */
    private int totalBuilderDiscount() {
        return clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(playerDTO.getNickName()))
                .findFirst()
                .map(p -> p.getCardDtoList().stream()
                        .filter(c -> c.getCardType() == CARD_TYPE.BUILDER)
                        .mapToInt(CardDTO::getFoodDiscount)
                        .sum())
                .orElse(0);
    }

    /**
     * Returns true if the local player can afford to pick the given card.
     * Non-building cards are always affordable; buildings require enough food after applying the discount.
     */
    private boolean canAfford(CardDTO card) {
        if (!(card instanceof BuildingDTO bld)) return true;
        int cost = bld.getFoodCost() - totalBuilderDiscount();
        return playerDTO.getFood() >= Math.max(0, cost);
    }

    /**
     * Applies visual state (opacity, click handler, glow) to every card node in a row.
     * {@code rowActive} is true when the player can draw from this row on the current turn.
     */
    private void applyCardRowState(HBox row, boolean rowActive) {
        for (Node node : row.getChildren()) {
            if (!(node.getUserData() instanceof CardDTO dto)) continue;
            boolean selectable = rowActive && dto.getCardType() != CARD_TYPE.EVENT && canAfford(dto);

            if (node == selectedCardView) {
                // Keep the gold glow on the selected card regardless of selectability
            } else if (!selectable) {
                node.setEffect(GUIEffects.GRAY);
                node.setOpacity(0.70);
            } else {
                node.setEffect(null);
                node.setOpacity(1.0);
            }

            if (dto instanceof BuildingDTO && !canAfford(dto)) {
                activeTooltips.computeIfAbsent(node, n -> {
                    Tooltip tt = new Tooltip("Non hai abbastanza cibo");
                    tt.setShowDelay(new Duration(300));
                    Tooltip.install(n, tt);
                    return tt;
                });
            } else {
                Tooltip tt = activeTooltips.remove(node);
                if (tt != null) Tooltip.uninstall(node, tt);
            }

            if (selectable) {
                ImageView iv = (ImageView) node;
                iv.setOnMouseClicked(e -> selectCard(iv));
                iv.setStyle("-fx-cursor: hand;");
            } else {
                node.setOnMouseClicked(null);
                node.setStyle("");
                if (node == selectedCardView) clearCardSelection();
            }
        }
    }

    // =========================================================
    // TILE / CARD SELECTION
    // =========================================================

    /**
     * Handles a click on an offer tile. Toggles selection and animates the local player's
     * totem between the default tile and the selected offer tile as a preview.
     */
    private void selectTile(javafx.scene.layout.StackPane sp, int position) {
        if (previewAnimating) return;
        COLOR myColor = colorOf(playerDTO.getNickName());
        if (selectedTilePane != null) selectedTilePane.setEffect(null);

        if (selectedTilePane == sp) {
            // Deselect: animate totem back to the default tile
            selectedTilePane = null;
            selectedTilePosition = -1;
            int old = previewTilePosition;
            previewTilePosition = -1;
            if (old >= 0 && defaultTileOverlay != null) {
                Pane oldOverlay = offerTileOverlays.get(old);
                if (oldOverlay != null) oldOverlay.getChildren().clear();
                int mySlot = myDefaultSlot();
                if (mySlot >= 0) {
                    previewAnimating = true;
                    animateTotem(offerSlotScene(old), defaultSlotScene(mySlot), myColor, () -> {
                        previewAnimating = false;
                        refreshDefaultTileOverlay(clientHandler.getDefaultTileOrder());
                    });
                }
            }
        } else {
            // Select new tile: animate totem from previous position (default or another offer tile)
            int old = previewTilePosition;
            sp.setEffect(GUIEffects.goldGlow());
            selectedTilePane = sp;
            selectedTilePosition = position;
            previewTilePosition = position;
            Pane newOverlay = offerTileOverlays.get(position);
            if (old >= 0) {
                // Moving preview from one offer tile to another
                Pane oldOverlay = offerTileOverlays.get(old);
                if (oldOverlay != null) oldOverlay.getChildren().clear();
                if (newOverlay != null) {
                    previewAnimating = true;
                    animateTotem(offerSlotScene(old), offerSlotScene(position), myColor, () -> {
                        previewAnimating = false;
                        newOverlay.getChildren().clear();
                        placeTotemOnOverlay(newOverlay, playerDTO.getNickName(),
                                OFFER_TOTEM_X, OFFER_TOTEM_Y, offerTileWidths.getOrDefault(position, cardFitHeight));
                    });
                }
            } else {
                // Moving preview from the default tile to an offer tile
                int mySlot = myDefaultSlot();
                if (mySlot >= 0 && defaultTileOverlay != null && newOverlay != null) {
                    Point2D src = defaultSlotScene(mySlot);
                    refreshDefaultTileOverlay(clientHandler.getDefaultTileOrder());
                    previewAnimating = true;
                    animateTotem(src, offerSlotScene(position), myColor, () -> {
                        previewAnimating = false;
                        newOverlay.getChildren().clear();
                        placeTotemOnOverlay(newOverlay, playerDTO.getNickName(),
                                OFFER_TOTEM_X, OFFER_TOTEM_Y, offerTileWidths.getOrDefault(position, cardFitHeight));
                    });
                }
            }
        }
        placeTotemButton.setDisable(selectedTilePosition == -1);
    }

    /**
     * Toggles card selection; applies or removes the gold glow effect.
     */
    private void selectCard(ImageView iv) {
        if (selectedCardView != null) selectedCardView.setEffect(null);
        if (selectedCardView == iv) {
            selectedCardView = null;
        } else {
            iv.setEffect(GUIEffects.goldGlow());
            selectedCardView = iv;
        }
        selectCardButton.setDisable(selectedCardView == null);
    }

    /**
     * Clears tile selection state and removes any in-progress totem preview,
     * restoring the default tile overlay to the current turn order.
     */
    private void clearTileSelection() {
        previewAnimating = false;
        if (previewTilePosition >= 0) {
            Pane prev = offerTileOverlays.get(previewTilePosition);
            if (prev != null) prev.getChildren().clear();
            previewTilePosition = -1;
            if (defaultTileOverlay != null)
                refreshDefaultTileOverlay(clientHandler.getDefaultTileOrder());
        }
        if (selectedTilePane != null) {
            selectedTilePane.setEffect(null);
            selectedTilePane = null;
        }
        selectedTilePosition = -1;
        if (placeTotemButton != null) placeTotemButton.setDisable(true);
    }

    /**
     * Removes the gold glow from the currently selected card and resets selection state.
     */
    private void clearCardSelection() {
        if (selectedCardView != null) {
            selectedCardView.setEffect(null);
            selectedCardView = null;
        }
        if (selectCardButton != null) selectCardButton.setDisable(true);
    }

    /**
     * Builds and shows the extra-draw banner centered at the top of the current window.
     */
    private void showExtraDrawPopup() {
        if (extraDrawPopup == null) {
            javafx.scene.control.Label label = new javafx.scene.control.Label("Seleziona carta extra:");
            label.setStyle(
                    "-fx-background-color: #2a1a0a;" +
                            "-fx-text-fill: gold;" +
                            "-fx-font-size: 22px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 14 28 14 28;" +
                            "-fx-border-color: gold;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;"
            );
            extraDrawPopup = new javafx.stage.Popup();
            extraDrawPopup.getContent().add(label);
            extraDrawPopup.setAutoFix(true);
        }
        javafx.stage.Window window = topCardHbox.getScene().getWindow();
        double centerX = window.getX() + window.getWidth() / 2.0;
        extraDrawPopup.show(window, centerX - 160, window.getY() + 60);
    }

    /**
     * Hides the extra-draw banner if it is currently visible.
     */
    private void hideExtraDrawPopup() {
        if (extraDrawPopup != null && extraDrawPopup.isShowing()) extraDrawPopup.hide();
    }

    /**
     * Ends the extra-draw mode: restores the top row from the current market state
     * (as stored in clientHandler, which reflects any refreshes that arrived while the
     * snapshot was on screen) and re-evaluates interaction state.
     */
    private void clearExtraDraw() {
        extraDrawActive = false;
        extraDrawTribeCount = 0;
        hideExtraDrawPopup();
        clearCardSelection();
        topCardHbox.getChildren().clear();
        topTribeCount = 0;
        for (CardDTO card : clientHandler.getTopCards()) {
            topCardHbox.getChildren().add(CardImageFactory.cardImageView(card, cardFitHeight));
            topTribeCount++;
        }
        for (BuildingDTO bld : clientHandler.getTopBuildings()) {
            topCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));
        }
        // Rebuild bottom row from the snapshots captured at round-end (before any race conditions)
        if (pendingBottomCards != null) {
            bottomCardHbox.getChildren().clear();
            bottomTribeCount = 0;
            for (CardDTO card : pendingBottomCards) {
                bottomCardHbox.getChildren().add(CardImageFactory.cardImageView(card, cardFitHeight));
                bottomTribeCount++;
            }
            List<BuildingDTO> blds = pendingBottomBuildings != null ? pendingBottomBuildings : new ArrayList<>();
            for (BuildingDTO bld : blds) {
                bottomCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));
            }
            pendingBottomCards = null;
            pendingBottomBuildings = null;
        }
        // Flush deferred event popups
        deferredUiActions.forEach(Runnable::run);
        deferredUiActions.clear();
        updateInteractionState();
    }

    // =========================================================
    // TOTEM
    // =========================================================

    /**
     * Places a totem image on the given overlay pane at the position specified
     * by fractional coordinates relative to the tile dimensions.
     */
    private void placeTotemOnOverlay(Pane overlay, String nickname, double xFrac, double yFrac, double tileW) {
        COLOR color = colorOf(nickname);
        Image totemImg = CardImageFactory.totemImage(color);
        double totemH = cardFitHeight * 0.20;
        double totemW = totemH * totemImg.getWidth() / totemImg.getHeight();
        ImageView iv = new ImageView(totemImg);
        iv.setFitHeight(totemH);
        iv.setPreserveRatio(true);
        iv.setRotate(90);
        iv.setLayoutX(tileW * xFrac - totemW / 2.0);
        iv.setLayoutY(cardFitHeight * yFrac - totemH / 2.0);
        overlay.getChildren().add(iv);
    }

    /**
     * Looks up the totem color for the given player nickname. Defaults to RED if not found.
     */
    private COLOR colorOf(String nickname) {
        return clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(nickname))
                .findFirst()
                .map(PlayerDTO::getColorTotem)
                .orElse(COLOR.RED);
    }

    /**
     * Returns the scene coordinates of the local player's slot on the default tile
     * (center of the slot circle at the given turn-order index).
     */
    private Point2D defaultSlotScene(int slot) {
        int totalPlayers = clientHandler.getPlayers().size();
        double[] ySlots = DEFAULT_SLOT_Y.getOrDefault(totalPlayers, new double[]{0.5});
        double yFrac = slot < ySlots.length ? ySlots[slot] : ySlots[ySlots.length - 1];
        return defaultTileOverlay.localToScene(defaultTileWidth * 0.50, cardFitHeight * yFrac);
    }

    /**
     * Returns the scene coordinates of the totem slot on the given offer tile
     * (the fixed OFFER_TOTEM_X / OFFER_TOTEM_Y position).
     */
    private Point2D offerSlotScene(int tilePos) {
        Pane overlay = offerTileOverlays.get(tilePos);
        if (overlay == null) return new Point2D(0, 0);
        double w = offerTileWidths.getOrDefault(tilePos, cardFitHeight);
        return overlay.localToScene(w * OFFER_TOTEM_X, cardFitHeight * OFFER_TOTEM_Y);
    }

    /**
     * Returns the index of the local player in the current default-tile turn order,
     * or -1 if the player is not found.
     */
    private int myDefaultSlot() {
        List<PlayerDTO> order = clientHandler.getDefaultTileOrder();
        for (int i = 0; i < order.size(); i++) {
            PlayerDTO p = order.get(i);
            if (p != null && p.getNickName().equals(playerDTO.getNickName())) return i;
        }
        return -1;
    }

    // =========================================================
    // ANIMATIONS
    // =========================================================

    /**
     * Fades a node in from transparent to its current opacity (as set by
     * {@link #updateInteractionState} or the default 1.0).
     */
    private void fadeInNode(Node node) {
        double target = node.getOpacity();
        node.setOpacity(0.0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0.0);
        ft.setToValue(target);
        ft.setOnFinished(e -> node.setOpacity(target));
        ft.play();
    }

    /**
     * Animates a card or building being removed from a row: shrinks and fades out in parallel,
     * then removes the node from the parent HBox and runs {@code onDone} if provided.
     */
    private void animateRemove(Node node, HBox parent, Runnable onDone) {
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(250), node);
        st.setToX(0.85);
        st.setToY(0.85);
        FadeTransition ft = new FadeTransition(Duration.millis(250), node);
        ft.setFromValue(node.getOpacity());
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            parent.getChildren().remove(node);
            if (onDone != null) onDone.run();
        });
        st.play();
        ft.play();
    }

    /**
     * Creates visual ghost copies (floaters) of the given nodes, placed at their current
     * absolute positions in the root AnchorPane. Nodes already exiting (mouseTransparent)
     * are skipped, as they have been picked by a player and should not fly to the bottom row.
     */
    private List<ImageView> snapshotFloaters(Pane root, java.util.List<Node> nodes) {
        List<ImageView> floaters = new ArrayList<>();
        for (Node node : nodes) {
            if (node.isMouseTransparent()) continue;
            ImageView floater;
            if (node.getUserData() instanceof BuildingDTO bld)
                floater = CardImageFactory.buildingImageView(bld, cardFitHeight);
            else if (node.getUserData() instanceof CardDTO card)
                floater = CardImageFactory.cardImageView(card, cardFitHeight);
            else continue;
            Point2D pos = root.sceneToLocal(node.localToScene(0, 0));
            floater.setMouseTransparent(true);
            floater.setLayoutX(pos.getX());
            floater.setLayoutY(pos.getY());
            root.getChildren().add(floater);
            floaters.add(floater);
        }
        return floaters;
    }

    /**
     * Animates each floater downward to the top edge of {@code bottomCardHbox},
     * fading out during the flight. When the last floater lands, {@code onAllLanded} is called
     * so the caller can add the new bottom-row cards with a fade-in.
     * If the floater list is empty, {@code onAllLanded} is invoked immediately.
     */
    private void flyToBottomThenFadeIn(Pane root, List<ImageView> floaters, Runnable onAllLanded) {
        if (floaters.isEmpty()) {
            if (onAllLanded != null) onAllLanded.run();
            return;
        }
        double dstY = root.sceneToLocal(bottomCardHbox.localToScene(0, 0)).getY();
        int[] remaining = {floaters.size()};
        for (ImageView floater : floaters) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(380), floater);
            tt.setByY(dstY - floater.getLayoutY());
            tt.setInterpolator(Interpolator.EASE_IN);
            FadeTransition ft = new FadeTransition(Duration.millis(380), floater);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            tt.setOnFinished(e -> {
                root.getChildren().remove(floater);
                if (--remaining[0] == 0 && onAllLanded != null) onAllLanded.run();
            });
            tt.play();
            ft.play();
        }
    }

    /**
     * Animates a totem image sliding from {@code srcScene} to {@code dstScene} across
     * the root pane, then removes the animated image and calls {@code onFinish}.
     */
    private void animateTotem(Point2D srcScene, Point2D dstScene, COLOR color, Runnable onFinish) {
        if (tileHbox.getScene() == null) {
            if (onFinish != null) onFinish.run();
            return;
        }
        Pane root = (Pane) tileHbox.getScene().getRoot();
        Point2D srcRoot = root.sceneToLocal(srcScene);
        Point2D dstRoot = root.sceneToLocal(dstScene);

        Image totemImg = CardImageFactory.totemImage(color);
        double totemH = cardFitHeight * 0.20;
        double totemW = totemH * totemImg.getWidth() / totemImg.getHeight();
        ImageView iv = new ImageView(totemImg);
        iv.setFitHeight(totemH);
        iv.setPreserveRatio(true);
        iv.setRotate(90);
        iv.setMouseTransparent(true);
        iv.setLayoutX(srcRoot.getX() - totemW / 2.0);
        iv.setLayoutY(srcRoot.getY() - totemH / 2.0);
        root.getChildren().add(iv);

        TranslateTransition tt = new TranslateTransition(Duration.millis(500), iv);
        tt.setByX(dstRoot.getX() - srcRoot.getX());
        tt.setByY(dstRoot.getY() - srcRoot.getY());
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setOnFinished(e -> {
            root.getChildren().remove(iv);
            if (onFinish != null) onFinish.run();
        });
        tt.play();
    }

    private void showTurnNotification() {
        Pane root = getSceneRoot();
        if (root == null) return;

        Label lbl = new Label("È il tuo turno!");
        lbl.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold;" +
                "-fx-text-fill: #1a0e00;" +
                "-fx-padding: 14 36 14 36;");

        javafx.scene.layout.StackPane box = new javafx.scene.layout.StackPane(lbl);
        box.setStyle(
                "-fx-background-color: rgba(205,155,20,0.92);" +
                "-fx-background-radius: 8;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.4),10,0,0,2);");
        box.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        box.setMouseTransparent(true);

        javafx.scene.layout.StackPane container = new javafx.scene.layout.StackPane(box);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        container.setMouseTransparent(true);
        container.setOpacity(0);

        if (root instanceof AnchorPane) {
            AnchorPane.setTopAnchor(container, 0.0);
            AnchorPane.setBottomAnchor(container, 0.0);
            AnchorPane.setLeftAnchor(container, 0.0);
            AnchorPane.setRightAnchor(container, 0.0);
        } else {
            container.prefWidthProperty().bind(root.widthProperty());
            container.prefHeightProperty().bind(root.heightProperty());
        }
        root.getChildren().add(container);

        javafx.animation.ScaleTransition scaleIn =
                new javafx.animation.ScaleTransition(Duration.millis(220), box);
        scaleIn.setFromX(0.65); scaleIn.setFromY(0.65);
        scaleIn.setToX(1.0);   scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), container);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(450), container);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(1600));
        fadeOut.setOnFinished(e -> {
            container.prefWidthProperty().unbind();
            container.prefHeightProperty().unbind();
            root.getChildren().remove(container);
        });

        scaleIn.play();
        fadeIn.play();
        fadeIn.setOnFinished(e -> fadeOut.play());
    }

    private void startHeartbeat() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "gui-heartbeat-ping");
            t.setDaemon(true);
            return t;
        });
        final int[] failedPings = {0};
        scheduler.scheduleAtFixedRate(() -> {
            if (clientHandler.isServerDead()) {
                scheduler.shutdownNow();
                return;
            }
            try {
                serverRemoteInterface.ping(playerDTO);
                failedPings[0] = 0;
            } catch (Exception e) {
                if (++failedPings[0] >= 3) {
                    clientHandler.handleServerDeath();
                    scheduler.shutdownNow();
                }
            }
        }, 0, PongWatchdog.INTERVAL_S, TimeUnit.SECONDS);
    }
}
