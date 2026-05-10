package it.polimi.ingsw.am25.client.GUI;

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
 * Splash screen per il gioco MESOS — IS26-AM25.
 *
 * Mostra la copertina del gioco con:
 *  - effetto "respiro" (scale + brightness che pulsano lentamente)
 *  - particelle di fuoco sovrapposte (cerchi arancioni animati)
 *  - testo "Tocca per iniziare" che lampeggia
 *
 * USO:
 *   Chiama SplashScreen.show(stage, onFinished) dal tuo main JavaFX.
 *   Quando il giocatore clicca, viene eseguito il Runnable onFinished
 *   (tipicamente: carica la lobby e mostra la finestra principale).
 *
 * RISORSA RICHIESTA:
 *   Metti frontScreen.png nella cartella src/main/resources/images/
 *   (o adatta il path nel metodo loadImage()).
 */
public class MesosSplashScreen {

    // ---------------------------------------------------------------
    // Costanti di layout
    // ---------------------------------------------------------------
    private static final int    WINDOW_W      = 900;
    private static final int    WINDOW_H      = 900;
    private static final double BREATHE_MIN   = 1.00; // scala minima
    private static final double BREATHE_MAX   = 1.04; // scala massima (4% di "gonfiamento")
    private static final int    BREATHE_MS    = 3200; // durata di un ciclo respiro (ms)
    private static final int    BLINK_MS      = 900;  // lampeggio testo (ms)
    private static final int    NUM_PARTICLES = 18;   // particelle di fuoco

