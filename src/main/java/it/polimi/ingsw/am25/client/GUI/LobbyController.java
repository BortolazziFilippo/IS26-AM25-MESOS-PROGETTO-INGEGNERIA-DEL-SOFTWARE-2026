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
    private static final String CSS = LobbyController.class
            .getResource("/FXML/Lobby.css").toExternalForm();

    private final Stage stage;
    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;

    // ---- Nodes injected by FXML (names MUST match the fx:id attributes) ----
    @FXML private TextField nicknameField;
    @FXML private ComboBox<COLOR> colorBox;
    @FXML private Button enterButton;
    @FXML private Button loadButton;
    @FXML private Button joinLoadedButton;
    @FXML private Button rankButton;
    @FXML private Label label;
    @FXML private ListView<String> playerList;

    // ---- Session state ----
    private PlayerDTO playerDTO;
    private MarketController marketController;
    private boolean gameScreenShown = false;

    /**
     * Lobby action currently in progress. Used to correctly interpret errors
     * that, with the Socket transport, arrive asynchronously (via onError)
     * instead of as a synchronous exception (as happens with RMI).
     */
    private enum PendingAction { NONE, JOINING, CREATING, LOADING }
    private PendingAction pendingAction = PendingAction.NONE;

    /** True after the first heartbeat start, to prevent starting it twice. */
    private boolean heartbeatStarted = false;

    /** Currently open leaderboard dialog, null if none. */
    private Dialog<Void> rankDialog;
    /** VBox for each player count (2–5) inside the leaderboard TabPane. */
    private Map<Integer, VBox> rankTabBoxes;

    /** Cache of totem images (loaded once). */
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
     * Finalizes the configuration that requires the nodes to already be ready.
     */
    @FXML
    private void initialize() {
        for (COLOR c : COLOR.values()) {
            Image img = loadTotemImage(c);
            if (img != null) totemImages.put(c, img);
        }

        colorBox.getItems().setAll(COLOR.values());
        colorBox.setValue(COLOR.RED);
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
     * Loads the totem image for the given color.
     * Path: /images/totems/pedine_specs_&lt;color&gt;Totem.png (lowercase).
     * Returns null if the image resource does not exist.
     */
    private Image loadTotemImage(COLOR color) {
        String path = "/images/totems/pedine_specs_" + color.name().toLowerCase() + "Totem.png";
        InputStream stream = getClass().getResourceAsStream(path);
        return stream != null ? new Image(stream) : null;
    }

    private PlayerDTO buildPlayer() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            label.setText("Nickname obbligatorio");
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
     * Single "Create game / Join" button handler:
     *  - attempts addPlayer;
     *  - if the server replies with NO_LOBBY_MESSAGE, opens a dialog asking
     *    for the player count and calls createGame;
     *  - if during creation another client has already created the lobby
     *    (IllegalStateException), automatically retries addPlayer.
     */
    @FXML
    private void onEnter() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        tryEnterLobby(player);
    }

    private void tryEnterLobby(PlayerDTO player) {
        // Set up playerDTO and marketController BEFORE the RMI call:
        // if we are the last player to fill the lobby, the server fires
        // gamePhaseChanged within the same call and onGamePhaseChanged
        // needs marketController to already exist.
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        pendingAction = PendingAction.JOINING;
        // Reset error state before each attempt so stale flags from a previous
        // lobby error do not interfere with the new request (mirrors TUI behaviour).
        clientHandler.connectionError = false;
        clientHandler.lastErrorMessage = null;
        try {
            serverStub.addPlayer(player, clientHandler);
            // No exception. With RMI this means "request accepted"; with Socket
            // it only means "message sent": the actual outcome will arrive later
            // via onGamePhaseChanged (success) or onError (failure).
            startHeartbeat();
            label.setText("Richiesta inviata. In attesa che la partita inizi...");
            disableLobbyButtons();
        } catch (GameFullException ex) {
            // RMI: the server responded immediately with an error.
            handleLobbyError(ex.getMessage());
        } catch (Exception ex) {
            handleLobbyError(ex.getMessage() != null ? ex.getMessage()
                    : ex.getClass().getSimpleName());
        }
    }

    /**
     * Single error-handling entry point for lobby failures.
     * Invoked both from the synchronous catch (RMI) and from the onError callback
     * (Socket, where errors arrive asynchronously).
     */
    private void handleLobbyError(String message) {
        if (pendingAction == PendingAction.JOINING && NO_LOBBY_MESSAGE.equals(message)) {
            // No open lobby: offer the user to create one.
            askCreateGame(playerDTO);
            return;
        }
        if (pendingAction == PendingAction.CREATING && GAME_ALREADY_STARTED_MESSAGE.equals(message)) {
            // Race condition: another client created the lobby in the meantime.
            label.setText("Una lobby è apparsa nel frattempo, ti unisco...");
            tryEnterLobby(playerDTO);
            return;
        }
        // Generic error: show it and re-enable the buttons.
        pendingAction = PendingAction.NONE;
        label.setText("Errore: " + message);
        enableLobbyButtons();
    }

    /** Modal dialog that asks for the player count and creates the game. */
    private void askCreateGame(PlayerDTO player) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Crea partita");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(CSS);
        dialogPane.getStyleClass().addAll("root", "dialog-border");

        Label headerLabel = new Label("Sei il primo giocatore!\nScegli quanti partecipanti:");
        headerLabel.getStyleClass().add("dialog-header");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(javafx.geometry.Pos.CENTER);
        headerLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        dialogPane.setHeader(headerLabel);

        Spinner<Integer> sp = new Spinner<>(2, 5, 2);
        sp.setEditable(false);
        sp.setPrefWidth(110);
        Label numLabel = new Label("Numero di giocatori:");
        numLabel.getStyleClass().add("dialog-field-label");
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
            // Cancelled: restore state and re-enable buttons.
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
            // As with addPlayer: no exception does not guarantee success with Socket;
            // any error will arrive via onError.
            startHeartbeat();
            label.setText("Richiesta inviata. In attesa di altri giocatori...");
            disableLobbyButtons();
        } catch (IllegalStateException ex) {
            // RMI: race condition, another client created the lobby in the meantime.
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
        // marketController must be created BEFORE the call: when loading, the game
        // may resume within the same call and onGamePhaseChanged needs marketController
        // to already exist.
        playerDTO = player;
        marketController = new MarketController(clientHandler, serverStub, playerDTO);
        pendingAction = PendingAction.LOADING;
        try {
            serverStub.loadGame(player, clientHandler);
            // With RMI "no exception" = game loaded; with Socket = message sent only,
            // any error will arrive via onError.
            startHeartbeat();
            label.setText("Richiesta inviata. In attesa degli altri giocatori...");
            disableLobbyButtons();
        } catch (Exception e) {
            handleLobbyError(e.getMessage() != null ? e.getMessage()
                    : e.getClass().getSimpleName());
        }
    }

    /**
     * "Join a loaded game" button: the player reconnects to a saved game
     * that another client has already loaded from disk.
     * Twin of onLoadGame but calls joinGameLoaded instead of loadGame.
     */
    @FXML
    private void onJoinLoadedGame() {
        PlayerDTO player = buildPlayer();
        if (player == null) return;
        // As with onLoadGame: marketController ready before the call,
        // because when the last player reconnects the game resumes
        // within the same call.
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
        // 1. Reset any stale data
        clientHandler.clearLeaderboards();

        // 2. Build a TabPane with one tab per player count
        rankTabBoxes = new java.util.HashMap<>();
        javafx.scene.control.TabPane tabPane = new javafx.scene.control.TabPane();
        tabPane.setTabClosingPolicy(javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("rank-tabs");

        for (int n = 2; n <= 5; n++) {
            VBox tabBox = new VBox(3);
            tabBox.setPadding(new Insets(10, 10, 10, 10));
            tabBox.getStyleClass().add("rank-tab-box");
            ProgressIndicator tabSpinner = new ProgressIndicator();
            tabSpinner.setPrefSize(24, 24);
            Label tabLoading = new Label("Caricamento...");
            tabLoading.getStyleClass().add("rank-loading");
            tabBox.getChildren().addAll(tabSpinner, tabLoading);
            rankTabBoxes.put(n, tabBox);

            ScrollPane tabScroll = new ScrollPane(tabBox);
            tabScroll.setFitToWidth(true);
            tabScroll.setPrefViewportHeight(360);
            tabScroll.getStyleClass().add("rank-scroll");

            javafx.scene.control.Tab tab = new javafx.scene.control.Tab(n + " giocatori");
            tab.setContent(tabScroll);
            tabPane.getTabs().add(tab);
        }

        rankDialog = new Dialog<>();
        rankDialog.setTitle("Classifica");

        DialogPane dialogPane = rankDialog.getDialogPane();
        dialogPane.getStylesheets().add(CSS);
        dialogPane.getStyleClass().addAll("root", "dialog-border");
        Label headerLabel = new Label("Classifica giocatori");
        headerLabel.getStyleClass().add("rank-dialog-header");
        dialogPane.setHeader(headerLabel);
        dialogPane.setContent(tabPane);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setPrefWidth(520);
        dialogPane.setPrefHeight(460);
        rankDialog.setOnHidden(e -> {
            rankDialog = null;
            rankTabBoxes = null;
        });

        // 3. Send the request to the server (response will arrive via onRankReceived)
        try {
            serverStub.askForRank("5", clientHandler);
        } catch (Exception e) {
            VBox firstTab = rankTabBoxes.get(2);
            if (firstTab != null) {
                firstTab.getChildren().clear();
                Label err = new Label("Errore di rete: " + e.getMessage());
                err.getStyleClass().add("rank-error");
                firstTab.getChildren().add(err);
            }
        }
        // 4. Show the dialog (non-blocking; onRankReceived will populate it)
        rankDialog.show();
    }

    /**
     * Renders a leaderboard section into the dialog's content.
     * One section corresponds to games played with a given player count.
     */
    private void renderRankSection(int playerCount, List<String> entries) {
        VBox target = rankTabBoxes.get(playerCount);
        if (target == null) return;
        target.getChildren().clear();

        if (entries == null || entries.isEmpty()) {
            Label empty = new Label("Nessun dato disponibile.");
            empty.getStyleClass().add("rank-empty");
            target.getChildren().add(empty);
            return;
        }

        // Entries arrive from the DB already numbered (e.g. "1. Nick - score").
        for (int i = 0; i < entries.size(); i++) {
            Label entryLabel = new Label(entries.get(i));
            entryLabel.getStyleClass().addAll("rank-entry", i % 2 == 0 ? "rank-entry-even" : "rank-entry-odd");
            entryLabel.setMaxWidth(Double.MAX_VALUE);
            target.getChildren().add(entryLabel);
        }
    }

    // --- Observer callbacks (called from the network thread!) ---

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
                // Error related to a pending lobby action: with the Socket transport
                // failures arrive here instead of as a synchronous exception.
                handleLobbyError(message);
            } else {
                label.setText("Errore: " + message);
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
            Platform.runLater(this::showStartingScreenThenGame);
        }
    }

    /**
     * Called from the network thread when the server returns the leaderboard.
     * Populates the dialog opened by onShowRank with sections
     * for 2, 3, 4 and 5 player games.
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
     * Shows a brief "Game is starting..." transition screen with a pulse effect,
     * then fades out and loads the game screen.
     */
    private void showStartingScreenThenGame() {
        Label title = new Label("MESOS");
        title.getStyleClass().add("starting-title");

        Label subtitle = new Label("La partita sta iniziando...");
        subtitle.getStyleClass().add("starting-subtitle");

        VBox box = new VBox(28, title, subtitle);
        box.setAlignment(Pos.CENTER);

        double w = stage.getWidth()  > 0 ? stage.getWidth()  : 720;
        double h = stage.getHeight() > 0 ? stage.getHeight() : 600;
        Scene startingScene = new Scene(box, w, h);
        startingScene.getStylesheets().add(CSS);
        stage.setScene(startingScene);
        stage.setTitle("MESOS — La partita inizia");

        PauseTransition pause = getPauseTransition(subtitle, box);
        pause.play();
    }

    private PauseTransition getPauseTransition(Label subtitle, VBox box) {
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
        return pause;
    }

    /** Loads Market.fxml and shows it on the stage. */
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
