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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileController implements GUIObserver {

    // --- pending data (arriva prima che la UI sia pronta) ---
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

    // --- posizioni totem (frazione larghezza/altezza tile) ---
    private static final double OFFER_TOTEM_X = 0.50;
    private static final double OFFER_TOTEM_Y = 0.27;
    private static final Map<Integer, double[]> DEFAULT_SLOT_Y = Map.of(
            2, new double[]{0.30, 0.48},
            3, new double[]{0.27, 0.43, 0.61},
            4, new double[]{0.21, 0.39, 0.57, 0.76},
            5, new double[]{0.15, 0.31, 0.49, 0.68, 0.86}
    );

    // --- flag render unico delle tile ---
    private boolean tilesRendered = false;

    // --- selezione tile ---
    private int selectedTilePosition = -1;
    private javafx.scene.layout.StackPane selectedTilePane = null;
    private int previewTilePosition = -1;
    private boolean previewAnimating = false;

    // --- overlay totem ---
    private Pane defaultTileOverlay = null;
    private double defaultTileWidth = 0;
    private final Map<Integer, Pane> offerTileOverlays = new HashMap<>();
    private final Map<Integer, Double> offerTileWidths = new HashMap<>();

    // --- selezione carta ---
    private ImageView selectedCardView = null;
    private int topTribeCount = 0;
    private int bottomTribeCount = 0;

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

    public TileController(ClientVirtualView clientHandler, ServerRemoteInterface serverRemoteInterface, PlayerDTO playerDTO) {
        this.clientHandler = clientHandler;
        this.serverRemoteInterface = serverRemoteInterface;
        this.playerDTO = playerDTO;
        clientHandler.addGUIObserver(this);
    }

    // =========================================================
    // FXML
    // =========================================================

    @FXML
    public void initialize() {
        double availableH = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        cardFitHeight = availableH / 3 - 100;
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

    @FXML
    private void handleSelectCard() {
        if (selectedCardView == null) return;
        try {
            CardDTO dto = (CardDTO) selectedCardView.getUserData();
            CARD_TYPE type = dto.getCardType();
            if (topCardHbox.getChildren().contains(selectedCardView)) {
                int idx = topCardHbox.getChildren().indexOf(selectedCardView);
                int pos = idx < topTribeCount ? idx : idx - topTribeCount;
                serverRemoteInterface.selectCardFromTopList(playerDTO, type, pos);
            } else {
                int idx = bottomCardHbox.getChildren().indexOf(selectedCardView);
                int pos = idx < bottomTribeCount ? idx : idx - bottomTribeCount;
                serverRemoteInterface.selectCardFromBottomList(playerDTO, type, pos);
            }
            clearCardSelection();
        } catch (Exception e) {
            GUIEffects.showError(e.getMessage());
        }
    }

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

    @FXML
    private void handleSkipTurn() {
        try {
            serverRemoteInterface.playerDoNothing(playerDTO);
        } catch (Exception e) {
            GUIEffects.showError(e.getMessage());
        }
    }

    @FXML
    private void showThisPlayerTribe() {
        //TODO
    }

    @FXML
    private void showPlayerStatus() {
        //TODO
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
            Point2D scenePos = node.localToScene(0, 0);
            topCardHbox.getChildren().remove(position);
            topTribeCount--;
            updateInteractionState();
            fadeOutFloating(node, scenePos);
        });
    }

    @Override
    public void onBottomCardRemoved(int position) {
        Platform.runLater(() -> {
            if (bottomCardHbox == null || position >= bottomTribeCount) return;
            Node node = bottomCardHbox.getChildren().get(position);
            if (node == selectedCardView) clearCardSelection();
            Point2D scenePos = node.localToScene(0, 0);
            bottomCardHbox.getChildren().remove(position);
            bottomTribeCount--;
            updateInteractionState();
            fadeOutFloating(node, scenePos);
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
            Point2D scenePos = node.localToScene(0, 0);
            topCardHbox.getChildren().remove(idx);
            fadeOutFloating(node, scenePos);
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
            Point2D scenePos = node.localToScene(0, 0);
            bottomCardHbox.getChildren().remove(idx);
            fadeOutFloating(node, scenePos);
        });
    }

    @Override
    public void onTopCardRefreshed(List<CardDTO> top) {
        List<CardDTO> bottomSnapshot = new java.util.ArrayList<>(clientHandler.getBottomCards());
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            clearCardSelection();

            javafx.scene.Scene scene = topCardHbox.getScene();
            if (scene == null) {
                doTopCardRefresh(top, bottomSnapshot);
                return;
            }
            Pane root = (Pane) scene.getRoot();
            Point2D botRoot = root.sceneToLocal(bottomCardHbox.localToScene(0, 0));

            List<ImageView> floaters = new ArrayList<>();
            for (int i = 0; i < topTribeCount && i < topCardHbox.getChildren().size(); i++) {
                Node node = topCardHbox.getChildren().get(i);
                if (!(node.getUserData() instanceof CardDTO card)) continue;
                Point2D nodeRoot = root.sceneToLocal(node.localToScene(0, 0));
                ImageView floater = CardImageFactory.cardImageView(card, cardFitHeight);
                floater.setMouseTransparent(true);
                floater.setLayoutX(nodeRoot.getX());
                floater.setLayoutY(nodeRoot.getY());
                root.getChildren().add(floater);
                floaters.add(floater);
            }

            if (topTribeCount > 0) topCardHbox.getChildren().remove(0, topTribeCount);
            topTribeCount = 0;
            List<Node> newTopNodes = new ArrayList<>();
            for (int i = 0; i < top.size(); i++) {
                ImageView iv = CardImageFactory.cardImageView(top.get(i), cardFitHeight);
                topCardHbox.getChildren().add(i, iv);
                newTopNodes.add(iv);
                topTribeCount++;
            }
            if (bottomTribeCount > 0) bottomCardHbox.getChildren().remove(0, bottomTribeCount);
            bottomTribeCount = 0;
            updateInteractionState();
            newTopNodes.forEach(this::fadeInNode);

            if (floaters.isEmpty()) {
                for (int i = 0; i < bottomSnapshot.size(); i++)
                    bottomCardHbox.getChildren().add(i, CardImageFactory.cardImageView(bottomSnapshot.get(i), cardFitHeight));
                bottomTribeCount = bottomSnapshot.size();
                updateInteractionState();
                return;
            }

            int[] remaining = {floaters.size()};
            for (ImageView floater : floaters) {
                double deltaY = botRoot.getY() - floater.getLayoutY();
                TranslateTransition tt = new TranslateTransition(Duration.millis(450), floater);
                tt.setByY(deltaY);
                tt.setInterpolator(Interpolator.EASE_BOTH);
                tt.setOnFinished(e -> {
                    root.getChildren().remove(floater);
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        for (int i = 0; i < bottomSnapshot.size(); i++)
                            bottomCardHbox.getChildren().add(i, CardImageFactory.cardImageView(bottomSnapshot.get(i), cardFitHeight));
                        bottomTribeCount = bottomSnapshot.size();
                        updateInteractionState();
                    }
                });
                tt.play();
            }
        });
    }

    private void doTopCardRefresh(List<CardDTO> top, List<CardDTO> bottomSnapshot) {
        if (topTribeCount > 0) topCardHbox.getChildren().remove(0, topTribeCount);
        topTribeCount = 0;
        for (int i = 0; i < top.size(); i++) {
            topCardHbox.getChildren().add(i, CardImageFactory.cardImageView(top.get(i), cardFitHeight));
            topTribeCount++;
        }
        if (bottomTribeCount > 0) bottomCardHbox.getChildren().remove(0, bottomTribeCount);
        bottomTribeCount = 0;
        for (int i = 0; i < bottomSnapshot.size(); i++) {
            bottomCardHbox.getChildren().add(i, CardImageFactory.cardImageView(bottomSnapshot.get(i), cardFitHeight));
            bottomTribeCount++;
        }
        updateInteractionState();
    }

    @Override
    public void onTopBuildingRefreshed(List<BuildingDTO> topBld) {
        List<BuildingDTO> topSnapshot = new java.util.ArrayList<>(topBld);
        List<BuildingDTO> bottomSnapshot = new java.util.ArrayList<>(clientHandler.getBottomBuildings());
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            clearCardSelection();

            javafx.scene.Scene scene = topCardHbox.getScene();
            if (scene == null) {
                doTopBuildingRefresh(topSnapshot, bottomSnapshot);
                return;
            }
            Pane root = (Pane) scene.getRoot();
            Point2D botRoot = root.sceneToLocal(bottomCardHbox.localToScene(0, 0));

            List<ImageView> floaters = new ArrayList<>();
            for (int i = topTribeCount; i < topCardHbox.getChildren().size(); i++) {
                Node node = topCardHbox.getChildren().get(i);
                if (!(node.getUserData() instanceof BuildingDTO bld)) continue;
                Point2D nodeRoot = root.sceneToLocal(node.localToScene(0, 0));
                ImageView floater = CardImageFactory.buildingImageView(bld, cardFitHeight);
                floater.setMouseTransparent(true);
                floater.setLayoutX(nodeRoot.getX());
                floater.setLayoutY(nodeRoot.getY());
                root.getChildren().add(floater);
                floaters.add(floater);
            }

            if (topCardHbox.getChildren().size() > topTribeCount)
                topCardHbox.getChildren().remove(topTribeCount, topCardHbox.getChildren().size());
            List<Node> newTopBldNodes = new ArrayList<>();
            for (BuildingDTO bld : topSnapshot) {
                ImageView iv = CardImageFactory.buildingImageView(bld, cardFitHeight);
                topCardHbox.getChildren().add(iv);
                newTopBldNodes.add(iv);
            }
            if (bottomCardHbox.getChildren().size() > bottomTribeCount)
                bottomCardHbox.getChildren().remove(bottomTribeCount, bottomCardHbox.getChildren().size());
            updateInteractionState();
            newTopBldNodes.forEach(this::fadeInNode);

            if (floaters.isEmpty()) {
                for (BuildingDTO bld : bottomSnapshot)
                    bottomCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));
                updateInteractionState();
                return;
            }

            int[] remaining = {floaters.size()};
            for (ImageView floater : floaters) {
                double deltaY = botRoot.getY() - floater.getLayoutY();
                TranslateTransition tt = new TranslateTransition(Duration.millis(450), floater);
                tt.setByY(deltaY);
                tt.setInterpolator(Interpolator.EASE_BOTH);
                tt.setOnFinished(e -> {
                    root.getChildren().remove(floater);
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        for (BuildingDTO bld : bottomSnapshot)
                            bottomCardHbox.getChildren().add(CardImageFactory.buildingImageView(bld, cardFitHeight));
                        updateInteractionState();
                    }
                });
                tt.play();
            }
        });
    }

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
                previewTilePosition = -1;
                return;
            }
            if (isMe) previewTilePosition = -1;
            if (fromSlot < 0 || defaultTileOverlay == null) {
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

    private void refreshDefaultTileOverlay(List<PlayerDTO> order) {
        if (defaultTileOverlay == null) return;
        defaultTileOverlay.getChildren().clear();
        int totalPlayers = clientHandler.getPlayers().size();
        double[] ySlots = DEFAULT_SLOT_Y.getOrDefault(totalPlayers, new double[]{0.5});
        for (int i = 0; i < order.size(); i++) {
            PlayerDTO p = order.get(i);
            if (p == null) continue;
            if (previewTilePosition >= 0 && p.getNickName().equals(playerDTO.getNickName())) continue;
            double yFrac = i < ySlots.length ? ySlots[i] : ySlots[ySlots.length - 1];
            placeTotemOnOverlay(defaultTileOverlay, p.getNickName(), 0.50, yFrac, defaultTileWidth);
        }
    }

    @Override
    public void onActionAvailableChanged(int drawTop, int drawBot) {
        Platform.runLater(() -> {
            if (drawTopLabel != null) drawTopLabel.setText("Pesca da sopra: " + drawTop);
            if (drawBotLabel != null) drawBotLabel.setText("Pesca da sotto: " + drawBot);
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
        Platform.runLater(() -> eventPopup.addEvent(eventID, eventType, cardFitHeight));
    }

    @Override
    public void onWinners(List<PlayerDTO> w) {
        GUIObserver.super.onWinners(w);
        //TODO: aggiungere schermata visualizzazione vincitori e classifica dal database: DANIELE
    }

    // =========================================================
    // RENDER
    // =========================================================

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

    private void renderTiles(List<OffertileDTO> tiles, List<DefaultTileDTO> defs) {
        if (tilesRendered) return;
        tilesRendered = true;
        tileHbox.getChildren().clear();
        clearTileSelection();
        offerTileOverlays.clear();
        offerTileWidths.clear();

        Image defImg = new Image(getClass().getResourceAsStream(
                "/images/Tiles/defaultTile/" + defs.size() + "plDefTile.png"));
        ImageView defIv = new ImageView(defImg);
        defIv.setFitHeight(cardFitHeight);
        defIv.setPreserveRatio(true);
        double defW = cardFitHeight * defImg.getWidth() / defImg.getHeight();
        defaultTileOverlay = new Pane();
        defaultTileOverlay.setPrefSize(defW, cardFitHeight);
        defaultTileOverlay.setMouseTransparent(true);
        defaultTileWidth = defW;
        javafx.scene.layout.StackPane defStack = new javafx.scene.layout.StackPane(defIv, defaultTileOverlay);
        defStack.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        tileHbox.getChildren().add(defStack);

        for (int i = 0; i < tiles.size(); i++) {
            Image img = new Image(getClass().getResourceAsStream(
                    "/images/Tiles/offertiles/" + tiles.get(i).getOfferTileID() + "offertile.png"));
            ImageView iv = new ImageView(img);
            iv.setFitHeight(cardFitHeight);
            iv.setPreserveRatio(true);
            double w = cardFitHeight * img.getWidth() / img.getHeight();
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

    private boolean isPlacingPhase() {
        GAME_PHASE phase = clientHandler.getGamePhase();
        return phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE;
    }

    private boolean isMyPlacingTurn() {
        return isPlacingPhase() && playerDTO.getNickName().equals(clientHandler.getPlayerToPlace());
    }

    private boolean isMyPlayingTurn() {
        return !isPlacingPhase() && playerDTO.getNickName().equals(clientHandler.getPlayerToPlay());
    }

    private void updateInteractionState() {
        if (placeTotemButton == null || tileHbox == null) return;

        boolean myPlacing = isMyPlacingTurn();
        boolean myPlaying = isMyPlayingTurn();

        Map<Integer, String> occupants = clientHandler.getOfferTileOccupants();
        for (int i = 0; i < tileHbox.getChildren().size(); i++) {
            javafx.scene.layout.StackPane sp = (javafx.scene.layout.StackPane) tileHbox.getChildren().get(i);
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

        applyCardRowState(topCardHbox, myPlaying && clientHandler.getDrawTop() > 0);
        applyCardRowState(bottomCardHbox, myPlaying && clientHandler.getDrawBot() > 0);
        if (!myPlaying) clearCardSelection();

        placeTotemButton.setDisable(!myPlacing || selectedTilePosition == -1);
        selectCardButton.setDisable(!myPlaying || selectedCardView == null);
        boolean hasActions = clientHandler.getDrawTop() > 0 || clientHandler.getDrawBot() > 0;
        skipTurnButton.setDisable(!myPlaying || !hasActions || hasSelectableTribeCard());
    }

    private boolean hasSelectableTribeCard() {
        if (clientHandler.getDrawTop() > 0 && tribeCardExistsInRow(topCardHbox, topTribeCount)) return true;
        if (clientHandler.getDrawBot() > 0 && tribeCardExistsInRow(bottomCardHbox, bottomTribeCount)) return true;
        return false;
    }

    private boolean tribeCardExistsInRow(HBox row, int tribeCount) {
        for (int i = 0; i < tribeCount && i < row.getChildren().size(); i++) {
            Node node = row.getChildren().get(i);
            if (node.getUserData() instanceof CardDTO dto && dto.getCardType() != CARD_TYPE.EVENT) return true;
        }
        return false;
    }

    private void updatePlayerStatsLabels() {
        int discount = totalBuilderDiscount();
        int stars = totalShamanStars();
        if (builderDiscountLabel != null) builderDiscountLabel.setText("Sconto Costruttori: " + discount);
        if (shamanStarLabel != null)      shamanStarLabel.setText("Stelle sciamano: " + stars);
        updateInteractionState();
    }

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

    private boolean canAfford(CardDTO card) {
        if (!(card instanceof BuildingDTO bld)) return true;
        int cost = bld.getFoodCost() - totalBuilderDiscount();
        return playerDTO.getFood() >= Math.max(0, cost);
    }

    private void applyCardRowState(HBox row, boolean rowActive) {
        for (Node node : row.getChildren()) {
            if (!(node.getUserData() instanceof CardDTO dto)) continue;
            boolean selectable = rowActive && dto.getCardType() != CARD_TYPE.EVENT && canAfford(dto);

            if (node == selectedCardView) {
                // mantieni il glow anche se non selezionabile
            } else if (!selectable) {
                node.setEffect(GUIEffects.GRAY);
                node.setOpacity(0.70);
            } else {
                node.setEffect(null);
                node.setOpacity(1.0);
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

    private void selectTile(javafx.scene.layout.StackPane sp, int position) {
        if (previewAnimating) return;
        COLOR myColor = colorOf(playerDTO.getNickName());
        if (selectedTilePane != null) selectedTilePane.setEffect(null);

        if (selectedTilePane == sp) {
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
            int old = previewTilePosition;
            sp.setEffect(GUIEffects.goldGlow());
            selectedTilePane = sp;
            selectedTilePosition = position;
            previewTilePosition = position;
            Pane newOverlay = offerTileOverlays.get(position);
            if (old >= 0) {
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

    private void clearCardSelection() {
        if (selectedCardView != null) {
            selectedCardView.setEffect(null);
            selectedCardView = null;
        }
        if (selectCardButton != null) selectCardButton.setDisable(true);
    }

    // =========================================================
    // TOTEM
    // =========================================================

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

    private COLOR colorOf(String nickname) {
        return clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(nickname))
                .findFirst()
                .map(PlayerDTO::getColorTotem)
                .orElse(COLOR.RED);
    }

    private Point2D defaultSlotScene(int slot) {
        int totalPlayers = clientHandler.getPlayers().size();
        double[] ySlots = DEFAULT_SLOT_Y.getOrDefault(totalPlayers, new double[]{0.5});
        double yFrac = slot < ySlots.length ? ySlots[slot] : ySlots[ySlots.length - 1];
        return defaultTileOverlay.localToScene(defaultTileWidth * 0.50, cardFitHeight * yFrac);
    }

    private Point2D offerSlotScene(int tilePos) {
        Pane overlay = offerTileOverlays.get(tilePos);
        if (overlay == null) return new Point2D(0, 0);
        double w = offerTileWidths.getOrDefault(tilePos, cardFitHeight);
        return overlay.localToScene(w * OFFER_TOTEM_X, cardFitHeight * OFFER_TOTEM_Y);
    }

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

    private void fadeInNode(Node node) {
        double target = node.getOpacity();
        node.setOpacity(0.0);
        FadeTransition ft = new FadeTransition(Duration.millis(400), node);
        ft.setFromValue(0.0);
        ft.setToValue(target);
        ft.play();
    }

    private void fadeOutFloating(Node original, Point2D nodeScenePos) {
        if (tileHbox.getScene() == null) return;
        Pane root = (Pane) tileHbox.getScene().getRoot();
        Point2D rootPos = root.sceneToLocal(nodeScenePos);

        ImageView floater;
        if (original.getUserData() instanceof BuildingDTO bld) {
            floater = CardImageFactory.buildingImageView(bld, cardFitHeight);
        } else if (original.getUserData() instanceof CardDTO card) {
            floater = CardImageFactory.cardImageView(card, cardFitHeight);
        } else return;

        floater.setLayoutX(rootPos.getX());
        floater.setLayoutY(rootPos.getY());
        floater.setMouseTransparent(true);
        root.getChildren().add(floater);

        FadeTransition ft = new FadeTransition(Duration.millis(300), floater);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> root.getChildren().remove(floater));
        ft.play();
    }

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
