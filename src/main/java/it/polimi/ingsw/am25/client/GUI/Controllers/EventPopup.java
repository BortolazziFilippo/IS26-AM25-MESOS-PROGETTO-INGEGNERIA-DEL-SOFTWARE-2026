package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EventPopup {

    private Stage stage = null;
    private VBox leftCol = null;
    private VBox rightCol = null;
    private int count = 0;

    public void addEvent(int eventID, EVENT_TYPE eventType, double cardFitHeight) {
        if (stage == null || !stage.isShowing()) {
            leftCol  = new VBox(12);
            leftCol.setPadding(new Insets(16));
            rightCol = new VBox(12);
            rightCol.setPadding(new Insets(16, 16, 16, 0));
            count = 0;

            stage = new Stage();
            stage.setTitle("Eventi risolti");
            stage.setScene(new Scene(new HBox(leftCol, rightCol)));
            stage.setOnHidden(e -> { stage = null; leftCol = null; rightCol = null; count = 0; });
            stage.show();
        }

        HBox row = new HBox(12);
        row.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 4 0;");
        String path = "/images/Card/events/" + eventID + CardImageFactory.eventTypePath(eventType) + "Event.png";
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(path)));
            iv.setFitHeight(cardFitHeight * 1.2);
            iv.setPreserveRatio(true);
            row.getChildren().add(iv);
        } catch (Exception ignored) {}
        Label lbl = new Label(eventType.toString());
        lbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        row.getChildren().add(lbl);

        (count < 2 ? leftCol : rightCol).getChildren().add(row);
        count++;
        stage.sizeToScene();
    }
}
