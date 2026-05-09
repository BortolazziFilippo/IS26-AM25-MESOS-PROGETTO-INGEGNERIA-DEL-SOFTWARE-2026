package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for PlayerStatus.fxml.
 *
 * <p>Loads one {@code PlayerCard.fxml} per player and adds it to the scroll list.
 * The card layout and styling are defined in PlayerCard.fxml (editable in Scene Builder);
 * data is injected via {@link PlayerCardController#populate}.
 */
public class PlayerStatusController {

    @FXML private VBox playersVBox;

    private ClientVirtualView clientHandler;
    private PlayerDTO myPlayer;

    public PlayerStatusController() {}

    /**
     * Injects runtime dependencies. Must be called before the FXML is loaded.
     *
     * @param clientHandler the source of live player data.
     * @param myPlayer      the local player DTO (used to mark "TU").
     */
    public void init(ClientVirtualView clientHandler, PlayerDTO myPlayer) {
        this.clientHandler = clientHandler;
        this.myPlayer = myPlayer;
    }

    @FXML
    public void initialize() {
        if (clientHandler == null) return;
        List<PlayerDTO> players = clientHandler.getPlayers();
        if (players.isEmpty()) {
            Label empty = new Label("Nessun dato giocatore disponibile.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
            playersVBox.getChildren().add(empty);
            return;
        }
        for (PlayerDTO player : players) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/FXML/PlayerCard.fxml"));
                VBox card = loader.load();
                PlayerCardController cardCtrl = loader.getController();
                boolean isMe         = myPlayer != null
                        && myPlayer.getNickName().equals(player.getNickName());
                boolean disconnected = clientHandler.isPlayerDisconnected(player.getNickName());
                cardCtrl.populate(player, isMe, disconnected);
                playersVBox.getChildren().add(card);
            } catch (Exception e) {
                Label err = new Label("Errore caricamento card: " + e.getMessage());
                err.setStyle("-fx-text-fill: red;");
                playersVBox.getChildren().add(err);
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) playersVBox.getScene().getWindow();
        stage.close();
    }
}
