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

import javafx.scene.layout.Region;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LobbyController implements GUIObserver {

    private static final String NO_LOBBY_MESSAGE = "Nessuna partita creata!";
    private static final String GAME_ALREADY_STARTED_MESSAGE = "Game already started";

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

    /**
     * Azione di lobby attualmente in corso. Serve a interpretare correttamente
     * gli errori che, con il trasporto Socket, arrivano in modo asincrono
     * (su onError) invece che come eccezione sincrona (come avviene con RMI).
     */
    private enum PendingAction { NONE, JOINING, CREATING, LOADING }
    private PendingAction pendingAction = PendingAction.NONE;

    /** True dopo il primo avvio dell'heartbeat, per non avviarlo due volte. */
    private boolean heartbeatStarted = false;

    /** Dialog della classifica attualmente aperto, null se nessuno. */
    private Dialog<Void> rankDialog;
    /** VBox per ogni numero di giocatori (2-5) dentro il TabPane della classifica. */
    private Map<Integer, VBox> rankTabBoxes;

    /** Cache delle immagini totem (caricate una volta sola). */
    private final Map<COLOR, Image> totemImages = new EnumMap<>(COLOR.class);

    /**
     * Creates the lobby controller and registers this observer with the client view.
     *
     * @param serverStub    the remote server interface used to create or join a game.
     * @param clientHandler the local client view that receives notifications from the server.
     * @param stage         the JavaFX stage on which the lobby will be displayed.
     */
    public LobbyController(ServerRemoteInterface serverStub, ClientVirtualView clientHandler, Stage stage) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.stage = stage;
        clientHandler.addGUIObserver(this);
    }

    /** Loads the FXML and displays the lobby. */
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
     * Called automatically by JavaFX after FXML node injection.
     * Finalises the configuration that requires the nodes to already be ready.
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
        if (heartbeatStarted) return;
        heartbeatStarted = true;
        clientHandler.heartbeatActive = true;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Heartbeat Thread");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            if (clientHandler.isServerDead()) {
                scheduler.shutdownNow();
                return;
            }
            try {
                serverStub.ping(playerDTO);
            } catch (Exception e) {
                clientHandler.handleServerDeath();
                scheduler.shutdownNow();
            }
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
        pendingAction = PendingAction.JOINING;
        // Reset error state before each attempt so stale flags from a previous
        // lobby error do not interfere with the new request (mirrors TUI behaviour).
        clientHandler.connectionError = false;
        clientHandler.lastErrorMessage = null;
        try {
            serverStub.addPlayer(player, clientHandler);
            // Nessuna eccezione. Con RMI significa "richiesta accettata"; con Socket
            // significa solo "messaggio inviato": l'esito vero arriverà comunque
            // dopo, su onGamePhaseChanged (successo) o su onError (fallimento).
            startHeartbeat();
            label.setText("Richiesta inviata. In attesa che la partita inizi...");
            disableLobbyButtons();
        } catch (GameFullException ex) {
            // RMI: il server ha risposto subito con un errore.
            handleLobbyError(ex.getMessage());
        } catch (Exception ex) {
            handleLobbyError(ex.getMessage() != null ? ex.getMessage()
                    : ex.getClass().getSimpleName());
        }
    }

    /**
     * Punto unico di gestione degli esiti negativi della lobby.
     * Viene invocato sia dal catch sincrono (RMI) sia dalla callback onError
     * (Socket, dove gli errori arrivano in modo asincrono).
     */
    private void handleLobbyError(String message) {
        if (pendingAction == PendingAction.JOINING && NO_LOBBY_MESSAGE.equals(message)) {
            // Nessuna lobby aperta: proponiamo all'utente di crearne una.
            askCreateGame(playerDTO);
            return;
        }
        if (pendingAction == PendingAction.CREATING && GAME_ALREADY_STARTED_MESSAGE.equals(message)) {
            // Race condition: un altro client ha creato la lobby nel frattempo.
            label.setText("Una lobby è apparsa nel frattempo, ti unisco...");
            tryEnterLobby(playerDTO);
            return;
        }
        // Errore generico: lo mostriamo e riabilitiamo i pulsanti.
        pendingAction = PendingAction.NONE;
        label.setText("❌ " + message);
        enableLobbyButtons();
    }

    /** Dialog modale che chiede il numero di giocatori e crea la partita. */
    private void askCreateGame(PlayerDTO player) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Crea partita");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/FXML/Lobby.css").toExternalForm());
        dialogPane.getStyleClass().add("root");
        dialogPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1a0f08, #0d0805);" +
            "-fx-border-color: #6b4a2a; -fx-border-width: 1.5;"
        );

        Label headerLabel = new Label("Sei il primo giocatore!\nScegli quanti partecipanti:");
        headerLabel.setStyle(
            "-fx-font-family: 'Georgia'; -fx-font-size: 17px; -fx-font-weight: bold;" +
            "-fx-text-fill: #f5dfa0; -fx-padding: 10 4 10 4; -fx-alignment: center;"
        );
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(javafx.geometry.Pos.CENTER);
        headerLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        dialogPane.setHeader(headerLabel);

        Spinner<Integer> sp = new Spinner<>(2, 5, 2);
        sp.setEditable(false);
        sp.setPrefWidth(110);
        Label numLabel = new Label("Numero di giocatori:");
        numLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e8c98b;");
        numLabel.setMaxWidth(Double.MAX_VALUE);
        numLabel.setAlignment(javafx.geometry.Pos.CENTER);
        VBox content = new VBox(12, numLabel, sp);
        content.setPadding(new Insets(14));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        dialogPane.setContent(content);

        ButtonType creaBT = new ButtonType("Crea partita", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(creaBT, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == creaBT ? sp.getValue() : null);

        Optional<Integer> res = dialog.showAndWait();
        if (res.isEmpty()) {
            // Annullato: ripristiniamo stato e pulsanti.
            pendingAction = PendingAction.NONE;
            label.setText("Creazione annullata.");
            enableLobbyButtons();
            return;
        }

        pendingAction = PendingAction.CREATING;
        // Reset error state before createGame so the stale NO_LOBBY flag does not
        // break turn-blocking operations (selectExtraCard etc.) during the game phase.
        clientHandler.connectionError = false;
        clientHandler.lastErrorMessage = null;
        try {
            serverStub.createGame(player, res.get(), clientHandler);
            // Come per addPlayer: nessuna eccezione non garantisce il successo
            // con Socket; l'eventuale errore arriverà su onError.
            startHeartbeat();
            label.setText("Richiesta inviata. In attesa di altri giocatori...");
            disableLobbyButtons();
        } catch (IllegalStateException ex) {
            // RMI: race condition, un altro client ha creato la lobby nel frattempo.
            handleLobbyError(GAME_ALREADY_STARTED_MESSAGE);
        } catch (Exception ex) {
            handleLobbyError(ex.getMessage() != null ? ex.getMessage()
                    : ex.getClass().getSimpleName());
        }
    }

    private void disableLobbyButtons() {
        enterButton.setDisable(true);
        loadButton.setDisable(true);
        joinLoadedButton.setDisable(true);
    }

    private void enableLobbyButtons() {
        enterButton.setDisable(false);
        loadButton.setDisable(false);
        joinLoadedButton.setDisable(false);
    }

    @FXML
    private void onLoadGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        // marketController va creato PRIMA della chiamata: con un caricamento la
        // partita può riprendere dentro la stessa chiamata e onGamePhaseChanged
        // ha bisogno che marketController esista già.
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        pendingAction = PendingAction.LOADING;
        try {
            serverStub.loadGame(player, clientHandler);
            // Con RMI "nessuna eccezione" = partita caricata; con Socket = solo
            // messaggio inviato, l'eventuale errore arriverà su onError.
            startHeartbeat();
            label.setText("Richiesta inviata. In attesa degli altri giocatori...");
            disableLobbyButtons();
        } catch (Exception e) {
            handleLobbyError(e.getMessage() != null ? e.getMessage()
                    : e.getClass().getSimpleName());
        }
    }

    /**
     * Bottone "Unisciti a una partita caricata": il giocatore si riconnette a
     * una partita salvata che un altro client ha già caricato dal disco.
     * Gemello di onLoadGame ma chiama joinGameLoaded invece di loadGame.
     */
    @FXML
    private void onJoinLoadedGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        // Come per onLoadGame: marketController pronto prima della chiamata,
        // perché con l'ultimo giocatore che si riconnette la partita riprende
        // dentro la stessa chiamata.
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        pendingAction = PendingAction.LOADING;
        try {
            serverStub.joinGameLoaded(player, clientHandler);
            startHeartbeat();
            label.setText("Richiesta inviata. In attesa degli altri giocatori...");
            disableLobbyButtons();
        } catch (Exception e) {
            handleLobbyError(e.getMessage() != null ? e.getMessage()
                    : e.getClass().getSimpleName());
        }
    }

    @FXML
    private void onShowRank() {
        // 1. Reset di eventuali dati vecchi
        clientHandler.clearLeaderboards();

        // 2. Costruisco un TabPane con un tab per numero di giocatori
        rankTabBoxes = new java.util.HashMap<>();
        javafx.scene.control.TabPane tabPane = new javafx.scene.control.TabPane();
        tabPane.setTabClosingPolicy(javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("rank-tabs");

        for (int n = 2; n <= 5; n++) {
            VBox tabBox = new VBox(3);
            tabBox.setPadding(new Insets(10, 10, 10, 10));
            tabBox.setStyle("-fx-background-color: transparent;");
            ProgressIndicator tabSpinner = new ProgressIndicator();
            tabSpinner.setPrefSize(24, 24);
            Label tabLoading = new Label("Caricamento...");
            tabLoading.setStyle("-fx-text-fill: #c9a66b; -fx-font-style: italic;");
            tabBox.getChildren().addAll(tabSpinner, tabLoading);
            rankTabBoxes.put(n, tabBox);

            ScrollPane tabScroll = new ScrollPane(tabBox);
            tabScroll.setFitToWidth(true);
            tabScroll.setPrefViewportHeight(360);
            tabScroll.setStyle("-fx-background: #0d0805; -fx-background-color: #0d0805; -fx-border-color: transparent;");

            javafx.scene.control.Tab tab = new javafx.scene.control.Tab(n + " giocatori");
            tab.setContent(tabScroll);
            tabPane.getTabs().add(tab);
        }

        rankDialog = new Dialog<>();
        rankDialog.setTitle("Classifica");

        DialogPane dialogPane = rankDialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/FXML/Lobby.css").toExternalForm());
        dialogPane.getStyleClass().add("root");
        dialogPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1a0f08, #0d0805);" +
            "-fx-border-color: #6b4a2a; -fx-border-width: 1.5;"
        );
        Label headerLabel = new Label("Classifica giocatori");
        headerLabel.setStyle(
            "-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-text-fill: #f5dfa0; -fx-padding: 8 4 8 4;"
        );
        dialogPane.setHeader(headerLabel);
        dialogPane.setContent(tabPane);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setPrefWidth(520);
        dialogPane.setPrefHeight(460);
        rankDialog.setOnHidden(e -> {
            rankDialog = null;
            rankTabBoxes = null;
        });

        // 3. Mando la richiesta al server (la risposta arriverà su onRankReceived)
        try {
            serverStub.askForRank("5", clientHandler);
        } catch (Exception e) {
            VBox firstTab = rankTabBoxes.get(2);
            if (firstTab != null) {
                firstTab.getChildren().clear();
                Label err = new Label("Errore di rete: " + e.getMessage());
                err.setStyle("-fx-text-fill: #ff6644;");
                firstTab.getChildren().add(err);
            }
        }

        // 4. Mostro il dialog (non bloccante, useremo onRankReceived per riempirlo)
        rankDialog.show();
    }

    /**
     * Renderizza una sezione della classifica (header + entries) dentro il content
     * del dialog. Una "sezione" è "Partite da N giocatori".
     */
    private void renderRankSection(int playerCount, List<String> entries) {
        VBox target = rankTabBoxes.get(playerCount);
        if (target == null) return;
        target.getChildren().clear();

        if (entries == null || entries.isEmpty()) {
            Label empty = new Label("Nessun dato disponibile.");
            empty.setStyle("-fx-font-style: italic; -fx-text-fill: #7a6a55; -fx-padding: 8;");
            target.getChildren().add(empty);
            return;
        }

        // Le voci arrivano dal DB già numerate ("1. Nick - punteggio").
        for (int i = 0; i < entries.size(); i++) {
            Label entryLabel = new Label(entries.get(i));
            String bg = (i % 2 == 0) ? "transparent" : "rgba(50,30,10,0.6)";
            entryLabel.setStyle(
                "-fx-text-fill: #f5dfa0; -fx-padding: 5 8 5 8;" +
                "-fx-background-color: " + bg + ";"
            );
            entryLabel.setMaxWidth(Double.MAX_VALUE);
            target.getChildren().add(entryLabel);
        }
    }

    // --- Observer callbacks (chiamate dal thread di rete!) ---

    /**
     * Called when a new player joins the lobby.
     * Adds the nickname and totem colour to the visible UI list.
     *
     * @param player the DTO of the player added to the lobby.
     */
    @Override
    public void onPlayerAdded(PlayerDTO player) {
        Platform.runLater(() -> playerList.getItems().add(
                player.getNickName() + " (" + player.getColorTotem() + ")"));
    }

    /**
     * Called when the server sends an error message during the lobby phase.
     * Displays the message in the status label.
     *
     * @param message the error text received from the server.
     */
    @Override
    public void onError(String message) {
        Platform.runLater(() -> {
            if (pendingAction != PendingAction.NONE) {
                // Errore relativo a un'azione di lobby in corso: con il trasporto
                // Socket gli esiti negativi arrivano qui invece che come eccezione.
                handleLobbyError(message);
            } else {
                label.setText("❌ " + message);
            }
        });
    }

    /**
     * Called when the game phase changes. If the phase is not SETUP and the game
     * screen has not yet been shown, starts the transition to the game screen.
     *
     * @param gamePhase the new game phase received from the server.
     */
    @Override
    public void onGamePhaseChanged(GAME_PHASE gamePhase) {
        if (gamePhase != GAME_PHASE.SETUP && !gameScreenShown) {
            gameScreenShown = true;
            clientHandler.startPongWatchdog();
            startHeartbeat();
            pendingAction = PendingAction.NONE;
            Platform.runLater(() -> showStartingScreenThenGame());
        }
    }

    /**
     * Chiamato dal thread di rete quando il server risponde con la classifica.
     * Riempie il dialog aperto da onShowRank con le quattro sezioni
     * (partite da 2, 3, 4 e 5 giocatori).
     */
    @Override
    public void onRankReceived(Map<Integer, List<String>> leaderboards) {
        Platform.runLater(() -> {
            if (rankTabBoxes == null) return;
            for (int playerCount = 2; playerCount <= 5; playerCount++) {
                renderRankSection(playerCount,
                        leaderboards != null ? leaderboards.get(playerCount) : null);
            }
        });
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
