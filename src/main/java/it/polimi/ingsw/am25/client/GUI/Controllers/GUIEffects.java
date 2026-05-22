package it.polimi.ingsw.am25.client.GUI.Controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.net.URL;

public class GUIEffects {

    public static final ColorAdjust GRAY = new ColorAdjust(0, -0.45, -0.1, 0);

    private GUIEffects() {
    }

    /**
     * Creates and returns a golden {@link DropShadow} effect,
     * used to highlight selected elements in the interface.
     *
     * @return a new {@link DropShadow} with gold colour, radius 25, and spread 0.4.
     */
    /** Sets the application icon on the given stage and on the OS taskbar. */
    public static void applyIcon(Stage stage) {
        URL url = GUIEffects.class.getResource("/images/frontScreen.png");
        if (url == null) return;

        for (int size : new int[]{16, 32, 64, 256}) {
            stage.getIcons().add(new Image(url.toExternalForm(), size, size, true, true));
        }

        // AWT Taskbar API — used on macOS/Windows (not needed on Linux where .desktop handles it)
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            try {
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar tb = Taskbar.getTaskbar();
                    if (tb.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        final java.awt.Image img = ImageIO.read(url);
                        java.awt.EventQueue.invokeLater(() -> tb.setIconImage(img));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static DropShadow goldGlow() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.GOLD);
        glow.setRadius(25);
        glow.setSpread(0.4);
        return glow;
    }

    /**
     * Shows a modal error dialog with the specified message.
     * If the message is {@code null}, the text "Errore sconosciuto" is displayed.
     *
     * @param message the error text to show the user.
     */
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
