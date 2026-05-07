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

public class GameController implements GUIObserver{
    private final Stage stage;
    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final PlayerDTO playerDTO;

    private Label phaseLabel;
    private Label turnLabel;
    private TextArea log;

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
        turnLabel  = new Label("Turno: -");
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
    @Override
    public void onGamePhaseChanged(GAME_PHASE phase) {
    Platform.runLater(() -> phaseLabel.setText("Fase: " + phase));
    appendLog("→ Cambio fase: " + phase);
    }
    @Override
    public void onPlayerToPlaceChanged(String n) {
    Platform.runLater(() -> turnLabel.setText("Turno (place): " + n));
    appendLog("→ Tocca a " + n + " (placing)");
    }
    @Override
    public void onPlayerToPlayChanged(String n) {
        Platform.runLater(() -> turnLabel.setText("Turno (play): " + n));
        appendLog("→ Tocca a " + n + " (playing)");
    }
    @Override
    public void onEventResolved(int eventID, it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE eventType){
        appendLog("Evento #" + eventID + " (" + eventType + ") risolto");
    }
    @Override
    public void onPlayerPPChanged (String n,int p){
        appendLog(n + " PP=" + p);
    }
    @Override
    public void onPlayerFoodChanged (String n,int f){
        appendLog(n + " food=" + f);
    }
    @Override
    public void onPlayerPlacedOnOfferTile(String n, int t, int fromSlot) {
        appendLog(n + " su tile " + t);
    }
    @Override
    public void onTopCardRemoved ( int p){
        appendLog("Top card removed @" + p);
    }
    @Override
    public void onBottomCardRemoved ( int p){
        appendLog("Bot card removed @" + p);
    }
    @Override
    public void onError (String m){
        appendLog("ERRORE: " + m);
    }
}