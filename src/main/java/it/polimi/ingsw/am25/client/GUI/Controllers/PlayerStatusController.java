package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.GUI.GUIObserver;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for PlayerStatus.fxml.
 *
 * <p>Shows a leaderboard above a TabPane where each tab contains one player's card.
 * Implements {@link GUIObserver} for live updates; registers on init and
 * deregisters when the window closes.
 */
public class PlayerStatusController implements GUIObserver {

    @FXML private VBox    leaderboardBox;
    @FXML private TabPane playerTabPane;

    private ClientVirtualView clientHandler;
    private PlayerDTO myPlayer;

    /** Maps each player's nickname to their Tab. */
    private final Map<String, Tab> playerTabs = new HashMap<>();

    /** Creates a new PlayerStatusController (required by the FXML loader). */
    public PlayerStatusController() {}

    /**
     * Provides the controller with the client handler and the local player's DTO.
     * Must be called before the FXML {@code initialize()} lifecycle method runs.
     *
     * @param clientHandler the client view holding live game state.
     * @param myPlayer      the DTO of the local player, used to highlight their own tab.
     */
    public void init(ClientVirtualView clientHandler, PlayerDTO myPlayer) {
        this.clientHandler = clientHandler;
        this.myPlayer = myPlayer;
    }

    /**
     * Removes this controller from the list of GUI observers.
     * Should be called when the player-status window is closed.
     */
    public void unregister() {
        if (clientHandler != null) clientHandler.removeGUIObserver(this);
    }

    // =========================================================
    // FXML lifecycle
    // =========================================================

    /**
     * FXML lifecycle callback. Registers this controller as a GUI observer,
     * builds the leaderboard, and populates one tab per player.
     */
    @FXML
    public void initialize() {
        if (clientHandler == null) return;
        clientHandler.addGUIObserver(this);

        buildLeaderboardContent(leaderboardBox);

        List<PlayerDTO> players = clientHandler.getPlayers();
        if (players.isEmpty()) return;

        for (PlayerDTO player : players) {
            Tab tab = buildTab(player);
            playerTabs.put(player.getNickName(), tab);
            playerTabPane.getTabs().add(tab);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) playerTabPane.getScene().getWindow();
        stage.close();
    }

    // =========================================================
    // GUIObserver
    // =========================================================

    @Override
    public void onCardAddedToTribe(String nickname, CardDTO card) {
        Platform.runLater(() -> rebuildCard(nickname));
    }

    @Override
    public void onPlayerFoodChanged(String nickname, int newFood) {
        Platform.runLater(() -> rebuildCard(nickname));
    }

    @Override
    public void onPlayerPPChanged(String nickname, int newPP) {
        Platform.runLater(() -> {
            refreshLeaderboard();
            rebuildCard(nickname);
        });
    }

    @Override
    public void onPlayerDisconnected(String nickname) {
        Platform.runLater(() -> rebuildCard(nickname));
    }

    @Override
    public void onPlayerReconnected(String nickname) {
        Platform.runLater(() -> rebuildCard(nickname));
    }

    @Override
    public void onPlayerToPlayChanged(String nickname) {
        Platform.runLater(this::rebuildAllCards);
    }

    @Override
    public void onPlayerToPlaceChanged(String nickname) {
        Platform.runLater(this::rebuildAllCards);
    }

    // =========================================================
    // Leaderboard
    // =========================================================

    private void refreshLeaderboard() {
        leaderboardBox.getChildren().clear();
        buildLeaderboardContent(leaderboardBox);
    }

    private void buildLeaderboardContent(VBox box) {
        Label title = new Label("CLASSIFICA  ·  Punti Prestigio");
        title.getStyleClass().add("leaderboard-title");
        box.getChildren().add(title);

        List<PlayerDTO> sorted = clientHandler.getPlayers().stream()
                .sorted(Comparator.comparingInt(PlayerDTO::getPrestigePoint).reversed())
                .toList();

        int maxPP = sorted.isEmpty() ? 1 : Math.max(1, sorted.getFirst().getPrestigePoint());

        for (int i = 0; i < sorted.size(); i++) {
            box.getChildren().add(buildRankRow(sorted.get(i), i + 1, maxPP));
        }
    }

    private HBox buildRankRow(PlayerDTO player, int rank, int maxPP) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label rankLabel = new Label(rankText(rank));
        rankLabel.getStyleClass().add(rank <= 3 ? "rank-" + rank : "rank-other");

