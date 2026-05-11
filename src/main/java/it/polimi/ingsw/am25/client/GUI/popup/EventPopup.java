package it.polimi.ingsw.am25.client.GUI.popup;

import it.polimi.ingsw.am25.client.GUI.Controllers.CardImageFactory;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class EventPopup {

    private static final int MAX_PER_COLUMN = 2;

    private Stage stage = null;
    private HBox columnsBox = null;
    private VBox currentColumn = null;
    private int countInCurrentColumn = 0;
    private int totalCount = 0;
    private double savedCardFitHeight = 200;

    public void addEvent(int eventID, EVENT_TYPE eventType, double cardFitHeight) {
        savedCardFitHeight = cardFitHeight;

        if (stage == null || !stage.isShowing()) {
            columnsBox = new HBox(20);
            columnsBox.setPadding(new Insets(20));
            columnsBox.setAlignment(Pos.TOP_CENTER);
            currentColumn = null;
            countInCurrentColumn = 0;
            totalCount = 0;

            Label titleLabel = new Label("EVENTI RISOLTI");
            titleLabel.getStyleClass().add("status-title");

            Label hint = new Label("Clicca o premi ESC per chiudere");
            hint.getStyleClass().add("sottotitolo");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox header = new HBox(12, titleLabel, spacer, hint);
            header.getStyleClass().add("status-title-bar");
            header.setAlignment(Pos.CENTER_LEFT);

            ScrollPane scroll = new ScrollPane(columnsBox);
            scroll.setFitToHeight(false);
            scroll.setFitToWidth(false);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.getStyleClass().add("player-tab-scroll");

            VBox root = new VBox(header, scroll);
            VBox.setVgrow(scroll, Priority.ALWAYS);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/FXML/Lobby.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/FXML/PlayerStatus.css").toExternalForm());

            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) stage.close();
            });
            scene.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> stage.close());

            stage = new Stage();
            stage.setTitle("Eventi risolti");
            stage.setScene(scene);
            stage.setOnHidden(e -> {
                stage = null;
                columnsBox = null;
                currentColumn = null;
                countInCurrentColumn = 0;
                totalCount = 0;
            });
            stage.show();
        }

        if (currentColumn == null || countInCurrentColumn >= MAX_PER_COLUMN) {
            currentColumn = new VBox(16);
            currentColumn.setAlignment(Pos.TOP_CENTER);
            columnsBox.getChildren().add(currentColumn);
            countInCurrentColumn = 0;
        }

        currentColumn.getChildren().add(buildEventEntry(eventID, eventType, cardFitHeight));
        countInCurrentColumn++;
        totalCount++;

        resizeStage(cardFitHeight);
    }

    private VBox buildEventEntry(int eventID, EVENT_TYPE eventType, double cardFitHeight) {
        VBox entry = new VBox(10);
        entry.setAlignment(Pos.CENTER);
        entry.getStyleClass().add("panel");
        entry.setPadding(new Insets(14));

        String path = "/images/Card/events/" + eventID
                + CardImageFactory.eventTypePath(eventType) + "Event.png";
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(path)));
            iv.setFitHeight(cardFitHeight);
            iv.setPreserveRatio(true);
            entry.getChildren().add(iv);
        } catch (Exception ignored) {
        }

        Label lbl = new Label(eventType.toString());
        lbl.getStyleClass().add("section-title");
        entry.getChildren().add(lbl);

        return entry;
    }

    private void resizeStage(double cardFitHeight) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        // Height: 2 full cards + label + padding per column, plus header
        double entryH = cardFitHeight + 10 + 26 + 28; // card + vgap + label + panel padding
        double contentH = MAX_PER_COLUMN * entryH + (MAX_PER_COLUMN - 1) * 16 + 40; // rows + gaps + columnsBox padding
        double headerH = 52;
        double scrollbarH = 18;
        double targetH = Math.min(contentH + headerH + scrollbarH, screen.getHeight() * 0.92);

        // Width: one column per pair of events, plus padding
        int numColumns = (totalCount + MAX_PER_COLUMN - 1) / MAX_PER_COLUMN;
        // Approximate card width from aspect ratio (events are roughly 2:3 portrait)
        double cardW = cardFitHeight * 0.68;
        double colW = cardW + 28; // card + panel horizontal padding
        double contentW = numColumns * colW + (numColumns - 1) * 20 + 40; // cols + gaps + outer padding
        double scrollbarW = 18;
        double targetW = Math.min(contentW + scrollbarW, screen.getWidth() * 0.92);

        stage.setWidth(targetW);
        stage.setHeight(targetH);
    }
}
