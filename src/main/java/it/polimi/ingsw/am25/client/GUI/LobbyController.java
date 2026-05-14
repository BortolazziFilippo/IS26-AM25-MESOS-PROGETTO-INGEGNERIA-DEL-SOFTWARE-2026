package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.client.GUI.Controllers.MarketController;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LobbyController implements GUIObserver {

    private final Stage stage;
    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;

    // ---- Nodi iniettati dall'FXML (i nomi DEVONO corrispondere agli fx:id) ----
    @FXML private TextField nicknameField;
    @FXML private ComboBox<COLOR> colorBox;
    @FXML private Spinner<Integer> spinner;
    @FXML private Button createButton;
    @FXML private Button joinButton;
    @FXML private Button loadButton;
    @FXML private Label label;
    @FXML private ListView<String> playerList;

    // ---- Stato di sessione ----
    private PlayerDTO playerDTO;
    private MarketController marketController;
    private boolean gameScreenShown = false;

    public LobbyController(ServerRemoteInterface serverStub, ClientVirtualView clientHandler, Stage stage) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.stage = stage;
        clientHandler.addGUIObserver(this);
    }

    /** Carica l'FXML e mostra la lobby. */
    public void showing() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Lobby.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("IS26-AM25 — Lobby");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Chiamato automaticamente da JavaFX dopo l'iniezione dei nodi.
     * Qui finalizziamo la configurazione che richiede i nodi già pronti.
     */
    @FXML
    private void initialize() {
        colorBox.getItems().setAll(COLOR.values());
        colorBox.setValue(COLOR.RED);
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 5, 2));
    }

    private PlayerDTO buildPlayer() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            label.setText("⚠ Nickname obbligatorio");
            return null;
        }
        return new PlayerDTO(nickname, 0, 0, colorBox.getValue());
    }

    private void startHeartbeat() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Heartbeat Thread");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                serverStub.ping(playerDTO);
            } catch (Exception ignored) {}
        }, 0, 1, TimeUnit.SECONDS);
    }

    @FXML
    private void onCreateGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        // IMPORTANTE: prepara playerDTO e marketController PRIMA della chiamata RMI.
        // Quando l'ultimo giocatore si unisce, il server (dentro la stessa chiamata)
        // fa partire la partita e spara gamePhaseChanged(PLACING_PHASE) al volo:
        // se marketController è ancora null, onGamePhaseChanged crasha.
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        try {
            serverStub.createGame(player, spinner.getValue(), clientHandler);
            startHeartbeat();
            label.setText("Partita creata. In attesa di altri giocatori...");
            createButton.setDisable(true);
            joinButton.setDisable(true);
        } catch (Exception e) {
            label.setText("❌ Errore durante la creazione: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        // Prepara playerDTO e marketController PRIMA della chiamata RMI
        // (il server può sparare gamePhaseChanged dentro la stessa chiamata).
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        try {
            serverStub.loadGame(player, clientHandler);
            startHeartbeat();
            label.setText("Partita trovata! In attesa degli altri giocatori...");
            loadButton.setDisable(true);
            createButton.setDisable(true);
            joinButton.setDisable(true);
        } catch (Exception e) {
            label.setText("❌ Errore: " + e.getMessage());
        }
    }

    @FXML
    private void onJoinGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        // IMPORTANTE: prepara playerDTO e marketController PRIMA della chiamata RMI.
        // Per il giocatore che fa partire la partita (cioè quello che riempie la
        // lobby), il server fa scattare gamePhaseChanged dentro la stessa chiamata,
        // e onGamePhaseChanged ha bisogno che marketController esista già.
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        try {
            serverStub.addPlayer(player, clientHandler);
            startHeartbeat();
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
        if (gamePhase != GAME_PHASE.SETUP && !gameScreenShown) {
            gameScreenShown = true;
            Platform.runLater(() -> showStartingScreenThenGame());
        }
    }

    /**
     * Mostra una breve schermata di transizione "La partita sta iniziando..."
     * con effetto pulse, poi fade out e carica la schermata di gioco.
     */
    private void showStartingScreenThenGame() {
        // Costruzione della schermata di transizione
        Label title = new Label("MESOS");
        title.setStyle(
            "-fx-font-family: 'Georgia'; -fx-font-size: 64px; -fx-font-weight: bold; "
          + "-fx-text-fill: #f5dfa0; "
          + "-fx-effect: dropshadow(gaussian, rgba(255, 140, 50, 0.7), 18, 0.5, 0, 3);"
        );

        Label subtitle = new Label("La partita sta iniziando...");
        subtitle.setStyle(
            "-fx-font-family: 'Georgia'; -fx-font-size: 24px; -fx-font-style: italic; "
          + "-fx-text-fill: #c9a66b;"
        );

        VBox box = new VBox(28, title, subtitle);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0f08, #0d0805);");

        // Mantiene le dimensioni correnti dello stage
        double w = stage.getWidth()  > 0 ? stage.getWidth()  : 720;
        double h = stage.getHeight() > 0 ? stage.getHeight() : 600;
        stage.setScene(new Scene(box, w, h));
        stage.setTitle("MESOS — La partita inizia");

        // Effetto pulse sul sottotitolo
        FadeTransition pulse = new FadeTransition(Duration.millis(700), subtitle);
        pulse.setFromValue(0.3);
        pulse.setToValue(1.0);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        // Dopo 2.5 secondi, fade out e poi carica la game scene
        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> {
            pulse.stop();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), box);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> loadGameScene());
            fadeOut.play();
        });
        pause.play();
    }

    /** Carica Market.fxml e lo mostra sullo stage. */
    private void loadGameScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Market.fxml"));
            loader.setController(marketController);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("IS26-AM25 — Game");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}