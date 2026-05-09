package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.GUI.GUIObserver;
import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for PlayerStatus.fxml.
 *
 * <p>Implements {@link GUIObserver} to receive live game events and refresh
 * individual player cards without rebuilding the whole list.
 * Registers itself on init and deregisters when the window is closed.
 */
public class PlayerStatusController implements GUIObserver {

    @FXML private VBox playersVBox;

    private ClientVirtualView clientHandler;
    private PlayerDTO myPlayer;

    /** Maps each player's nickname to their card's index in playersVBox. */
    private final Map<String, Integer> cardIndex = new HashMap<>();

    public PlayerStatusController() {}

    /**
     * Injects runtime dependencies. Must be called before the FXML is loaded.
     */
    public void init(ClientVirtualView clientHandler, PlayerDTO myPlayer) {
        this.clientHandler = clientHandler;
        this.myPlayer = myPlayer;
    }

    /** Called by the owning Stage's onHidden handler to deregister this observer. */
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
    // GUIObserver — only the events that affect player cards
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
        Platform.runLater(() -> rebuildCard(nickname));
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
    // Card management
    // =========================================================

    /**
     * Replaces the card for the given player with a freshly populated one.
     * No-op if the player is not in the index (e.g. event arrived before init).
     */
    private void rebuildCard(String nickname) {
        Integer idx = cardIndex.get(nickname);
        if (idx == null || idx >= playersVBox.getChildren().size()) return;

        PlayerDTO updated = clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(nickname))
                .findFirst().orElse(null);
        if (updated == null) return;

        playersVBox.getChildren().set(idx, buildCard(updated));
    }

    /** Loads PlayerCard.fxml, populates it and returns the root VBox. */
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
            VBox fallback = new VBox(err);
            return fallback;
        }
    }
}
