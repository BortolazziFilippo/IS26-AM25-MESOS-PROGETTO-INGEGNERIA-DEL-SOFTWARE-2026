package it.polimi.ingsw.am25.client.GUI.popup;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DisconnectPopup {

    private Stage stage = null;
    private VBox box = null;

    public void addDisconnection(String nickname) {
        addEntry("Il giocatore " + nickname + " si è disconnesso", "#c0392b");
    }
    public void addReconnection(String nickname) {
        addEntry("Il giocatore " + nickname + " si è riconnesso","#027404" );
    }

    private void addEntry(String text, String color) {
        if (stage == null || !stage.isShowing()) {
            box = new VBox(10);
            box.setPadding(new Insets(20));

            stage = new Stage();
            stage.setTitle("Stato giocatori");
            Scene scene = new Scene(box);
            scene.getStylesheets().add(getClass().getResource("/FXML/Market.css").toExternalForm());
            stage.setScene(scene);
            stage.setOnHidden(e -> {
                stage = null;
                box = null;
            });
            stage.show();
        }

        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        box.getChildren().add(lbl);
        stage.sizeToScene();
    }
}