    // ---------------------------------------------------------------
    // Entry point standalone (per test; rimuovi se integri nel progetto)
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        Application.launch(StandaloneApp.class, args);
    }

    public static class StandaloneApp extends Application {
        @Override
        public void start(Stage stage) {
            MesosSplashScreen.show(stage, () -> {
                System.out.println("Splash terminata — carica la lobby!");
                stage.close();
            });
        }
    }

    // ---------------------------------------------------------------
    // Metodo principale da chiamare dall'esterno
    // ---------------------------------------------------------------

    /**
     * Mostra la splash screen sul {@code stage} fornito.
     * Quando l'utente clicca, vengono fermati tutti gli effetti
     * e viene eseguito {@code onFinished}.
     */
    public static void show(Stage stage, Runnable onFinished) {

        // --- Root pane ---
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0d0805;");

        // --- Immagine di copertina ---
        ImageView cover = loadCoverImage();
        cover.setFitWidth(WINDOW_W);
        cover.setFitHeight(WINDOW_H);
        cover.setPreserveRatio(true);
        root.getChildren().add(cover);

        // --- Overlay scuro ai bordi (vignette) ---
        javafx.scene.shape.Rectangle vignette = new javafx.scene.shape.Rectangle(WINDOW_W, WINDOW_H);
        vignette.setFill(javafx.scene.paint.Color.TRANSPARENT);
        vignette.setStyle(
            "-fx-fill: radial-gradient(center 50% 50%, radius 70%, " +
            "transparent 0%, rgba(0,0,0,0.55) 100%);"
        );
        root.getChildren().add(vignette);

        // --- Particelle fuoco ---
        StackPane particleLayer = new StackPane();
        particleLayer.setPickOnBounds(false);
        addFireParticles(particleLayer);
        root.getChildren().add(particleLayer);

        // --- Testo "Premi per iniziare" ---
        Text prompt = new Text("— tocca per iniziare —");
        prompt.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        prompt.setFill(Color.web("#f5dfa0"));
        prompt.setStyle(
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 8, 0.5, 0, 2);"
        );
        StackPane.setAlignment(prompt, Pos.BOTTOM_CENTER);
        StackPane.setMargin(prompt, new javafx.geometry.Insets(0, 0, 48, 0));
        root.getChildren().add(prompt);

        // --- Scena & Stage ---
        Scene scene = new Scene(root, WINDOW_W, WINDOW_H);
        stage.setScene(scene);
        stage.setTitle("MESOS — IS26-AM25");
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);
        stage.show();

        // =============================================================
        // ANIMAZIONI
        // =============================================================

        // 1. Respiro: scala leggermente l'immagine avanti e indietro
        ScaleTransition breathe = new ScaleTransition(Duration.millis(BREATHE_MS), cover);
        breathe.setFromX(BREATHE_MIN);
        breathe.setFromY(BREATHE_MIN);
        breathe.setToX(BREATHE_MAX);
        breathe.setToY(BREATHE_MAX);
        breathe.setAutoReverse(true);
        breathe.setCycleCount(Animation.INDEFINITE);
        breathe.setInterpolator(Interpolator.EASE_BOTH);
        breathe.play();

        // 2. Luminosità pulsante sincrona col respiro (usa ColorAdjust)
        javafx.scene.effect.ColorAdjust glow = new javafx.scene.effect.ColorAdjust();
        cover.setEffect(glow);

        Timeline brightnessTimeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(glow.brightnessProperty(), -0.05, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(BREATHE_MS),
                new KeyValue(glow.brightnessProperty(),  0.08, Interpolator.EASE_BOTH))
        );
        brightnessTimeline.setAutoReverse(true);
        brightnessTimeline.setCycleCount(Animation.INDEFINITE);
        brightnessTimeline.play();

        // 3. Lampeggio testo
        FadeTransition blink = new FadeTransition(Duration.millis(BLINK_MS), prompt);
        blink.setFromValue(1.0);
        blink.setToValue(0.25);
        blink.setAutoReverse(true);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.setInterpolator(Interpolator.EASE_BOTH);
        blink.play();

        // =============================================================
        // CLICK → transizione fade-out ed esecuzione callback
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
    // Helper: carica l'immagine dal classpath
    // ---------------------------------------------------------------
    private static ImageView loadCoverImage() {
        ImageView iv = new ImageView();
        try {
            // Cerca prima nel classpath (jar), poi come file locale
            java.io.InputStream stream =
                MesosSplashScreen.class.getResourceAsStream("/images/frontScreen.png");

            if (stream == null) {
                // Fallback: prova path relativo (utile in sviluppo)
                java.io.File f = new java.io.File("src/main/resources/images/frontScreen.png");
                if (f.exists()) {
                    stream = new java.io.FileInputStream(f);
                }
            }
            if (stream != null) {
                iv.setImage(new Image(stream));
            } else {
                System.err.println("[SplashScreen] frontScreen.png non trovata! " +
                    "Mettila in src/main/resources/images/");
                // Sfondo arancione di emergenza
                iv.setStyle("-fx-background-color: #c44020;");
            }
        } catch (Exception ex) {
            System.err.println("[SplashScreen] Errore caricamento immagine: " + ex.getMessage());
        }
        return iv;
    }

    // ---------------------------------------------------------------
    // Helper: particelle fuoco (cerchietti arancio/gialli animati)
    // ---------------------------------------------------------------
    private static void addFireParticles(StackPane layer) {
        java.util.Random rnd = new java.util.Random(42);

        for (int i = 0; i < NUM_PARTICLES; i++) {
            // Posizione: concentrata nella metà inferiore (dove c'è il fuoco)
            double startX = (rnd.nextDouble() - 0.5) * WINDOW_W * 0.35; // -175..+175 px dal centro
            double startY =  rnd.nextDouble() * 180 + 180;               // 180..360 px dal centro

            // Dimensione e colore casuali
            double radius  = rnd.nextDouble() * 5 + 2;
            String[] colors = {"#ff8800", "#ffaa00", "#ffcc44", "#ff6600", "#ffdd88"};
            String color = colors[rnd.nextInt(colors.length)];

            javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(radius);
            c.setFill(Color.web(color, 0.7));
            c.setTranslateX(startX);
            c.setTranslateY(startY);
            c.setEffect(new javafx.scene.effect.GaussianBlur(radius * 0.8));
            c.setPickOnBounds(false);
            layer.getChildren().add(c);

            // Animazione: sali verso l'alto, dissolviti
            double duration = rnd.nextDouble() * 2000 + 1500; // 1.5–3.5 s
            double delay    = rnd.nextDouble() * 2500;

            TranslateTransition rise = new TranslateTransition(Duration.millis(duration), c);
            rise.setByY(-(rnd.nextDouble() * 220 + 120)); // sale 120–340 px
            rise.setByX((rnd.nextDouble() - 0.5) * 60);   // leggera deriva laterale
            rise.setCycleCount(Animation.INDEFINITE);
            rise.setInterpolator(Interpolator.EASE_IN);
            rise.setDelay(Duration.millis(delay));

            FadeTransition fade = new FadeTransition(Duration.millis(duration), c);
            fade.setFromValue(0.7);
            fade.setToValue(0.0);
            fade.setCycleCount(Animation.INDEFINITE);
            fade.setDelay(Duration.millis(delay));

            // Reset posizione ad ogni ciclo
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
}
