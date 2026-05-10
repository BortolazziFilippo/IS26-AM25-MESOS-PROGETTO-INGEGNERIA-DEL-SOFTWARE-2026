package it.polimi.ingsw.am25.client.GUI.Controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class GUIEffects {

    public static final ColorAdjust GRAY = new ColorAdjust(0, -0.45, -0.1, 0);

    private GUIEffects() {
    }

    public static DropShadow goldGlow() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.GOLD);
        glow.setRadius(25);
        glow.setSpread(0.4);
        return glow;
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        Label content = new Label(message != null ? message : "Errore sconosciuto");
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}
