package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.GUI.GUIObserver;
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
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MarketController implements GUIObserver {

    // --- data buffered when observer callbacks arrive before the FXML is ready ---
    private volatile List<CardDTO> pendingTopTribeCard;
    private volatile List<BuildingDTO> pendingTopBuildingCard;
    private volatile List<CardDTO> pendingBottomTribeCard;
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
    /** Y-fraction slots on the default tile for each possible player count. */
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
    /** Position of the tile currently showing the local player's totem preview (-1 = none). */
    private int previewTilePosition = -1;
    /** True while a totem preview animation is in flight; prevents re-entrant tile selection. */
    private boolean previewAnimating = false;

    // --- transparent overlay panes that host totem images on top of tile images ---
    private Pane defaultTileOverlay = null;
    private double defaultTileWidth = 0;
    private final Map<Integer, Pane> offerTileOverlays = new HashMap<>();
    private final Map<Integer, Double> offerTileWidths = new HashMap<>();

    // --- card selection state ---
    private ImageView selectedCardView = null;
    /** Tracks active tooltips on building nodes so they can be cleanly uninstalled. */
    private final WeakHashMap<Node, Tooltip> activeTooltips = new WeakHashMap<>();
    /** Number of tribe cards (non-building) currently in the top row. */
    private int topTribeCount = 0;
    /** Number of tribe cards (non-building) currently in the bottom row. */
    private int bottomTribeCount = 0;
    /** True while the player must pick an extra card from the end-of-round snapshot. */
    private boolean extraDrawActive = false;
    /** Number of tribe cards in the extra draw snapshot currently shown in the top row. */
    private int extraDrawTribeCount = 0;
    /** Non-blocking banner shown while extra draw is active. */
    private javafx.stage.Popup extraDrawPopup;
    /** UI actions deferred until the player resolves the extra draw (event popups). */
    private final List<Runnable> deferredUiActions = new ArrayList<>();
    /** Snapshot of the bottom tribe cards captured at round-end, applied after extra draw is resolved. */
    private List<CardDTO> pendingBottomCards = null;
    /** Snapshot of the bottom buildings captured at round-end, applied after extra draw is resolved. */
    private List<BuildingDTO> pendingBottomBuildings = null;
    private final DisconnectPopup disconnectPopup = new DisconnectPopup();

    @FXML private HBox topCardHbox;
    @FXML private HBox tileHbox;
    @FXML private HBox bottomCardHbox;
    @FXML private Button selectCardButton;
    @FXML private Button placeTotemButton;
    @FXML private Button skipTurnButton;
    @FXML private Button tribeVisualizerButton;
    @FXML private Button playerStatusButton;
    @FXML private Label phaseLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label drawTopLabel;
    @FXML private Label drawBotLabel;
    @FXML private Label prestigePointLabel;
    @FXML private Label foodLabel;
    @FXML private Label shamanStarLabel;
    @FXML private Label builderDiscountLabel;
    @FXML private HBox headerHBox;
    @FXML private javafx.scene.control.SplitPane splitPane;

    public MarketController(ClientVirtualView clientHandler, ServerRemoteInterface serverRemoteInterface, PlayerDTO playerDTO) {
        this.clientHandler = clientHandler;
        this.serverRemoteInterface = serverRemoteInterface;
        this.playerDTO = playerDTO;
        clientHandler.addGUIObserver(this);
    }

    // =========================================================
    // FXML
    // =========================================================

    /** Called by FXMLLoader after all @FXML fields are injected. Flushes any pending data. */
    @FXML
    public void initialize() {
        double availableH = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        cardFitHeight = Math.max(100, (availableH - 80) / 3 - 30);

        // Keep SplitPane top-anchor in sync with whatever height the header actually needs.
        headerHBox.heightProperty().addListener((obs, old, newH) ->
                AnchorPane.setTopAnchor(splitPane, newH.doubleValue()));
        if (placeTotemButton != null) placeTotemButton.setDisable(true);
        if (selectCardButton != null) selectCardButton.setDisable(true);
        if (skipTurnButton != null) skipTurnButton.setDisable(true);
        if (pendingOffertiles != null) {
            renderTiles(pendingOffertiles, pendingDefs);
            pendingOffertiles = null;
            pendingDefs = null;
        }
        if (pendingTopTribeCard != null) {
            renderCards(pendingTopTribeCard, pendingBottomTribeCard, pendingTopBuildingCard);
            pendingTopTribeCard = null;
            pendingBottomTribeCard = null;
            pendingTopBuildingCard = null;
        }
        updatePlayerStatsLabels();
        if (foodLabel != null) foodLabel.setText("Cibo: " + playerDTO.getFood());
        if (prestigePointLabel != null) prestigePointLabel.setText("Punti Prestigio: " + 0);
        updateInteractionState();
    }

    /** Sends the selected card to the server (top or bottom row, tribe or building). */
    @FXML
    private void handleSelectCard() {
        if (selectedCardView == null) return;
        try {
            CardDTO dto = (CardDTO) selectedCardView.getUserData();
            CARD_TYPE type = dto.getCardType();
            if (extraDrawActive) {
                int idx = topCardHbox.getChildren().indexOf(selectedCardView);
                int pos = idx < extraDrawTribeCount ? idx : idx - extraDrawTribeCount;
                serverRemoteInterface.selectExtraCard(playerDTO, type, pos);
                clearCardSelection();
                clearExtraDraw();
            } else if (topCardHbox.getChildren().contains(selectedCardView)) {
                int idx = topCardHbox.getChildren().indexOf(selectedCardView);
                int pos = idx < topTribeCount ? idx : idx - topTribeCount;
                serverRemoteInterface.selectCardFromTopList(playerDTO, type, pos);
                clearCardSelection();
            } else {
                int idx = bottomCardHbox.getChildren().indexOf(selectedCardView);
                int pos = idx < bottomTribeCount ? idx : idx - bottomTribeCount;
                serverRemoteInterface.selectCardFromBottomList(playerDTO, type, pos);
                clearCardSelection();
            }
        } catch (Exception e) {
            GUIEffects.showError(e.getMessage());
        }
    }

    /** Sends the totem placement request to the server for the currently selected tile. */
    @FXML
    private void handlePlaceTotem() {
        if (selectedTilePosition == -1) return;
        try {
            serverRemoteInterface.placingPlayer(playerDTO, selectedTilePosition);
            if (selectedTilePane != null) { selectedTilePane.setEffect(null); selectedTilePane = null; }
            selectedTilePosition = -1;
            placeTotemButton.setDisable(true);
            updateInteractionState();
        } catch (Exception e) {
            GUIEffects.showError(e.getMessage());
        }
    }

    /** Notifies the server that the local player passes (or skips the extra draw). */
    @FXML
    private void handleSkipTurn() {
        try {
            if (extraDrawActive) {
                serverRemoteInterface.skipExtraDraw(playerDTO);
                clearExtraDraw();
            } else {
                serverRemoteInterface.playerDoNothing(playerDTO);
            }
        } catch (Exception e) {
            GUIEffects.showError(e.getMessage());
        }
    }

    @FXML
    private void showThisPlayerTribe() {
        //TODO show the local player's tribe (ROBERT)
    }

    @FXML
    private void showPlayerStatus() {
        //TODO show all players' status (ROBERT)
    }

    // =========================================================
    // OBSERVER
    // =========================================================

    @Override
    public void onGamePhaseChanged(GAME_PHASE phase) {
        Platform.runLater(() -> {
            if (phaseLabel != null) phaseLabel.setText("Fase corrente: " + phase);
            if (phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE) {
                offerTileOverlays.values().forEach(p -> p.getChildren().clear());
            }
            updateInteractionState();
        });
    }

    @Override
    public void onPlayerToPlaceChanged(String nickname) {
        Platform.runLater(() -> {
            if (currentPlayerLabel != null) currentPlayerLabel.setText("Giocatore di turno: " + nickname);
            updateInteractionState();
        });
    }

    @Override
    public void onPlayerToPlayChanged(String nickname) {
        Platform.runLater(() -> {
            if (currentPlayerLabel != null) currentPlayerLabel.setText("Giocatore di turno: " + nickname);
            updateInteractionState();
        });
    }

    @Override
    public void onMarketInitialized(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld) {
        if (topCardHbox == null) {
            // FXML not ready yet — buffer the data for initialize()
            this.pendingTopTribeCard = top;
            this.pendingBottomTribeCard = bot;
            this.pendingTopBuildingCard = topBld;
        } else {
            Platform.runLater(() -> renderCards(top, bot, topBld));
        }
    }

    @Override
    public void onTopCardRemoved(int position) {
        Platform.runLater(() -> {
            if (topCardHbox == null || position >= topTribeCount) return;
            Node node = topCardHbox.getChildren().get(position);
            if (node == selectedCardView) clearCardSelection();
            topTribeCount--;
            // Mark as non-interactive immediately; the node is removed after the animation
            node.setMouseTransparent(true);
            animateRemove(node, topCardHbox, this::updateInteractionState);
        });
    }

    @Override
    public void onBottomCardRemoved(int position) {
        Platform.runLater(() -> {
            if (bottomCardHbox == null || position >= bottomTribeCount) return;
            Node node = bottomCardHbox.getChildren().get(position);
            if (node == selectedCardView) clearCardSelection();
            bottomTribeCount--;
            node.setMouseTransparent(true);
            animateRemove(node, bottomCardHbox, this::updateInteractionState);
        });
    }

    @Override
    public void onTopBuildRemoved(int position) {
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            int idx = topTribeCount + position;
            if (idx >= topCardHbox.getChildren().size()) return;
            Node node = topCardHbox.getChildren().get(idx);
            if (node == selectedCardView) clearCardSelection();
            node.setMouseTransparent(true);
            animateRemove(node, topCardHbox, null);
        });
    }

    @Override
    public void onBottomBuildRemoved(int position) {
        Platform.runLater(() -> {
            if (bottomCardHbox == null) return;
            int idx = bottomTribeCount + position;
            if (idx >= bottomCardHbox.getChildren().size()) return;
            Node node = bottomCardHbox.getChildren().get(idx);
            if (node == selectedCardView) clearCardSelection();
            node.setMouseTransparent(true);
            animateRemove(node, bottomCardHbox, null);
        });
    }

    @Override
    public void onTopCardRefreshed(List<CardDTO> top) {
        // Snapshot the bottom list now, before any scene-graph changes
        List<CardDTO> bottomSnap = new ArrayList<>(clientHandler.getBottomCards());
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            clearCardSelection();
            if (extraDrawActive) {
                topTribeCount = top.size();
                pendingBottomCards = bottomSnap; // captured before any concurrent modification
                return;
            }
            javafx.scene.Scene scene = topCardHbox.getScene();
            if (scene == null) { doTopCardRefresh(top, bottomSnap); return; }
            Pane root = (Pane) scene.getRoot();

            // Collect all visible tribe cards (no buildings, no already-fading nodes)
            List<Node> topTribeNodes = topCardHbox.getChildren().stream()
                    .filter(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO))
                    .toList();
            List<ImageView> floaters = snapshotFloaters(root, topTribeNodes);

            // Replace top tribe cards with the new list
            topCardHbox.getChildren().removeIf(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO));
            topTribeCount = 0;
            for (int i = 0; i < top.size(); i++) {
                topCardHbox.getChildren().add(i, CardImageFactory.cardImageView(top.get(i), cardFitHeight));
                topTribeCount++;
            }
            // Clear old bottom tribe cards; the new ones are added after the fly animation
            bottomCardHbox.getChildren().removeIf(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO));
            bottomTribeCount = 0;
            updateInteractionState();
            for (int i = 0; i < topTribeCount; i++) fadeInNode(topCardHbox.getChildren().get(i));

            flyToBottomThenFadeIn(root, floaters, () -> {
                List<ImageView> nodes = new ArrayList<>();
                for (int i = 0; i < bottomSnap.size(); i++) {
                    ImageView iv = CardImageFactory.cardImageView(bottomSnap.get(i), cardFitHeight);
                    bottomCardHbox.getChildren().add(i, iv);
                    nodes.add(iv);
                }
                bottomTribeCount = bottomSnap.size();
                updateInteractionState();
                nodes.forEach(this::fadeInNode);
            });
        });
    }

    /**
     * Fallback for when the scene is not yet attached: replaces both rows without animations.
     */
    private void doTopCardRefresh(List<CardDTO> top, List<CardDTO> bottomSnap) {
        topCardHbox.getChildren().removeIf(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO));
        topTribeCount = 0;
        for (int i = 0; i < top.size(); i++) {
            topCardHbox.getChildren().add(i, CardImageFactory.cardImageView(top.get(i), cardFitHeight));
            topTribeCount++;
        }
        bottomCardHbox.getChildren().removeIf(n -> n.getUserData() instanceof CardDTO && !(n.getUserData() instanceof BuildingDTO));
        bottomTribeCount = 0;
        for (int i = 0; i < bottomSnap.size(); i++) {
            bottomCardHbox.getChildren().add(i, CardImageFactory.cardImageView(bottomSnap.get(i), cardFitHeight));
            bottomTribeCount++;
        }
        updateInteractionState();
    }

    @Override
    public void onTopBuildingRefreshed(List<BuildingDTO> topBld) {
        List<BuildingDTO> topSnap = new ArrayList<>(topBld);
        List<BuildingDTO> botSnap = new ArrayList<>(clientHandler.getBottomBuildings());
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            clearCardSelection();
            if (extraDrawActive) {
                pendingBottomBuildings = botSnap; // captured before any concurrent modification
                return;
            }
            javafx.scene.Scene scene = topCardHbox.getScene();
            if (scene == null) { doTopBuildingRefresh(topSnap, botSnap); return; }
            Pane root = (Pane) scene.getRoot();

            // Collect all visible building nodes from the top row
            List<Node> topBldNodes = topCardHbox.getChildren().stream()
                    .filter(n -> n.getUserData() instanceof BuildingDTO)
                    .toList();
            List<ImageView> floaters = snapshotFloaters(root, topBldNodes);

            // Replace top buildings
            if (topCardHbox.getChildren().size() > topTribeCount)
                topCardHbox.getChildren().remove(topTribeCount, topCardHbox.getChildren().size());
            for (BuildingDTO bld : topSnap) topCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));

            // Clear old bottom buildings; the new ones are added after the fly animation
            if (bottomCardHbox.getChildren().size() > bottomTribeCount)
                bottomCardHbox.getChildren().remove(bottomTribeCount, bottomCardHbox.getChildren().size());
            updateInteractionState();
            for (int i = topTribeCount; i < topCardHbox.getChildren().size(); i++) fadeInNode(topCardHbox.getChildren().get(i));

            flyToBottomThenFadeIn(root, floaters, () -> {
                List<ImageView> nodes = new ArrayList<>();
                for (BuildingDTO bld : botSnap) {
                    ImageView iv = CardImageFactory.buildingImageView(bld, cardFitHeight);
                    bottomCardHbox.getChildren().add(iv);
                    nodes.add(iv);
                }
                updateInteractionState();
                nodes.forEach(this::fadeInNode);
            });
        });
    }

    /** Fallback for when the scene is not yet attached: replaces both rows without animations. */
    private void doTopBuildingRefresh(List<BuildingDTO> topSnapshot, List<BuildingDTO> bottomSnapshot) {
        if (topCardHbox.getChildren().size() > topTribeCount)
            topCardHbox.getChildren().remove(topTribeCount, topCardHbox.getChildren().size());
        for (BuildingDTO bld : topSnapshot)
            topCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));
        if (bottomCardHbox.getChildren().size() > bottomTribeCount)
            bottomCardHbox.getChildren().remove(bottomTribeCount, bottomCardHbox.getChildren().size());
        for (BuildingDTO bld : bottomSnapshot)
            bottomCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));
        updateInteractionState();
    }

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

    @Override
    public void onDefaultTileOrderChanged(List<PlayerDTO> order) {
        Platform.runLater(() -> refreshDefaultTileOverlay(order));
    }

    /** Redraws totem icons on the default tile according to the given turn order. */
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

    @Override
    public void onActionAvailableChanged(int drawTop, int drawBot) {
        Platform.runLater(() -> {
            if (isMyPlayingTurn()) {
                if (drawTopLabel != null) drawTopLabel.setText("Pesca da sopra: " + drawTop);
                if (drawBotLabel != null) drawBotLabel.setText("Pesca da sotto: " + drawBot);
            } else {
                drawTopLabel.setText("Pesca da sopra: -");
                drawBotLabel.setText("Pesca da sotto: -");
            }
            updateInteractionState();
        });
    }

    @Override
    public void onPlayerAdded(PlayerDTO player) {
        GUIObserver.super.onPlayerAdded(player);
    }

    @Override
    public void onError(String message) {
        Platform.runLater(() -> GUIEffects.showError(message));
    }

    @Override
    public void onPlayerDisconnected(String nickname) {
        Platform.runLater(() -> disconnectPopup.addDisconnection(nickname));
    }

    @Override
    public void onPlayerPPChanged(String n, int p) {
        if (playerDTO.getNickName().equals(n))
            Platform.runLater(() -> { if (prestigePointLabel != null) prestigePointLabel.setText("Punti Prestigio: " + p); });
    }

    @Override
    public void onPlayerFoodChanged(String n, int f) {
        if (playerDTO.getNickName().equals(n)) {
            playerDTO.setFood(f);
            Platform.runLater(() -> {
                if (foodLabel != null) foodLabel.setText("Cibo: " + f);
                updateInteractionState();
            });
        }
    }

    @Override
    public void onCardAddedToTribe(String nickname, CardDTO card) {
        if (playerDTO.getNickName().equals(nickname))
            Platform.runLater(this::updatePlayerStatsLabels);
    }

    @Override
    public void onEventResolved(int eventID, EVENT_TYPE eventType) {
        Platform.runLater(() -> {
            if (extraDrawActive) {
                deferredUiActions.add(() -> eventPopup.addEvent(eventID, eventType, cardFitHeight));
            } else {
                eventPopup.addEvent(eventID, eventType, cardFitHeight);
            }
        });
    }

    @Override
    public void onAskExtraDraw(List<CardDTO> cards, List<BuildingDTO> buildings) {
        Platform.runLater(() -> {
            clearCardSelection();
            extraDrawActive = true;
            topCardHbox.getChildren().clear();
            extraDrawTribeCount = 0;
            for (CardDTO card : cards) {
                topCardHbox.getChildren().add(CardImageFactory.cardImageView(card, cardFitHeight));
                extraDrawTribeCount++;
            }
            for (BuildingDTO bld : buildings) {
                topCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));
            }
            updateInteractionState();
            showExtraDrawPopup();
        });
    }

    @Override
    public void onWinners(List<PlayerDTO> w) {
        List<PlayerDTO> allPlayers = new java.util.ArrayList<>(clientHandler.getPlayers());
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/FXML/EndGame.fxml"));
                it.polimi.ingsw.am25.client.GUI.EndGameController ctrl =
                        new it.polimi.ingsw.am25.client.GUI.EndGameController();
                loader.setController(ctrl);
                javafx.scene.Parent root = loader.load();
                ctrl.setData(w, allPlayers);
                javafx.stage.Stage stage = (javafx.stage.Stage) tileHbox.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("IS26-AM25 — Fine Partita");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    // =========================================================
    // RENDER
    // =========================================================

    /** Populates both card rows from scratch (called on market initialization). */
    private void renderCards(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld) {
        topCardHbox.getChildren().clear();
        bottomCardHbox.getChildren().clear();
        selectedCardView = null;

        topTribeCount = top.size();
        for (CardDTO card : top) topCardHbox.getChildren().add(CardImageFactory.cardImageView(card, cardFitHeight));
        for (BuildingDTO bld : topBld) topCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));

        bottomTribeCount = bot.size();
        for (CardDTO card : bot) bottomCardHbox.getChildren().add(CardImageFactory.cardImageView(card, cardFitHeight));

        updateInteractionState();
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
        updateInteractionState();
    }

    // =========================================================
    // INTERACTION STATE
    // =========================================================

    /** Returns true when the current phase is a placing phase (either normal or last round). */
    private boolean isPlacingPhase() {
        GAME_PHASE phase = clientHandler.getGamePhase();
        return phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE;
    }

    /** Returns true when it is this player's turn to place their totem. */
    private boolean isMyPlacingTurn() {
        return isPlacingPhase() && playerDTO.getNickName().equals(clientHandler.getPlayerToPlace());
    }

    /** Returns true when it is this player's turn to pick a card. */
    private boolean isMyPlayingTurn() {
        return !isPlacingPhase() && playerDTO.getNickName().equals(clientHandler.getPlayerToPlay());
    }

    /**
     * Central method that recalculates the enabled/disabled and visual state of every
     * interactive element (tiles, cards, buttons) based on the current game phase and turn.
     */
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

        placeTotemButton.setDisable(!myPlacing || selectedTilePosition == -1);
        selectCardButton.setDisable((!myPlaying && !extraDrawActive) || selectedCardView == null);
        if (extraDrawActive) {
            skipTurnButton.setDisable(false);
        } else {
            boolean hasActions = clientHandler.getDrawTop() > 0 || clientHandler.getDrawBot() > 0;
            skipTurnButton.setDisable(!myPlaying || !hasActions || hasSelectableTribeCard());
        }
    }

    /**
     * Returns true if there is at least one non-event tribe card available to pick
     * in a row that has remaining draw actions.
     */
    private boolean hasSelectableTribeCard() {
        if (clientHandler.getDrawTop() > 0 && tribeCardExistsInRow(topCardHbox, topTribeCount)) return true;
        if (clientHandler.getDrawBot() > 0 && tribeCardExistsInRow(bottomCardHbox, bottomTribeCount)) return true;
        return false;
    }

    /** Checks whether a non-event tribe card exists among the first {@code tribeCount} children of the row. */
    private boolean tribeCardExistsInRow(HBox row, int tribeCount) {
        for (int i = 0; i < tribeCount && i < row.getChildren().size(); i++) {
            Node node = row.getChildren().get(i);
            if (node.getUserData() instanceof CardDTO dto && dto.getCardType() != CARD_TYPE.EVENT) return true;
        }
        return false;
    }

    /** Refreshes the shaman-star and builder-discount labels from the live player data. */
    private void updatePlayerStatsLabels() {
        int discount = totalBuilderDiscount();
        int stars = totalShamanStars();
        if (builderDiscountLabel != null) builderDiscountLabel.setText("Sconto Costruttori: " + discount);
        if (shamanStarLabel != null)      shamanStarLabel.setText("Stelle sciamano: " + stars);
        updateInteractionState();
    }

    /** Sums the star values of all shaman cards in the local player's tribe. */
    private int totalShamanStars() {
        return clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(playerDTO.getNickName()))
                .findFirst()
                .map(p -> p.getCardDtoList().stream()
                        .filter(c -> c.getCardType() == CARD_TYPE.SHAMAN)
                        .mapToInt(c -> switch (c.getStarNumber()) {
                            case ONE   -> 1;
                            case TWO   -> 2;
                            case THREE -> 3;
                        })
                        .sum())
                .orElse(0);
    }

    /** Sums the food-discount values of all builder cards in the local player's tribe. */
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
                    tt.setShowDelay(new Duration( 300));
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

    /** Toggles card selection; applies or removes the gold glow effect. */
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

    /** Removes the gold glow from the currently selected card and resets selection state. */
    private void clearCardSelection() {
        if (selectedCardView != null) {
            selectedCardView.setEffect(null);
            selectedCardView = null;
        }
        if (selectCardButton != null) selectCardButton.setDisable(true);
    }

    /** Builds and shows the extra-draw banner centered at the top of the current window. */
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

    /** Hides the extra-draw banner if it is currently visible. */
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
        Image totemImg = new Image(getClass().getResourceAsStream(CardImageFactory.totemPath(color)));
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

    /** Looks up the totem color for the given player nickname. Defaults to RED if not found. */
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
        if (tileHbox.getScene() == null) { if (onFinish != null) onFinish.run(); return; }
        Pane root = (Pane) tileHbox.getScene().getRoot();
        Point2D srcRoot = root.sceneToLocal(srcScene);
        Point2D dstRoot = root.sceneToLocal(dstScene);

        Image totemImg = new Image(getClass().getResourceAsStream(CardImageFactory.totemPath(color)));
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
        tt.setOnFinished(e -> { root.getChildren().remove(iv); if (onFinish != null) onFinish.run(); });
        tt.play();
    }
}
