package it.polimi.ingsw.am25.client.GUI;

import it.polimi.ingsw.am25.client.GUI.Controllers.GUIEffects;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Splash screen for the MESOS board game — IS26-AM25.
 * <p>
 * Displays the game cover with:
 * - a "breathing" effect (scale and brightness pulsing slowly)
 * - overlaid fire particles (animated orange circles)
 * - a blinking "touch to start" prompt
 * <p>
 * USAGE:
 * Call {@code MesosSplashScreen.show(stage, onFinished)} from your JavaFX main.
 * When the player clicks, the {@code onFinished} Runnable is executed
 * (typically: load the lobby and show the main window).
 * <p>
 * REQUIRED RESOURCE:
 * Place {@code frontScreen.png} in {@code src/main/resources/images/}
 * (or adapt the path in {@code loadCoverImage()}).
 */
public class MesosSplashScreen {

    /**
     * Private constructor — all methods are static; this class is not meant to be instantiated.
     */
    private MesosSplashScreen() {}

    // ---------------------------------------------------------------
    // Layout constants
    // ---------------------------------------------------------------
    private static final int WINDOW_W = 900;
    private static final int WINDOW_H = 900;
    private static final double BREATHE_MIN = 1.00; // minimum scale
    private static final double BREATHE_MAX = 1.04; // maximum scale (4% "inflate")
    private static final int BREATHE_MS = 3200; // duration of one breathe cycle (ms)
    private static final int BLINK_MS = 900;  // text blink duration (ms)
    private static final int NUM_PARTICLES = 18;   // fire particles

    // ---------------------------------------------------------------
    // Standalone entry point (for testing; remove when integrating into the project)
    // ---------------------------------------------------------------
    static void main(String[] args) {
        Application.launch(StandaloneApp.class, args);
    }

    /**
     * Displays the splash screen on the provided {@code stage}.
     * When the user clicks, all running animations are stopped
     * and {@code onFinished} is executed.
     *
     * @param stage      the JavaFX stage on which to display the splash screen.
     * @param onFinished the callback to run once the user dismisses the splash screen.
     */
    public static void show(Stage stage, Runnable onFinished) {

        // --- Root pane ---
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0d0805;");

        // --- Cover image ---
        ImageView cover = loadCoverImage();
        cover.setFitWidth(WINDOW_W);
        cover.setFitHeight(WINDOW_H);
        cover.setPreserveRatio(true);
        root.getChildren().add(cover);

        // --- Dark edge overlay (vignette) ---
        javafx.scene.shape.Rectangle vignette = new javafx.scene.shape.Rectangle(WINDOW_W, WINDOW_H);
        vignette.setFill(javafx.scene.paint.Color.TRANSPARENT);
        vignette.setStyle("-fx-fill: radial-gradient(center 50% 50%, radius 70%, " + "transparent 0%, rgba(0,0,0,0.55) 100%);");
        root.getChildren().add(vignette);

        // --- Fire particles ---
        StackPane particleLayer = new StackPane();
        particleLayer.setPickOnBounds(false);
        addFireParticles(particleLayer);
        root.getChildren().add(particleLayer);

        // --- "Press to start" prompt ---
        Text prompt = new Text("— tocca per iniziare —");
        prompt.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        prompt.setFill(Color.web("#f5dfa0"));
        prompt.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 8, 0.5, 0, 2);");
        StackPane.setAlignment(prompt, Pos.BOTTOM_CENTER);
        StackPane.setMargin(prompt, new javafx.geometry.Insets(0, 0, 48, 0));
        root.getChildren().add(prompt);

