package it.polimi.ingsw.am25.client.GUI.Controllers;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DisconnectPopup {

    private Stage stage = null;
    private VBox box = null;

    public void addDisconnection(String nickname) {
        if (stage == null || !stage.isShowing()) {
            box = new VBox(10);
            box.setPadding(new Insets(20));

            stage = new Stage();
            stage.setTitle("Disconnessione");
            Scene scene = new Scene(box);
            scene.getStylesheets().add(getClass().getResource("/FXML/Market.css").toExternalForm());
            stage.setScene(scene);
            stage.setOnHidden(e -> { stage = null; box = null; });
            stage.show();
        }

        Label lbl = new Label("Il giocatore " + nickname + " si è disconnesso");
        lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        stage.sizeToScene();
    }
}
