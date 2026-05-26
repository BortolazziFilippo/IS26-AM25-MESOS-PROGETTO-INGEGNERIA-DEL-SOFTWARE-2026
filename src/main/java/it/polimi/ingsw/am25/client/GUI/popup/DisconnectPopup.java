package it.polimi.ingsw.am25.client.GUI.popup;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

/**
 * Floating overlay panel that notifies players about disconnection and reconnection events
 * during a game. The panel stacks multiple entries and dismisses on click or ESC.
 */
public class DisconnectPopup {

    /** Creates a new DisconnectPopup. The overlay is not shown until the first event is added. */
    public DisconnectPopup() {}

    private static final String PANEL_BG   = "-fx-background-color: #12121e; -fx-background-radius: 10;";
    private static final String DISCONNECT = "#e74c3c";
    private static final String RECONNECT  = "#2ecc71";

    private StackPane overlay = null;
    private VBox entries = null;
    private EventHandler<KeyEvent> escHandler = null;

    /**
     * Adds a disconnection entry for the given player to the overlay panel,
     * creating the panel if it does not yet exist.
     *
     * @param nickname  the nickname of the disconnected player.
     * @param sceneRoot the root pane of the current scene, used to anchor the overlay.
     */
    public void addDisconnection(String nickname, Pane sceneRoot) {
        addEntry("⚠  " + nickname + " si è disconnesso", DISCONNECT, sceneRoot);
    }

    /**
     * Adds a reconnection entry for the given player to the overlay panel,
     * creating the panel if it does not yet exist.
     *
     * @param nickname  the nickname of the reconnected player.
     * @param sceneRoot the root pane of the current scene, used to anchor the overlay.
     */
    public void addReconnection(String nickname, Pane sceneRoot) {
        addEntry("✔  " + nickname + " si è riconnesso", RECONNECT, sceneRoot);
    }

    private void addEntry(String text, String accentHex, Pane sceneRoot) {
        if (sceneRoot == null) return;

        if (overlay == null) {
            entries = new VBox(10);
            entries.setPadding(new Insets(0, 24, 20, 24));

            Label titleLabel = new Label("STATO GIOCATORI");
            titleLabel.setStyle(
                    "-fx-font-size: 22px; -fx-font-weight: bold;" +
                    "-fx-text-fill: #ecf0f1; -fx-padding: 20 24 14 24;");

            Region divider = new Region();
            divider.setPrefHeight(2);
            divider.setStyle("-fx-background-color: #2c2c3e;");

            Label hint = new Label("Clicca o premi ESC per chiudere");
            hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 14 24 0 24;");

            VBox content = new VBox(0, titleLabel, divider, entries, hint);
            content.setStyle(PANEL_BG);
            content.maxWidthProperty().bind(sceneRoot.widthProperty().multiply(0.45));
            content.setMinWidth(360);

            ensureCss(sceneRoot.getScene());

            overlay = new StackPane(content);
            overlay.setStyle("-fx-background-color: rgba(5,5,15,0.82);");
            overlay.setAlignment(Pos.CENTER);
            overlay.setPickOnBounds(true);

            anchorToRoot(overlay, sceneRoot);

            overlay.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> dismiss(sceneRoot));

            Scene scene = sceneRoot.getScene();
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

        entries.getChildren().add(buildCard(text, accentHex));
    }

    private HBox buildCard(String text, String accentHex) {
        Region accent = new Region();
        accent.setPrefWidth(6);
        accent.setMinWidth(6);
        accent.setStyle("-fx-background-color: " + accentHex + "; -fx-background-radius: 4 0 0 4;");

        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");
        lbl.setPadding(new Insets(12, 16, 12, 14));
        HBox.setHgrow(lbl, Priority.ALWAYS);

        HBox card = new HBox(0, accent, lbl);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #1e1e30; -fx-background-radius: 4;");
        return card;
    }

    private void dismiss(Pane sceneRoot) {
        if (overlay == null) return;
        Scene scene = sceneRoot.getScene();
        if (scene != null && escHandler != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, escHandler);
            escHandler = null;
        }
        VBox content = (VBox) overlay.getChildren().getFirst();
        content.maxWidthProperty().unbind();
        if (!(sceneRoot instanceof AnchorPane)) {
            overlay.prefWidthProperty().unbind();
            overlay.prefHeightProperty().unbind();
        }
        sceneRoot.getChildren().remove(overlay);
        overlay = null;
        entries = null;
    }

    private static void anchorToRoot(StackPane overlay, Pane sceneRoot) {
        if (sceneRoot instanceof AnchorPane) {
            AnchorPane.setTopAnchor(overlay, 0.0);
            AnchorPane.setBottomAnchor(overlay, 0.0);
            AnchorPane.setLeftAnchor(overlay, 0.0);
            AnchorPane.setRightAnchor(overlay, 0.0);
        } else {
            overlay.prefWidthProperty().bind(sceneRoot.widthProperty());
            overlay.prefHeightProperty().bind(sceneRoot.heightProperty());
        }
    }

    private void ensureCss(Scene scene) {
        if (scene == null) return;
        String lobbyCss  = getClass().getResource("/FXML/Lobby.css").toExternalForm();
        String statusCss = getClass().getResource("/FXML/PlayerStatus.css").toExternalForm();
        if (!scene.getStylesheets().contains(lobbyCss))  scene.getStylesheets().add(lobbyCss);
        if (!scene.getStylesheets().contains(statusCss)) scene.getStylesheets().add(statusCss);
    }
}
