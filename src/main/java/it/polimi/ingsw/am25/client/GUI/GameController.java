package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameController implements GUIObserver {
    private final Stage stage;
    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final PlayerDTO playerDTO;

    private Label phaseLabel;
    private Label turnLabel;
    private TextArea log;

    /**
     * Creates the game controller and registers this observer with the client view.
     *
     * @param stage         the JavaFX stage on which the game screen will be displayed.
     * @param serverStub    the remote server interface used to send actions.
     * @param clientHandler the local client view that receives notifications from the server.
     * @param playerDTO     the DTO of the local player.
     */
    public GameController(Stage stage, ServerRemoteInterface serverStub,
                          ClientVirtualView clientHandler, PlayerDTO playerDTO) {
        this.stage = stage;
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.playerDTO = playerDTO;
        clientHandler.addGUIObserver(this);
    }

    public void showing() {
        phaseLabel = new Label("Fase: " + clientHandler.getGamePhase());
        turnLabel = new Label("Turno: -");
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(20);

        VBox root = new VBox(10,
                new Label("IS26-AM25 — Game (" + playerDTO.getNickName() + ")"),
                phaseLabel, turnLabel, log);
        root.setPadding(new Insets(15));
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("IS26-AM25 — Game");
        stage.show();

        appendLog("Schermata di gioco avviata. Fase iniziale: " + clientHandler.getGamePhase());
    }


    private void appendLog(String line) {
        Platform.runLater(() -> log.appendText(line + "\n"));
    }

    /**
     * Called when the game phase changes. Updates the phase label and appends a log entry.
     *
     * @param phase the new game phase.
     */
    @Override
    public void onGamePhaseChanged(GAME_PHASE phase) {
        Platform.runLater(() -> phaseLabel.setText("Fase: " + phase));
        appendLog("→ Cambio fase: " + phase);
    }

    /**
     * Called when the active player changes during the placement phase.
     *
     * @param n the nickname of the player who must now place their totem.
     */
    @Override
    public void onPlayerToPlaceChanged(String n) {
        Platform.runLater(() -> turnLabel.setText("Turno (place): " + n));
        appendLog("→ Tocca a " + n + " (placing)");
    }

    /**
     * Called when the active player changes during the play phase.
     *
     * @param n the nickname of the player who must now take their action.
     */
    @Override
    public void onPlayerToPlayChanged(String n) {
        Platform.runLater(() -> turnLabel.setText("Turno (play): " + n));
        appendLog("→ Tocca a " + n + " (playing)");
    }

    /**
     * Called when a game event is resolved. Appends a log entry.
     *
     * @param eventID   the unique ID of the resolved event.
     * @param eventType the type of the resolved event.
     */
    @Override
    public void onEventResolved(int eventID, it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE eventType) {
        appendLog("Evento #" + eventID + " (" + eventType + ") risolto");
    }

    /**
     * Called when a player's Prestige Points change. Appends a log entry.
     *
     * @param n the player's nickname.
     * @param p the new Prestige Points value.
     */
    @Override
    public void onPlayerPPChanged(String n, int p) {
        appendLog(n + " PP=" + p);
    }

    /**
     * Called when a player's food reserve changes. Appends a log entry.
     *
     * @param n the player's nickname.
     * @param f the new food reserve value.
     */
    @Override
    public void onPlayerFoodChanged(String n, int f) {
        appendLog(n + " food=" + f);
    }

    /**
     * Called when a player places their totem on an offer tile. Appends a log entry.
     *
     * @param n        the player's nickname.
     * @param t        the position of the occupied offer tile.
     * @param fromSlot the originating slot on the default tile.
     */
    @Override
    public void onPlayerPlacedOnOfferTile(String n, int t, int fromSlot) {
        appendLog(n + " su tile " + t);
    }

    /**
     * Called when a card is removed from the top row of the market. Appends a log entry.
     *
     * @param p the position of the removed card.
     */
    @Override
    public void onTopCardRemoved(int p) {
        appendLog("Top card removed @" + p);
    }

    /**
     * Called when a card is removed from the bottom row of the market. Appends a log entry.
     *
     * @param p the position of the removed card.
     */
    @Override
    public void onBottomCardRemoved(int p) {
        appendLog("Bot card removed @" + p);
    }

    /**
     * Called when the server sends an error message. Appends a log entry.
     *
     * @param m the error text received from the server.
     */
    @Override
    public void onError(String m) {
        appendLog("ERRORE: " + m);
    }
}