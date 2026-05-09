package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.GUI.GUIObserver;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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
 * <p>Shows a leaderboard at the top (sorted by PP) followed by one card per player.
 * Implements {@link GUIObserver} for live updates; registers on init and
 * deregisters when the window closes.
 */
public class PlayerStatusController implements GUIObserver {

    /** Index in playersVBox occupied by the leaderboard widget. */
    private static final int LEADERBOARD_IDX = 0;

    @FXML private VBox playersVBox;

    private ClientVirtualView clientHandler;
    private PlayerDTO myPlayer;

    /** Maps each player's nickname to their card index in playersVBox (cards start at index 1). */
    private final Map<String, Integer> cardIndex = new HashMap<>();

    public PlayerStatusController() {}

    public void init(ClientVirtualView clientHandler, PlayerDTO myPlayer) {
        this.clientHandler = clientHandler;
        this.myPlayer = myPlayer;
    }

    public void unregister() {
        if (clientHandler != null) clientHandler.removeGUIObserver(this);
    }

    // =========================================================
    // FXML lifecycle
    // =========================================================

    @FXML
    public void initialize() {
        if (clientHandler == null) return;
        clientHandler.addGUIObserver(this);

        // Leaderboard always at index 0
        playersVBox.getChildren().add(buildLeaderboard());

        List<PlayerDTO> players = clientHandler.getPlayers();
        if (players.isEmpty()) {
            Label empty = new Label("Nessun dato giocatore disponibile.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #9a8060;");
            playersVBox.getChildren().add(empty);
            return;
        }
        for (PlayerDTO player : players) {
            int idx = playersVBox.getChildren().size();
            cardIndex.put(player.getNickName(), idx);
            playersVBox.getChildren().add(buildCard(player));
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) playersVBox.getScene().getWindow();
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

    // =========================================================
    // Leaderboard
    // =========================================================

    private VBox buildLeaderboard() {
        VBox box = new VBox(8);
        box.getStyleClass().add("leaderboard");
        buildLeaderboardContent(box);
        return box;
    }

    /** Replaces the leaderboard at index 0 with a fresh one. */
    private void refreshLeaderboard() {
        if (playersVBox.getChildren().isEmpty()) return;
        VBox box = new VBox(8);
        box.getStyleClass().add("leaderboard");
        buildLeaderboardContent(box);
        playersVBox.getChildren().set(LEADERBOARD_IDX, box);
    }

    private void buildLeaderboardContent(VBox box) {
        Label title = new Label("CLASSIFICA  ·  Punti Prestigio");
        title.getStyleClass().add("leaderboard-title");
        box.getChildren().add(title);

        List<PlayerDTO> sorted = clientHandler.getPlayers().stream()
                .sorted(Comparator.comparingInt(PlayerDTO::getPrestigePoint).reversed())
                .toList();

        int maxPP = sorted.isEmpty() ? 1
                : Math.max(1, sorted.get(0).getPrestigePoint());

        for (int i = 0; i < sorted.size(); i++) {
            box.getChildren().add(buildRankRow(sorted.get(i), i + 1, maxPP));
        }
    }

    private HBox buildRankRow(PlayerDTO player, int rank, int maxPP) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        // Rank badge
        Label rankLabel = new Label(rankText(rank));
        rankLabel.getStyleClass().add(rank <= 3 ? "rank-" + rank : "rank-other");

        // Player name (colored by totem)
        Label nameLabel = new Label(player.getNickName());
        nameLabel.getStyleClass().add("leaderboard-name");
        nameLabel.setStyle("-fx-text-fill: " + totemHex(player.getColorTotem()) + ";");

        // Progress bar
        HBox bar = buildBar(player.getPrestigePoint(), maxPP,
                totemHex(player.getColorTotem()));

        // PP value
        Label ppLabel = new Label(player.getPrestigePoint() + " PP");
        ppLabel.getStyleClass().add("leaderboard-pp");

        row.getChildren().addAll(rankLabel, nameLabel, bar, ppLabel);
        return row;
    }

    /**
     * Builds a custom progress bar: a filled region next to an empty one,
     * proportional to {@code value / max}, colored with {@code colorHex}.
     */
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
    // Card management
    // =========================================================

    private void rebuildCard(String nickname) {
        Integer idx = cardIndex.get(nickname);
        if (idx == null || idx >= playersVBox.getChildren().size()) return;
        PlayerDTO updated = clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(nickname))
                .findFirst().orElse(null);
        if (updated == null) return;
        playersVBox.getChildren().set(idx, buildCard(updated));
    }

    private VBox buildCard(PlayerDTO player) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PlayerCard.fxml"));
            VBox card = loader.load();
            PlayerCardController ctrl = loader.getController();
            boolean isMe         = myPlayer != null
                    && myPlayer.getNickName().equals(player.getNickName());
            boolean disconnected = clientHandler.isPlayerDisconnected(player.getNickName());
            ctrl.populate(player, isMe, disconnected);
            return card;
        } catch (Exception e) {
            Label err = new Label("Errore: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            return new VBox(err);
        }
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
