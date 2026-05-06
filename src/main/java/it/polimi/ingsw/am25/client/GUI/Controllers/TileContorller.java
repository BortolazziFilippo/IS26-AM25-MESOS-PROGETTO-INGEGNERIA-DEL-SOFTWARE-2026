package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.GUI.GUIObserver;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import java.rmi.RemoteException;
import java.util.List;

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

    // --- stato di gioco ---
    private GAME_PHASE currentPhase = null;
    private String playerToPlace = null;
    private String playerToPlay = null;

    // --- selezione tile ---
    private int selectedTilePosition = -1;
    private ImageView selectedTileView = null;

    // --- selezione carta ---
    private ImageView selectedCardView = null;
    private int topTribeCount = 0;    // carte tribù in topCardHbox; building vengono dopo
    private int bottomTribeCount = 0; // carte tribù in bottomCardHbox; building vengono dopo

    @FXML private HBox topCardHbox;
    @FXML private HBox tileHbox;
    @FXML private HBox bottomCardHbox;
    @FXML private Button selectCardButton;
    @FXML private Button placeTotemButton;
    @FXML private Label phaseLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label drawTopLabel;
    @FXML private Label drawBotLabel;

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
        if (pendingOffertiles != null) {
            renderTiles(pendingOffertiles, pendingDefs);
            pendingOffertiles = null; pendingDefs = null;
        }
        if (pendingTopTribeCard != null) {
            renderCards(pendingTopTribeCard, pendingBottomTribeCard, pendingTopBuildingCard);
            pendingTopTribeCard = null; pendingBottomTribeCard = null; pendingTopBuildingCard = null;
        }
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
                int pos = bottomCardHbox.getChildren().indexOf(selectedCardView);
                serverRemoteInterface.selectCardFromBottomList(playerDTO, type, pos);
            }
            clearCardSelection();
        } catch (Exception e) {
            System.err.println("Errore selezione carta: " + e.getMessage());
        }
    }

    @FXML
    private void handlePlaceTotem() {
        if (selectedTilePosition == -1) return;
        try {
            serverRemoteInterface.placingPlayer(playerDTO, selectedTilePosition);
            clearTileSelection();
            placeTotemButton.setDisable(true);
        } catch (RemoteException e) {
            System.err.println("Errore posizionamento totem: " + e.getMessage());
        }
    }

    // =========================================================
    // OBSERVER
    // =========================================================

    @Override
    public void onGamePhaseChanged(GAME_PHASE phase) {
        this.currentPhase = phase;
        Platform.runLater(() -> {
            if (phaseLabel != null) phaseLabel.setText("Fase corrente: " + phase);
            updateInteractionState();
        });
    }

    @Override
    public void onPlayerToPlaceChanged(String nickname) {
        this.playerToPlace = nickname;
        Platform.runLater(() -> {
            if (currentPlayerLabel != null) currentPlayerLabel.setText("Giocatore di turno: " + nickname);
            updateInteractionState();
        });
    }

    @Override
    public void onPlayerToPlayChanged(String nickname) {
        this.playerToPlay = nickname;
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
        List<BuildingDTO> topSnapshot    = new java.util.ArrayList<>(topBld);
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
        if (tileHbox == null) {
            this.pendingDefs = defs;
            this.pendingOffertiles = tiles;
        } else {
            Platform.runLater(() -> renderTiles(tiles, defs));
        }
    }

    @Override
    public void onPlayerPlacedOnOfferTile(String nickname, int tilePosition) {
        GUIObserver.super.onPlayerPlacedOnOfferTile(nickname, tilePosition);
    }

    @Override
    public void onActionAvailableChanged(int drawTop, int drawBot) {
        Platform.runLater(() -> {
            if (drawTopLabel != null) drawTopLabel.setText("Pesca da sopra: " + drawTop);
            if (drawBotLabel != null) drawBotLabel.setText("Pesca da sotto: " + drawBot);
            updateInteractionState();
        });
    }

    @Override public void onPlayerAdded(PlayerDTO player)     { GUIObserver.super.onPlayerAdded(player); }
    @Override public void onError(String message)             { GUIObserver.super.onError(message); }
    @Override public void onPlayerPPChanged(String n, int p)  { GUIObserver.super.onPlayerPPChanged(n, p); }
    @Override
    public void onPlayerFoodChanged(String n, int f) {
        if (playerDTO.getNickName().equals(n)) {
            playerDTO.setFood(f);
            Platform.runLater(this::updateInteractionState);
        }
    }
    @Override public void onEventResolved(String d)           { GUIObserver.super.onEventResolved(d); }
    @Override public void onWinners(List<PlayerDTO> w)        { GUIObserver.super.onWinners(w); }

    // =========================================================
    // RENDER
    // =========================================================

    private void renderCards(List<CardDTO> top, List<CardDTO> bot, List<BuildingDTO> topBld) {
        topCardHbox.getChildren().clear();
        bottomCardHbox.getChildren().clear();
        selectedCardView = null;

        topTribeCount = top.size();
        for (CardDTO card : top)       topCardHbox.getChildren().add(cardImageView(card));
        for (BuildingDTO bld : topBld) topCardHbox.getChildren().add(buildingImageView(bld));

        bottomTribeCount = bot.size();
        for (CardDTO card : bot)       bottomCardHbox.getChildren().add(cardImageView(card));

        updateInteractionState();
    }

    private void renderTiles(List<OffertileDTO> tiles, List<DefaultTileDTO> defs) {
        tileHbox.getChildren().clear();
        clearTileSelection();

        ImageView defIv = new ImageView(new Image(getClass().getResourceAsStream(
                "/images/Tiles/defaultTile/" + defs.size() + "plDefTile.png")));
        defIv.setFitHeight(cardFitHeight);
        defIv.setPreserveRatio(true);
        tileHbox.getChildren().add(defIv);

        for (OffertileDTO tile : tiles) {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(
                    "/images/Tiles/offertiles/" + tile.getOfferTileID() + "offertile.png")));
            iv.setFitHeight(cardFitHeight);
            iv.setPreserveRatio(true);
            tileHbox.getChildren().add(iv);
        }
        updateInteractionState();
    }

    // =========================================================
    // INTERACTION STATE
    // =========================================================

    private boolean isPlacingPhase() {
        return currentPhase == GAME_PHASE.PLACING_PHASE || currentPhase == GAME_PHASE.LAST_ROUND_PLACING_PHASE;
    }

    private boolean isMyPlacingTurn() {
        return isPlacingPhase() && playerDTO.getNickName().equals(playerToPlace);
    }

    private boolean isMyPlayingTurn() {
        return !isPlacingPhase() && playerDTO.getNickName().equals(playerToPlay);
    }

    private void updateInteractionState() {
        if (placeTotemButton == null || tileHbox == null) return;

        boolean myPlacing = isMyPlacingTurn();
        boolean myPlaying = isMyPlayingTurn();

        ColorAdjust gray = new ColorAdjust();
        gray.setSaturation(-0.8);
        gray.setBrightness(-0.2);

        // --- tile (indice 0 = default tile, mai cliccabile) ---
        for (int i = 0; i < tileHbox.getChildren().size(); i++) {
            ImageView iv = (ImageView) tileHbox.getChildren().get(i);
            boolean clickable = myPlacing && i > 0;
            if (clickable) {
                iv.setEffect(iv == selectedTileView ? goldGlow() : null);
                iv.setOpacity(1.0);
                final int pos = i - 1;
                iv.setOnMouseClicked(e -> selectTile(iv, pos));
                iv.setStyle("-fx-cursor: hand;");
            } else {
                if (iv != selectedTileView) iv.setEffect(myPlacing ? null : gray);
                iv.setOpacity(myPlacing && i == 0 ? 1.0 : (myPlacing ? 1.0 : 0.45));
                iv.setOnMouseClicked(null);
                iv.setStyle("");
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
            if (!(node.getUserData() instanceof CardDTO)) continue;
            CardDTO dto = (CardDTO) node.getUserData();
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

    private void selectTile(ImageView iv, int position) {
        if (selectedTileView != null) selectedTileView.setEffect(null);
        if (selectedTileView == iv) {
            selectedTileView = null;
            selectedTilePosition = -1;
        } else {
            iv.setEffect(goldGlow());
            selectedTileView = iv;
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
        if (selectedTileView != null) { selectedTileView.setEffect(null); selectedTileView = null; }
        selectedTilePosition = -1;
        if (placeTotemButton != null) placeTotemButton.setDisable(true);
    }

    private void clearCardSelection() {
        if (selectedCardView != null) { selectedCardView.setEffect(null); selectedCardView = null; }
        if (selectCardButton != null) selectCardButton.setDisable(true);
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
            case ARTIST   -> "/images/Card/artist/Artist.png";
            case GATHERER -> "/images/Card/gatherer/Gatherer.png";
            case HUNTER   -> card.isHasIcon() ? "/images/Card/hunters/hunterWIcon.png" : "/images/Card/hunters/hunterNormal.png";
            case SHAMAN   -> "/images/Card/shaman/" + shamanPath(card.getStarNumber()) + "Shaman.png";
            case INVENTOR -> "/images/Card/inventors/" + invPath(card.getInvIcon()) + "Inventor.png";
            case BUILDER  -> "/images/Card/builders/" + card.getBuilderID() + "IDBuilder.png";
            case EVENT    -> "/images/Card/events/" + ((EventDTO) card).getEventID() + eventTypePath(((EventDTO) card).getEventType()) + "Event.png";
            default       -> "/images/Card/artist/Artist.png";
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
            case HUNT         -> "hunt";
            case PAINTINGS    -> "painting";
            case SHAMANIC_RIT -> "Shaman";
            case SUSTENANCE   -> "sustenance";
            default           -> "hunt";
        };
    }

    private String shamanPath(it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR star) {
        return switch (star) {
            case ONE   -> "oneStar";
            case TWO   -> "twoStar";
            case THREE -> "threeStar";
        };
    }

    private String invPath(it.polimi.ingsw.am25.server.model.Enums.INV_ICON icon) {
        return switch (icon) {
            case STONE    -> "stone";
            case BAIT     -> "bait";
            case ARROW    -> "arrow";
            case BOWL     -> "bowl";
            case BREAD    -> "bread";
            case NECKLACE -> "necklace";
            case GHOST    -> "ghost";
            case LEATHER  -> "leather";
            case ROPE     -> "rope";
            case FLUTE    -> "flute";
        };
    }
}
