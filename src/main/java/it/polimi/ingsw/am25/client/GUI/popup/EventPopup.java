package it.polimi.ingsw.am25.client.GUI.popup;

import it.polimi.ingsw.am25.client.GUI.Controllers.CardImageFactory;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

/**
 * Floating overlay panel that displays the resolved event cards during a round.
 * Events are laid out in columns and the overlay dismisses on click or ESC.
 */
public class EventPopup {

    /** Creates a new EventPopup. The overlay is not shown until the first event is added. */
    public EventPopup() {}

    private static final int MAX_PER_COLUMN = 2;

    private StackPane overlay = null;
    private VBox content = null;
    private HBox columnsBox = null;
    private VBox currentColumn = null;
    private int countInCurrentColumn = 0;
    private int totalCount = 0;
    private EventHandler<KeyEvent> escHandler = null;

    /**
     * Adds a resolved event to the overlay, creating it if not yet visible.
     * The overlay is injected directly into {@code sceneRoot} to avoid the
     * rendering overhead of a second JavaFX Stage.
     *
     * @param eventID       the unique identifier of the resolved event.
     * @param eventType     the category of the resolved event.
     * @param cardFitHeight the display height of the event card image in pixels.
     * @param sceneRoot     the root pane of the current scene used to anchor the overlay.
     */
    public void addEvent(int eventID, EVENT_TYPE eventType, double cardFitHeight, Pane sceneRoot) {
        if (sceneRoot == null) return;

        if (overlay == null) {
            columnsBox = new HBox(20);
            columnsBox.setPadding(new Insets(20));
            columnsBox.setAlignment(Pos.TOP_CENTER);
            currentColumn = null;
            countInCurrentColumn = 0;
            totalCount = 0;

            Label titleLabel = new Label("EVENTI RISOLTI");
            titleLabel.getStyleClass().add("status-title");
            titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

            Label hint = new Label("Clicca o premi ESC per chiudere");
            hint.getStyleClass().add("sottotitolo");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox header = new HBox(12, titleLabel, spacer, hint);
            header.getStyleClass().add("status-title-bar");
            header.setAlignment(Pos.CENTER_LEFT);

            ScrollPane scroll = new ScrollPane(columnsBox);
            scroll.setFitToHeight(false);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.getStyleClass().add("player-tab-scroll");

            content = new VBox(header, scroll);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            content.maxWidthProperty().bind(sceneRoot.widthProperty().multiply(0.88));
            content.maxHeightProperty().bind(sceneRoot.heightProperty().multiply(0.88));

            Scene scene = sceneRoot.getScene();
            if (scene != null) {
                String lobbyCss = getClass().getResource("/FXML/Lobby.css").toExternalForm();
                String statusCss = getClass().getResource("/FXML/PlayerStatus.css").toExternalForm();
                if (!scene.getStylesheets().contains(lobbyCss)) scene.getStylesheets().add(lobbyCss);
                if (!scene.getStylesheets().contains(statusCss)) scene.getStylesheets().add(statusCss);
            }

            overlay = new StackPane(content);
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
            overlay.setAlignment(Pos.CENTER);
            overlay.setPickOnBounds(true);

            if (sceneRoot instanceof AnchorPane) {
                AnchorPane.setTopAnchor(overlay, 0.0);
                AnchorPane.setBottomAnchor(overlay, 0.0);
                AnchorPane.setLeftAnchor(overlay, 0.0);
                AnchorPane.setRightAnchor(overlay, 0.0);
            } else {
                overlay.prefWidthProperty().bind(sceneRoot.widthProperty());
                overlay.prefHeightProperty().bind(sceneRoot.heightProperty());
            }

            // Event filter (top-down) intercepts all clicks before any child, matching
            // the original Stage behavior of "click anywhere to close"
            overlay.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> dismiss(sceneRoot));

            if (scene != null) {
                escHandler = e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        dismiss(sceneRoot);
                        e.consume();
                    }
                };
                scene.addEventFilter(KeyEvent.KEY_PRESSED, escHandler);
            }

            sceneRoot.getChildren().add(overlay);
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
    }

    private void dismiss(Pane sceneRoot) {
        if (overlay == null) return;
        Scene scene = sceneRoot.getScene();
        if (scene != null && escHandler != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, escHandler);
            escHandler = null;
        }
        content.maxWidthProperty().unbind();
        content.maxHeightProperty().unbind();
        if (!(sceneRoot instanceof AnchorPane)) {
            overlay.prefWidthProperty().unbind();
            overlay.prefHeightProperty().unbind();
        }
        sceneRoot.getChildren().remove(overlay);
        overlay = null;
        content = null;
        columnsBox = null;
        currentColumn = null;
        countInCurrentColumn = 0;
        totalCount = 0;
    }

    private VBox buildEventEntry(int eventID, EVENT_TYPE eventType, double cardFitHeight) {
        VBox entry = new VBox(10);
        entry.setAlignment(Pos.CENTER);
        entry.getStyleClass().add("panel");
        entry.setPadding(new Insets(14));

        try {
            ImageView iv = new ImageView(CardImageFactory.eventImage(eventID, eventType));
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
}