        Label nameLabel = new Label(player.getNickName());
        nameLabel.getStyleClass().add("leaderboard-name");
        nameLabel.setStyle("-fx-text-fill: " + totemHex(player.getColorTotem()) + ";");

        HBox bar = buildBar(player.getPrestigePoint(), maxPP, totemHex(player.getColorTotem()));

        Label ppLabel = new Label(player.getPrestigePoint() + " PP");
        ppLabel.getStyleClass().add("leaderboard-pp");

        row.getChildren().addAll(rankLabel, nameLabel, bar, ppLabel);
        return row;
    }

    private HBox buildBar(int value, int max, String colorHex) {
        double fraction = max == 0 ? 0 : Math.min(1.0, (double) value / max);
        double totalW   = 220;

        Region filled = new Region();
        filled.setPrefWidth(totalW * fraction);
        filled.setPrefHeight(10);
        filled.setStyle(
            "-fx-background-color: " + colorHex + ";" +
            "-fx-background-radius: 4 0 0 4;"
        );

        Region empty = new Region();
        HBox.setHgrow(empty, Priority.ALWAYS);
        empty.setPrefHeight(10);
        empty.setStyle("-fx-background-color: #16100a; -fx-background-radius: 0 4 4 0;");

        HBox bar = new HBox(filled, empty);
        bar.setPrefWidth(totalW);
        bar.setMaxWidth(totalW);
        bar.setStyle("-fx-background-radius: 4;");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    // =========================================================
    // Tab / card management
    // =========================================================

    private Tab buildTab(PlayerDTO player) {
        Tab tab = new Tab();
        tab.setClosable(false);
        tab.setGraphic(buildTabHeader(player));

        ScrollPane sp = new ScrollPane(buildCard(player));
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.getStyleClass().add("player-tab-scroll");
        tab.setContent(sp);

        return tab;
    }

    private Label buildTabHeader(PlayerDTO player) {
        boolean isTurn = player.getNickName().equals(currentTurnNickname());
        String prefix = isTurn ? "▶  " : "●  ";
        Label lbl = new Label(prefix + player.getNickName());
        lbl.setStyle(
            "-fx-text-fill: " + totemHex(player.getColorTotem()) + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;"
        );
        return lbl;
    }

    private void rebuildCard(String nickname) {
        Tab tab = playerTabs.get(nickname);
        if (tab == null) return;
        PlayerDTO updated = clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(nickname))
                .findFirst().orElse(null);
        if (updated == null) return;
        setTabCard(tab, updated);
    }

    private void rebuildAllCards() {
        for (PlayerDTO player : clientHandler.getPlayers()) {
            Tab tab = playerTabs.get(player.getNickName());
            if (tab != null) {
                tab.setGraphic(buildTabHeader(player));
                setTabCard(tab, player);
            }
        }
    }

    private void setTabCard(Tab tab, PlayerDTO player) {
        tab.setGraphic(buildTabHeader(player));
        ScrollPane sp = (ScrollPane) tab.getContent();
        sp.setContent(buildCard(player));
    }

    private VBox buildCard(PlayerDTO player) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PlayerCard.fxml"));
            VBox card = loader.load();
            PlayerCardController ctrl = loader.getController();
            boolean isMe          = myPlayer != null
                    && myPlayer.getNickName().equals(player.getNickName());
            boolean disconnected  = clientHandler.isPlayerDisconnected(player.getNickName());
            boolean isCurrentTurn = player.getNickName().equals(currentTurnNickname());
            ctrl.populate(player, isMe, disconnected, isCurrentTurn);
            return card;
        } catch (Exception e) {
            Label err = new Label("Errore: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            return new VBox(err);
        }
    }

    /** Returns the nickname of the player whose turn it currently is, or null if unknown. */
    private String currentTurnNickname() {
        GAME_PHASE phase = clientHandler.getGamePhase();
        if (phase == null) return null;
        return switch (phase) {
            case PLACING_PHASE, LAST_ROUND_PLACING_PHASE -> clientHandler.getPlayerToPlace();
            default -> clientHandler.getPlayerToPlay();
        };
    }

    // =========================================================
    // Helpers
    // =========================================================

    private String rankText(int rank) {
        return switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> rank + ".";
        };
    }

    private String totemHex(COLOR color) {
        if (color == null) return "#e8d4a0";
        return switch (color) {
            case RED    -> "#dd5555";
            case BLUE   -> "#5588ee";
            case YELLOW -> "#ddbb22";
            case WHITE  -> "#cccccc";
            case PURPLE -> "#aa55ee";
        };
    }
}
