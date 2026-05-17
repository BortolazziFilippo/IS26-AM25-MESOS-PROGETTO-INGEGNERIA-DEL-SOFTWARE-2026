package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.client.GUI.Controllers.MarketController;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.GameFullException;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LobbyController implements GUIObserver {

    private static final String NO_LOBBY_MESSAGE = "Nessuna partita creata!";

    private final Stage stage;
    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;

    // ---- Nodi iniettati dall'FXML (i nomi DEVONO corrispondere agli fx:id) ----
    @FXML private TextField nicknameField;
    @FXML private ComboBox<COLOR> colorBox;
    @FXML private Button enterButton;
    @FXML private Button loadButton;
    @FXML private Button joinLoadedButton;
    @FXML private Button rankButton;
    @FXML private Label label;
    @FXML private ListView<String> playerList;

    // ---- Stato di sessione ----
    private PlayerDTO playerDTO;
    private MarketController marketController;
    private boolean gameScreenShown = false;

    /** Cache delle immagini totem (caricate una volta sola). */
    private final Map<COLOR, Image> totemImages = new EnumMap<>(COLOR.class);

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
        // Preload delle immagini totem
        for (COLOR c : COLOR.values()) {
            Image img = loadTotemImage(c);
            if (img != null) totemImages.put(c, img);
        }

        colorBox.getItems().setAll(COLOR.values());
        colorBox.setValue(COLOR.RED);

        // Voci della tendina con immagine totem + nome del colore
        colorBox.setCellFactory(lv -> new ListCell<COLOR>() {
            private final ImageView iv = new ImageView();
            {
                iv.setFitWidth(28);
                iv.setFitHeight(28);
                iv.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(COLOR item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.name());
                    Image img = totemImages.get(item);
                    iv.setImage(img);
                    setGraphic(img != null ? iv : null);
                }
            }
        });

        // Cella selezionata mostrata nel bottone del ComboBox
        colorBox.setButtonCell(new ListCell<COLOR>() {
            private final ImageView iv = new ImageView();
            {
                iv.setFitWidth(24);
                iv.setFitHeight(24);
                iv.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(COLOR item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.name());
                    Image img = totemImages.get(item);
                    iv.setImage(img);
                    setGraphic(img != null ? iv : null);
                }
            }
        });
    }

    /**
     * Carica l'immagine del totem per il colore indicato.
     * Path: /images/totems/pedine_specs_&lt;color&gt;Totem.png (lowercase).
     * Restituisce null se l'immagine non esiste.
     */
    private Image loadTotemImage(COLOR color) {
        String path = "/images/totems/pedine_specs_" + color.name().toLowerCase() + "Totem.png";
        InputStream stream = getClass().getResourceAsStream(path);
        return stream != null ? new Image(stream) : null;
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

    /**
     * Bottone unico "Crea partita / Unisciti":
     *  - tenta addPlayer;
     *  - se il server risponde "Nessuna partita creata!" apre un dialog che chiede
     *    il numero di giocatori e chiama createGame;
     *  - se durante la creazione un altro client ha già creato la lobby
     *    (IllegalStateException), ritenta automaticamente addPlayer.
     */
    @FXML
    private void onEnter() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        tryEnterLobby(player);
    }

    private void tryEnterLobby(PlayerDTO player) {
        // Prepara playerDTO e marketController PRIMA della chiamata RMI:
        // se siamo l'ultimo che riempie la lobby, il server fa scattare
        // gamePhaseChanged dentro la stessa chiamata e onGamePhaseChanged
        // ha bisogno che marketController esista già.
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        try {
            serverStub.addPlayer(player, clientHandler);
            startHeartbeat();
            label.setText("Unito alla lobby. In attesa che inizi...");
            disableLobbyButtons();
        } catch (GameFullException ex) {
            if (NO_LOBBY_MESSAGE.equals(ex.getMessage())) {
                askCreateGame(player);
            } else {
                label.setText("❌ " + ex.getMessage());
            }
        } catch (Exception ex) {
            label.setText("❌ " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /** Dialog modale che chiede il numero di giocatori e crea la partita. */
    private void askCreateGame(PlayerDTO player) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Crea partita");
        dialog.setHeaderText("Nessuna partita aperta.\nVuoi crearne una nuova?");

        Spinner<Integer> sp = new Spinner<>(2, 5, 2);
        sp.setEditable(false);
        sp.setPrefWidth(100);
        VBox content = new VBox(10, new Label("Numero di giocatori:"), sp);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType creaBT = new ButtonType("Crea partita", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(creaBT, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == creaBT ? sp.getValue() : null);

        Optional<Integer> res = dialog.showAndWait();
        if (res.isEmpty()) {
            label.setText("Creazione annullata.");
            return;
        }

        try {
            serverStub.createGame(player, res.get(), clientHandler);
            startHeartbeat();
            label.setText("Partita creata. In attesa di altri giocatori...");
            disableLobbyButtons();
        } catch (IllegalStateException ex) {
            // Race: qualcuno ha creato la lobby nel frattempo. Ritentiamo addPlayer.
            label.setText("Una lobby è apparsa nel frattempo, ti unisco...");
            tryEnterLobby(player);
        } catch (Exception ex) {
            label.setText("❌ Errore creazione: " + ex.getMessage());
        }
    }

    private void disableLobbyButtons() {
        enterButton.setDisable(true);
        loadButton.setDisable(true);
        joinLoadedButton.setDisable(true);
    }

    @FXML
    private void onLoadGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        try {
            serverStub.loadGame(player, clientHandler);
            startHeartbeat();
            label.setText("Partita trovata! In attesa degli altri giocatori...");
            disableLobbyButtons();
        } catch (Exception e) {
            label.setText("❌ Errore: " + e.getMessage());
        }
    }

    @FXML
    private void onJoinLoadedGame() {
        // TODO: implementare l'unione a una partita caricata
        label.setText("La funzione 'Unisciti a partita caricata' arriverà presto.");
    }

    @FXML
    private void onShowRank() {
        // TODO: implementare la visualizzazione della classifica
        label.setText("La funzione 'Classifica' arriverà presto.");
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

        double w = stage.getWidth()  > 0 ? stage.getWidth()  : 720;
        double h = stage.getHeight() > 0 ? stage.getHeight() : 600;
        stage.setScene(new Scene(box, w, h));
        stage.setTitle("MESOS — La partita inizia");

        FadeTransition pulse = new FadeTransition(Duration.millis(700), subtitle);
        pulse.setFromValue(0.3);
        pulse.setToValue(1.0);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

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
