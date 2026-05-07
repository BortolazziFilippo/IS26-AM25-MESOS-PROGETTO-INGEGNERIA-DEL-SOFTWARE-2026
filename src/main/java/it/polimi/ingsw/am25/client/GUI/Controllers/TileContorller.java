package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.GUI.GUIObserver;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TileContorller implements GUIObserver {

    // --- pending data (arriva prima che la UI sia pronta) ---
    private List<CardDTO> pendingTopTribeCard;
    private List<BuildingDTO> pendingTopBuildingCard;
    private List<CardDTO> pendingBottomTribeCard;
    private List<DefaultTileDTO> pendingDefs;
    private List<OffertileDTO> pendingOffertiles;

    private ClientVirtualView clienHandler;
    private ServerRemoteInterface serverRemoteInterface;
    private PlayerDTO playerDTO;

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

    // --- overlay totem ---
    private javafx.scene.layout.Pane defaultTileOverlay = null;
    private double defaultTileWidth = 0;
    private final Map<Integer, javafx.scene.layout.Pane> offerTileOverlays = new HashMap<>();
    private final Map<Integer, Double> offerTileWidths = new HashMap<>();

    // --- popup eventi ---
    private Stage eventPopupStage = null;
    private VBox eventListBox = null;

    // --- selezione carta ---
    private ImageView selectedCardView = null;
    private int topTribeCount = 0;    // carte tribù in topCardHbox; building vengono dopo
    private int bottomTribeCount = 0; // carte tribù in bottomCardHbox; building vengono dopo

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
    private Button tribeVisualizerButton;
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


    public TileContorller(ClientVirtualView clienHandler, ServerRemoteInterface serverRemoteInterface, PlayerDTO playerDTO) {
        this.clienHandler = clienHandler;
        this.serverRemoteInterface = serverRemoteInterface;
        this.playerDTO = playerDTO;
        clienHandler.addGUIObserver(this);
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
            showError(e.getMessage());
        }
    }

    @FXML
    private void handlePlaceTotem() {
        if (selectedTilePosition == -1) return;
        try {
            serverRemoteInterface.placingPlayer(playerDTO, selectedTilePosition);
            clearTileSelection();
            placeTotemButton.setDisable(true);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleSkipTurn() {
        try {
            serverRemoteInterface.playerDoNothing(playerDTO);
        } catch (Exception e) {
            showError(e.getMessage());
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
            Node removed = topCardHbox.getChildren().remove(position);
            topTribeCount--;
            if (removed == selectedCardView) clearCardSelection();
            updateInteractionState();
        });
    }

    @Override
    public void onBottomCardRemoved(int position) {
        Platform.runLater(() -> {
            if (bottomCardHbox == null || position >= bottomCardHbox.getChildren().size()) return;
            Node removed = bottomCardHbox.getChildren().remove(position);
            bottomTribeCount--;
            if (removed == selectedCardView) clearCardSelection();
            updateInteractionState();
        });
    }

    @Override
    public void onTopBuildRemoved(int position) {
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            int idx = topTribeCount + position;
            if (idx >= topCardHbox.getChildren().size()) return;
            Node removed = topCardHbox.getChildren().remove(idx);
            if (removed == selectedCardView) clearCardSelection();
        });
    }

    @Override
    public void onTopCardRefreshed(List<CardDTO> top) {
        List<CardDTO> bottomSnapshot = new java.util.ArrayList<>(clienHandler.getBottomCards());

        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            clearCardSelection();

            // rimuovi solo le carte tribù (le prime topTribeCount); lascia gli edifici al loro posto
            if (topTribeCount > 0)
                topCardHbox.getChildren().remove(0, topTribeCount);
            topTribeCount = 0;
            for (int i = 0; i < top.size(); i++) {
                topCardHbox.getChildren().add(i, cardImageView(top.get(i)));
                topTribeCount++;
            }

            // rifai le tribù di bottom (gli edifici verranno aggiornati da onTopBuildingRefreshed)
            if (bottomTribeCount > 0)
                bottomCardHbox.getChildren().remove(0, bottomTribeCount);
            bottomTribeCount = 0;
            for (int i = 0; i < bottomSnapshot.size(); i++) {
                bottomCardHbox.getChildren().add(i, cardImageView(bottomSnapshot.get(i)));
                bottomTribeCount++;
            }

            updateInteractionState();
        });
    }

    @Override
    public void onTopBuildingRefreshed(List<BuildingDTO> topBld) {
        List<BuildingDTO> topSnapshot = new java.util.ArrayList<>(topBld);
        List<BuildingDTO> bottomSnapshot = new java.util.ArrayList<>(clienHandler.getBottomBuildings());
        Platform.runLater(() -> {
            if (topCardHbox == null) return;
            clearCardSelection();

            if (topCardHbox.getChildren().size() > topTribeCount)
                topCardHbox.getChildren().remove(topTribeCount, topCardHbox.getChildren().size());
            for (BuildingDTO bld : topSnapshot)
                topCardHbox.getChildren().add(buildingImageView(bld));

            if (bottomCardHbox.getChildren().size() > bottomTribeCount)
                bottomCardHbox.getChildren().remove(bottomTribeCount, bottomCardHbox.getChildren().size());
            for (BuildingDTO bld : bottomSnapshot)
                bottomCardHbox.getChildren().add(buildingImageView(bld));

            updateInteractionState();
        });
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
    public void onPlayerPlacedOnOfferTile(String nickname, int tilePosition) {
        Platform.runLater(() -> {
            javafx.scene.layout.Pane overlay = offerTileOverlays.get(tilePosition);
            if (overlay == null) return;
            double w = offerTileWidths.getOrDefault(tilePosition, cardFitHeight);
            overlay.getChildren().clear();
            placeTotemOnOverlay(overlay, nickname, OFFER_TOTEM_X, OFFER_TOTEM_Y, w);
        });
    }

    @Override
    public void onDefaultTileOrderChanged(List<PlayerDTO> order) {
        Platform.runLater(() -> refreshDefaultTileOverlay(order));
    }

    private void refreshDefaultTileOverlay(List<PlayerDTO> order) {
        if (defaultTileOverlay == null) return;
        defaultTileOverlay.getChildren().clear();
        int totalPlayers = clienHandler.getPlayers().size();
        double[] ySlots = DEFAULT_SLOT_Y.getOrDefault(totalPlayers, new double[]{0.5});
        for (int i = 0; i < order.size(); i++) {
            PlayerDTO p = order.get(i);
            if (p == null) continue;
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
        Platform.runLater(() -> showError(message));
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
        Platform.runLater(() -> addEventToPopup(eventID, eventType));
    }

    private void addEventToPopup(int eventID, EVENT_TYPE eventType) {
        if (eventPopupStage == null || !eventPopupStage.isShowing()) {
            eventListBox = new VBox(12);
            eventListBox.setPadding(new Insets(16));

            eventPopupStage = new Stage();
            eventPopupStage.setTitle("Eventi risolti");
            eventPopupStage.setScene(new Scene(eventListBox));
            eventPopupStage.setOnHidden(e -> { eventPopupStage = null; eventListBox = null; });
            eventPopupStage.show();
        }

        String path = "/images/Card/events/" + eventID + eventTypePath(eventType) + "Event.png";
        HBox row = new HBox(12);
        row.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 4 0;");
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(path)));
            iv.setFitHeight(cardFitHeight * 1.2);
            iv.setPreserveRatio(true);
            row.getChildren().add(iv);
        } catch (Exception ignored) {}
        Label lbl = new Label(eventType.toString());
        lbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        row.getChildren().add(lbl);
        eventListBox.getChildren().add(row);
        eventPopupStage.sizeToScene();
    }

    @Override
    public void onWinners(List<PlayerDTO> w) {
        GUIObserver.super.onWinners(w);
    }

    // =========================================================
    // RENDER
    // =========================================================

    private void renderCards(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld) {
        topCardHbox.getChildren().clear();
        bottomCardHbox.getChildren().clear();
        selectedCardView = null;

        topTribeCount = top.size();
        for (CardDTO card : top) topCardHbox.getChildren().add(cardImageView(card));
        for (BuildingDTO bld : topBld) topCardHbox.getChildren().add(buildingImageView(bld));

        bottomTribeCount = bot.size();
        for (CardDTO card : bot) bottomCardHbox.getChildren().add(cardImageView(card));

        updateInteractionState();
    }

    private void renderTiles(List<OffertileDTO> tiles, List<DefaultTileDTO> defs) {
        if (tilesRendered) return;
        tilesRendered = true;
        tileHbox.getChildren().clear();
        clearTileSelection();
        offerTileOverlays.clear();
        offerTileWidths.clear();

        // --- default tile ---
        Image defImg = new Image(getClass().getResourceAsStream(
                "/images/Tiles/defaultTile/" + defs.size() + "plDefTile.png"));
        ImageView defIv = new ImageView(defImg);
        defIv.setFitHeight(cardFitHeight);
        defIv.setPreserveRatio(true);
        double defW = cardFitHeight * defImg.getWidth() / defImg.getHeight();
        defaultTileOverlay = new javafx.scene.layout.Pane();
        defaultTileOverlay.setPrefSize(defW, cardFitHeight);
        defaultTileOverlay.setMouseTransparent(true);
        defaultTileWidth = defW;
        javafx.scene.layout.StackPane defStack = new javafx.scene.layout.StackPane(defIv, defaultTileOverlay);
        defStack.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        tileHbox.getChildren().add(defStack);

        // --- offer tiles ---
        for (int i = 0; i < tiles.size(); i++) {
            Image img = new Image(getClass().getResourceAsStream(
                    "/images/Tiles/offertiles/" + tiles.get(i).getOfferTileID() + "offertile.png"));
            ImageView iv = new ImageView(img);
            iv.setFitHeight(cardFitHeight);
            iv.setPreserveRatio(true);
            double w = cardFitHeight * img.getWidth() / img.getHeight();
            javafx.scene.layout.Pane overlay = new javafx.scene.layout.Pane();
            overlay.setPrefSize(w, cardFitHeight);
            overlay.setMouseTransparent(true);
            offerTileOverlays.put(i, overlay);
            offerTileWidths.put(i, w);
            javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(iv, overlay);
            stack.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            tileHbox.getChildren().add(stack);
        }
        List<PlayerDTO> initialOrder = clienHandler.getDefaultTileOrder();
        if (!initialOrder.isEmpty()) refreshDefaultTileOverlay(initialOrder);
        updateInteractionState();
    }

    // =========================================================
    // INTERACTION STATE
    // =========================================================

    private boolean isPlacingPhase() {
        GAME_PHASE phase = clienHandler.getGamePhase();
        return phase == GAME_PHASE.PLACING_PHASE || phase == GAME_PHASE.LAST_ROUND_PLACING_PHASE;
    }

    private boolean isMyPlacingTurn() {
        return isPlacingPhase() && playerDTO.getNickName().equals(clienHandler.getPlayerToPlace());
    }

    private boolean isMyPlayingTurn() {
        return !isPlacingPhase() && playerDTO.getNickName().equals(clienHandler.getPlayerToPlay());
    }

    private void updateInteractionState() {
        if (placeTotemButton == null || tileHbox == null) return;

        boolean myPlacing = isMyPlacingTurn();
        boolean myPlaying = isMyPlayingTurn();

        ColorAdjust gray = new ColorAdjust();
        gray.setSaturation(-0.8);
        gray.setBrightness(-0.2);

        // --- tile (indice 0 = default tile, mai cliccabile) ---
        Map<Integer, String> occupants = clienHandler.getOfferTileOccupants();
        for (int i = 0; i < tileHbox.getChildren().size(); i++) {
            javafx.scene.layout.StackPane sp = (javafx.scene.layout.StackPane) tileHbox.getChildren().get(i);
            boolean clickable = myPlacing && i > 0 && !occupants.containsKey(i - 1);
            if (clickable) {
                sp.setEffect(sp == selectedTilePane ? goldGlow() : null);
                sp.setOpacity(1.0);
                final int pos = i - 1;
                sp.setOnMouseClicked(e -> selectTile(sp, pos));
                sp.setStyle("-fx-cursor: hand;");
            } else {
                if (sp != selectedTilePane) sp.setEffect(myPlacing ? null : gray);
                sp.setOpacity(myPlacing ? 1.0 : 0.45);
                sp.setOnMouseClicked(null);
                sp.setStyle("");
            }
        }
        if (!myPlacing) clearTileSelection();

        // --- carte ---
        applyCardRowState(topCardHbox, myPlaying && clienHandler.getDrawTop() > 0);
        applyCardRowState(bottomCardHbox, myPlaying && clienHandler.getDrawBot() > 0);
        if (!myPlaying) clearCardSelection();

        // --- pulsanti ---
        placeTotemButton.setDisable(!myPlacing || selectedTilePosition == -1);
        selectCardButton.setDisable(!myPlaying || selectedCardView == null);
        boolean hasActions = clienHandler.getDrawTop() > 0 || clienHandler.getDrawBot() > 0;
        skipTurnButton.setDisable(!myPlaying || !hasActions || hasSelectableTribeCard());
    }

    private boolean hasSelectableTribeCard() {
        boolean topAvailable = clienHandler.getDrawTop() > 0;
        boolean botAvailable = clienHandler.getDrawBot() > 0;
        if (topAvailable && tribeCardExistsInRow(topCardHbox, topTribeCount)) return true;
        if (botAvailable && tribeCardExistsInRow(bottomCardHbox, bottomTribeCount)) return true;
        return false;
    }

    private boolean tribeCardExistsInRow(HBox row, int tribeCount) {
        for (int i = 0; i < tribeCount && i < row.getChildren().size(); i++) {
            Node node = row.getChildren().get(i);
            if (node.getUserData() instanceof CardDTO dto
                    && dto.getCardType() != CARD_TYPE.EVENT) return true;
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
        return clienHandler.getPlayers().stream()
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
        return clienHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(playerDTO.getNickName()))
                .findFirst()
                .map(p -> p.getCardDtoList().stream()
                        .filter(c -> c.getCardType() == CARD_TYPE.BUILDER)
                        .mapToInt(CardDTO::getFoodDiscount)
                        .sum())
                .orElse(0);
    }

    private boolean canAfford(CardDTO card) {
        if (card.getCardType() != CARD_TYPE.BUILDING) return true;
        int cost = ((BuildingDTO) card).getFoodCost() - totalBuilderDiscount();
        return playerDTO.getFood() >= Math.max(0, cost);
    }

    private void applyCardRowState(HBox row, boolean rowActive) {
        ColorAdjust gray = new ColorAdjust();
        gray.setSaturation(-0.8);
        gray.setBrightness(-0.2);

        for (Node node : row.getChildren()) {
            if (!(node.getUserData() instanceof CardDTO dto)) continue;
            boolean selectable = rowActive
                    && dto.getCardType() != CARD_TYPE.EVENT
                    && canAfford(dto);
            boolean active = rowActive && selectable;

            if (node == selectedCardView) {
                // mantieni il glow anche se non selezionabile (verrà pulito da clearCardSelection se serve)
            } else if (!rowActive || !selectable) {
                node.setEffect(gray);
                node.setOpacity(0.45);
            } else {
                node.setEffect(null);
                node.setOpacity(1.0);
            }

            if (active) {
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

    private void selectTile(javafx.scene.layout.StackPane sp, int position) {
        if (selectedTilePane != null) selectedTilePane.setEffect(null);
        if (selectedTilePane == sp) {
            selectedTilePane = null;
            selectedTilePosition = -1;
        } else {
            sp.setEffect(goldGlow());
            selectedTilePane = sp;
            selectedTilePosition = position;
        }
        placeTotemButton.setDisable(selectedTilePosition == -1);
    }

    private void selectCard(ImageView iv) {
        if (selectedCardView != null) selectedCardView.setEffect(null);
        if (selectedCardView == iv) {
            selectedCardView = null;
        } else {
            iv.setEffect(goldGlow());
            selectedCardView = iv;
        }
        selectCardButton.setDisable(selectedCardView == null);
    }

    private void clearTileSelection() {
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

    private void placeTotemOnOverlay(javafx.scene.layout.Pane overlay, String nickname,
                                     double xFrac, double yFrac, double tileW) {
        COLOR color = colorOf(nickname);
        Image totemImg = new Image(getClass().getResourceAsStream(totemPath(color)));
        double totemH = cardFitHeight * 0.20;
        double totemW = totemH * totemImg.getWidth() / totemImg.getHeight();
        ImageView iv = new ImageView(totemImg);
        iv.setFitHeight(totemH);
        iv.setPreserveRatio(true);
        iv.setRotate(90);
        // dopo rotate(90) il centro visivo rimane invariato rispetto al layoutX/Y
        iv.setLayoutX(tileW * xFrac - totemW / 2.0);
        iv.setLayoutY(cardFitHeight * yFrac - totemH / 2.0);
        overlay.getChildren().add(iv);
    }

    private COLOR colorOf(String nickname) {
        return clienHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(nickname))
                .findFirst()
                .map(PlayerDTO::getColorTotem)
                .orElse(COLOR.RED);
    }

    private String totemPath(COLOR color) {
        return switch (color) {
            case RED    -> "/images/totems/pedine_specs_redTotem.png";
            case BLUE   -> "/images/totems/pedine_specs_blueTotem.png";
            case YELLOW -> "/images/totems/pedine_specs_yellowTotem.png";
            case PURPLE -> "/images/totems/pedine_specs_purpleTotem.png";
            case WHITE  -> "/images/totems/pedine_specs_whiteTotem.png";
        };
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        Label content = new Label(message != null ? message : "Errore sconosciuto");
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: red; -fx-font-size: 16 px;-fx-font-weight: bald");
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private DropShadow goldGlow() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.GOLD);
        glow.setRadius(25);
        glow.setSpread(0.4);
        return glow;
    }

    // =========================================================
    // IMAGE VIEWS
    // =========================================================

    private ImageView cardImageView(CardDTO card) {
        String path = switch (card.getCardType()) {
            case GATHERER -> "/images/Card/gatherer/Gatherer.png";
            case HUNTER ->
                    card.isHasIcon() ? "/images/Card/hunters/hunterWIcon.png" : "/images/Card/hunters/hunterNormal.png";
            case SHAMAN -> "/images/Card/shaman/" + shamanPath(card.getStarNumber()) + "Shaman.png";
            case INVENTOR -> "/images/Card/inventors/" + invPath(card.getInvIcon()) + "Inventor.png";
            case BUILDER -> "/images/Card/builders/" + card.getBuilderID() + "IDBuilder.png";
            case EVENT ->
                    "/images/Card/events/" + ((EventDTO) card).getEventID() + eventTypePath(((EventDTO) card).getEventType()) + "Event.png";
            default -> "/images/Card/artist/Artist.png";
        };
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(path)));
        iv.setFitHeight(cardFitHeight);
        iv.setPreserveRatio(true);
        iv.setUserData(card);
        return iv;
    }

    private ImageView buildingImageView(BuildingDTO bld) {
        int id = bld.getBuildingID();
        String era = id <= 6 ? "eraOne" : id <= 13 ? "eraTwo" : "eraThree";
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(
                "/images/Card/Buildings/" + era + "/" + id + "IDbuilding.png")));
        iv.setFitHeight(cardFitHeight);
        iv.setPreserveRatio(true);
        iv.setUserData(bld);
        return iv;
    }

    private String eventTypePath(EVENT_TYPE type) {
        return switch (type) {
            case HUNT -> "hunt";
            case PAINTINGS -> "painting";
            case SHAMANIC_RIT -> "Shaman";
            case SUSTENANCE -> "sustenance";
            default -> "hunt";
        };
    }

    private String shamanPath(it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR star) {
        return switch (star) {
            case ONE -> "oneStar";
            case TWO -> "twoStar";
            case THREE -> "threeStar";
        };
    }

    private String invPath(it.polimi.ingsw.am25.server.model.Enums.INV_ICON icon) {
        return switch (icon) {
            case STONE -> "stone";
            case BAIT -> "bait";
            case ARROW -> "arrow";
            case BOWL -> "bowl";
            case BREAD -> "bread";
            case NECKLACE -> "necklace";
            case GHOST -> "ghost";
            case LEATHER -> "leather";
            case ROPE -> "rope";
            case FLUTE -> "flute";
        };
    }
}
