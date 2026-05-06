package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.client.GUI.Controllers.TileContorller;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LobbyController implements GUIObserver {

    private final Stage stage;
    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;

    private TextField nicknameField;
    private ComboBox<COLOR> colorBox;
    private Spinner<Integer> spinner;
    private Button createButton;
    private Button joinButton;
    private Label label;
    private ListView<String> playerList;

    private PlayerDTO playerDTO;
    private TileContorller tileController;
    private boolean gameScreenShown = false;

    public LobbyController(ServerRemoteInterface serverStub, ClientVirtualView clientHandler, Stage stage) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.stage = stage;
        clientHandler.addGUIObserver(this);
    }

    public void showing() {
        Label title = new Label("IS26-AM25 — Lobby");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        nicknameField = new TextField();
        nicknameField.setPromptText("nickname");

        colorBox = new ComboBox<>();
        colorBox.getItems().setAll(COLOR.values());
        colorBox.setValue(COLOR.RED);

        spinner = new Spinner<>(2, 5, 2);
        spinner.setEditable(false);

        createButton = new Button("Crea partita");
        joinButton = new Button("Unisciti");
        createButton.setOnAction(e -> createGame());
        joinButton.setOnAction(e -> joinGame());

        label = new Label("");
        label.setWrapText(true);

        playerList = new ListView<>();
        playerList.setPrefHeight(150);

        HBox formRow = new HBox(10,
                new Label("Nickname:"), nicknameField,
                new Label("Colore:"), colorBox,
                new Label("# giocatori:"), spinner);
        formRow.setAlignment(Pos.CENTER_LEFT);

        HBox btnRow = new HBox(10, createButton, joinButton);

        VBox root = new VBox(12,
                title,
                formRow,
                btnRow,
                new Label("Giocatori in lobby:"),
                playerList,
                label);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 450);
        stage.setScene(scene);
        stage.setTitle("IS26-AM25 — Lobby");
        stage.show();
    }

    private PlayerDTO buildPlayer() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            label.setText("⚠ Nickname obbligatorio");
            return null;
        }
        return new PlayerDTO(nickname, 0, 0, colorBox.getValue());
    }

    private void createGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) {
            return;
        }
        try {
            serverStub.createGame(player, spinner.getValue(), clientHandler);
            playerDTO = player;
            tileController = new TileContorller(clientHandler, serverStub, playerDTO);
            label.setText("Partita creata. In attesa di altri giocatori...");
            createButton.setDisable(true);
            joinButton.setDisable(true);
        } catch (Exception e) {
            label.setText("❌ Errore durante la creazione: " + e.getMessage());
        }
    }

    private void joinGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) {
            return;
        }
        try {
            serverStub.addPlayer(player, clientHandler);
            playerDTO = player;
            tileController = new TileContorller(clientHandler, serverStub, playerDTO);
            label.setText("Unito alla lobby. In attesa che inizi...");
            createButton.setDisable(true);
            joinButton.setDisable(true);
        } catch (Exception e) {
            label.setText("❌ Errore durante l'unione alla lobby: " + e.getMessage());
        }
    }

    // --- Observer callbacks (chiamate dal thread di rete!) ---

    @Override
    public void onPlayerAdded(PlayerDTO player) {
        Platform.runLater(() -> playerList.getItems().add(
                player.getNickName() + " (" + player.getColorTotem() + ")"));
    }

    @Override
    public void onError(String message) {
        Platform.runLater(() -> label.setText("❌ " + message));
    }

    @Override
    public void onGamePhaseChanged(GAME_PHASE gamePhase) {
        if (gamePhase == GAME_PHASE.PLACING_PHASE && !gameScreenShown) {
            gameScreenShown = true;
            Platform.runLater(() -> {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource("/FXML/Market.fxml"));
                    loader.setController(tileController);
                    javafx.scene.Parent root = loader.load();
                    stage.setScene(new javafx.scene.Scene(root));
                    stage.setTitle("IS26-AM25 — Game");
                    stage.setMaximized(true);
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