        // --- Scene & Stage ---
        Scene scene = new Scene(root, WINDOW_W, WINDOW_H);
        stage.setScene(scene);
        stage.setTitle("MESOS — IS26-AM25");
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);
        GUIEffects.applyIcon(stage);
        stage.show();

        // =============================================================
        // ANIMATIONS
        // =============================================================

        // 1. Breathe: gently scale the image back and forth
        ScaleTransition breathe = new ScaleTransition(Duration.millis(BREATHE_MS), cover);
        breathe.setFromX(BREATHE_MIN);
        breathe.setFromY(BREATHE_MIN);
        breathe.setToX(BREATHE_MAX);
        breathe.setToY(BREATHE_MAX);
        breathe.setAutoReverse(true);
        breathe.setCycleCount(Animation.INDEFINITE);
        breathe.setInterpolator(Interpolator.EASE_BOTH);
        breathe.play();

        // 2. Brightness pulsing in sync with the breathe animation (via ColorAdjust)
        javafx.scene.effect.ColorAdjust glow = new javafx.scene.effect.ColorAdjust();
        cover.setEffect(glow);

        Timeline brightnessTimeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(glow.brightnessProperty(), -0.05, Interpolator.EASE_BOTH)), new KeyFrame(Duration.millis(BREATHE_MS), new KeyValue(glow.brightnessProperty(), 0.08, Interpolator.EASE_BOTH)));
        brightnessTimeline.setAutoReverse(true);
        brightnessTimeline.setCycleCount(Animation.INDEFINITE);
        brightnessTimeline.play();

        // 3. Text blink
        FadeTransition blink = new FadeTransition(Duration.millis(BLINK_MS), prompt);
        blink.setFromValue(1.0);
        blink.setToValue(0.25);
        blink.setAutoReverse(true);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.setInterpolator(Interpolator.EASE_BOTH);
        blink.play();

        // =============================================================
        // CLICK → fade-out transition and callback execution
        // =============================================================
        scene.setOnMouseClicked(e -> {
            breathe.stop();
            brightnessTimeline.stop();
            blink.stop();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> onFinished.run());
            fadeOut.play();
        });
    }

    // ---------------------------------------------------------------
    // Helper: loads the cover image from the classpath
    // ---------------------------------------------------------------
    private static ImageView loadCoverImage() {
        ImageView iv = new ImageView();
        try {
            // Try the classpath first (jar), then as a local file
            java.io.InputStream stream = MesosSplashScreen.class.getResourceAsStream("/images/frontScreen.png");

            if (stream == null) {
                // Fallback: try relative path (useful during development)
                java.io.File f = new java.io.File("src/main/resources/images/frontScreen.png");
                if (f.exists()) {
                    stream = new java.io.FileInputStream(f);
                }
            }
            if (stream != null) {
                iv.setImage(new Image(stream));
            } else {
                System.err.println("[SplashScreen] frontScreen.png non trovata! " + "Mettila in src/main/resources/images/");
                // Emergency orange background fallback
                iv.setStyle("-fx-background-color: #c44020;");
            }
        } catch (Exception ex) {
            System.err.println("[SplashScreen] Errore caricamento immagine: " + ex.getMessage());
        }
        return iv;
    }

    // ---------------------------------------------------------------
    // Helper: fire particles (animated orange/yellow circles)
    // ---------------------------------------------------------------
    private static void addFireParticles(StackPane layer) {
        java.util.Random rnd = new java.util.Random(42);

        for (int i = 0; i < NUM_PARTICLES; i++) {
            // Position: concentrated in the lower half (where the fire is)
            double startX = (rnd.nextDouble() - 0.5) * WINDOW_W * 0.35; // -175..+175 px from center
            double startY = rnd.nextDouble() * 180 + 180;               // 180..360 px from center

            // Random size and color
            double radius = rnd.nextDouble() * 5 + 2;
            String[] colors = {"#ff8800", "#ffaa00", "#ffcc44", "#ff6600", "#ffdd88"};
            String color = colors[rnd.nextInt(colors.length)];

            javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(radius);
            c.setFill(Color.web(color, 0.7));
            c.setTranslateX(startX);
            c.setTranslateY(startY);
            c.setEffect(new javafx.scene.effect.GaussianBlur(radius * 0.8));
            c.setPickOnBounds(false);
            layer.getChildren().add(c);

            // Animation: rise upward and fade out
            double duration = rnd.nextDouble() * 2000 + 1500; // 1.5–3.5 s
            double delay = rnd.nextDouble() * 2500;

            TranslateTransition rise = new TranslateTransition(Duration.millis(duration), c);
            rise.setByY(-(rnd.nextDouble() * 220 + 120)); // rises 120–340 px
            rise.setByX((rnd.nextDouble() - 0.5) * 60);   // slight lateral drift
            rise.setCycleCount(Animation.INDEFINITE);
            rise.setInterpolator(Interpolator.EASE_IN);
            rise.setDelay(Duration.millis(delay));

            FadeTransition fade = new FadeTransition(Duration.millis(duration), c);
            fade.setFromValue(0.7);
            fade.setToValue(0.0);
            fade.setCycleCount(Animation.INDEFINITE);
            fade.setDelay(Duration.millis(delay));

            // Reset position on each cycle
            rise.statusProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == Animation.Status.RUNNING && oldVal == Animation.Status.STOPPED) {
                    c.setTranslateX(startX + (rnd.nextDouble() - 0.5) * 30);
                    c.setTranslateY(startY);
                    c.setOpacity(0.7);
                }
            });

            rise.play();
            fade.play();
        }
    }

    /**
     * Standalone JavaFX application used to test the splash screen animation in isolation,
     * without launching the full Mesos client.
     */
    public static class StandaloneApp extends Application {

        /**
         * Creates a new StandaloneApp instance. JavaFX instantiates this class reflectively via {@link Application#launch}.
         */
        public StandaloneApp() {}

        /**
         * Launches the standalone splash screen to test the animation in isolation.
         * When the splash screen closes, prints a message and closes the stage.
         *
         * @param stage the JavaFX stage provided by the runtime.
         */
        @Override
        public void start(Stage stage) {
            MesosSplashScreen.show(stage, () -> {
                System.out.println("Splash terminata — carica la lobby!");
                stage.close();
            });
        }
    }
}
