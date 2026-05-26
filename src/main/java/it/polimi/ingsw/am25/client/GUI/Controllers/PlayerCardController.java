package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * JavaFX controller that renders a player's tribe cards as thumbnail images.
 * Used inside the player-status panel and the player list in the main game view.
 */
public class PlayerCardController {

    /** Default constructor used by the JavaFX FXML loader. */
    public PlayerCardController() {}

    private static final double CARD_THUMB_H = 62.0;
    private static final int MAX_THUMBS = 8;

    @FXML
    private VBox cardRoot;
    @FXML
    private ImageView totemImageView;
    @FXML
    private Label nameLabel;
    @FXML
    private Label colorLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label turnLabel;
    @FXML
    private Label meLabel;
    @FXML
    private Label foodLabel;
    @FXML
    private Label ppLabel;
    @FXML
    private Label discountLabel;
    @FXML
    private Label shamanLabel;
    @FXML
    private VBox tribeSection;
    @FXML
    private Label tribeTitleLabel;

    // =========================================================
    // Public API
    // =========================================================

    /**
     * Populates the player card with data from the provided DTO.
     * Sets the totem color, labels, connection status, turn badge, and the rows
     * of card thumbnails for each card type present in the tribe.
     *
     * @param player        the DTO of the player to display.
     * @param isMe          {@code true} if the player corresponds to the local client.
     * @param disconnected  {@code true} if the player is currently disconnected.
     * @param isCurrentTurn {@code true} if it is this player's turn.
     */
    public void populate(PlayerDTO player, boolean isMe, boolean disconnected, boolean isCurrentTurn) {
        List<CardDTO> tribe = player.getCardDtoList();

        // Totem color: CSS class + image
        cardRoot.getStyleClass().add(totemStyleClass(player.getColorTotem()));
        loadTotemImage(player.getColorTotem());

        // Header labels
        nameLabel.setText(player.getNickName());
        colorLabel.setText(colorName(player.getColorTotem()));
        if (disconnected) {
            statusLabel.setText("DISCONNESSO");
            statusLabel.getStyleClass().remove("player-status-online");
            statusLabel.getStyleClass().add("player-status-disconnected");
        } else {
            statusLabel.setText("ONLINE");
        }
        meLabel.setVisible(isMe);
        meLabel.setManaged(isMe);

        // Turn badge
        turnLabel.setVisible(isCurrentTurn);
        turnLabel.setManaged(isCurrentTurn);
        if (isCurrentTurn) {
            FadeTransition pulse = new FadeTransition(Duration.millis(700), turnLabel);
            pulse.setFromValue(1.0);
            pulse.setToValue(0.35);
            pulse.setCycleCount(Timeline.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
        }

        // Stats
        foodLabel.setText(String.valueOf(player.getFood()));
        ppLabel.setText(String.valueOf(player.getPrestigePoint()));
        discountLabel.setText(computeBuildingDiscount(tribe) + " cibo");
        shamanLabel.setText(String.valueOf(computeShamanStars(tribe)));

        // Tribe
        int total = tribe == null ? 0 : tribe.size();
        tribeTitleLabel.setText("TRIBU'  ·  " + total + " carte");

        if (tribe == null || tribe.isEmpty()) {
            tribeSection.getChildren().add(styledLabel("nessuna carta", "tribe-empty"));
        } else {
            addTribeTypeRow("Cacciatori", byType(tribe, CARD_TYPE.HUNTER));
            addTribeTypeRow("Raccoglitori", byType(tribe, CARD_TYPE.GATHERER));
            addTribeTypeRow("Artisti", byType(tribe, CARD_TYPE.ARTIST));
            addTribeTypeRow("Sciamani", byType(tribe, CARD_TYPE.SHAMAN));
            addTribeTypeRow("Costruttori", byType(tribe, CARD_TYPE.BUILDER));
            addTribeTypeRow("Inventori", byType(tribe, CARD_TYPE.INVENTOR));
            addBuildingRows(byType(tribe, CARD_TYPE.BUILDING));
        }
    }

    // =========================================================
    // Tribe rows with card thumbnails
    // =========================================================

    private void addTribeTypeRow(String typeName, List<CardDTO> cards) {
        if (cards.isEmpty()) return;

        HBox row = new HBox(10);
        row.getStyleClass().add("tribe-type-row");

        Label typeLabel = styledLabel(typeName + "  (" + cards.size() + ")", "tribe-type-label");
        row.getChildren().add(typeLabel);

        HBox strip = new HBox(4);
        strip.getStyleClass().add("tribe-image-strip");

        int shown = Math.min(cards.size(), MAX_THUMBS);
        for (int i = 0; i < shown; i++) {
            ImageView iv = cardThumb(cards.get(i));
            if (iv != null) strip.getChildren().add(iv);
        }
        if (cards.size() > MAX_THUMBS) {
            strip.getChildren().add(styledLabel("+" + (cards.size() - MAX_THUMBS), "tribe-more-label"));
        }

        row.getChildren().add(strip);
        tribeSection.getChildren().add(row);
    }

    private void addBuildingRows(List<CardDTO> buildings) {
        if (buildings.isEmpty()) return;

        HBox row = new HBox(10);
        row.getStyleClass().add("tribe-type-row");
        row.getChildren().add(styledLabel("Edifici  (" + buildings.size() + ")", "tribe-type-label"));

        HBox strip = new HBox(4);
        strip.getStyleClass().add("tribe-image-strip");

        int shown = Math.min(buildings.size(), MAX_THUMBS);
        for (int i = 0; i < shown; i++) {
            ImageView iv = buildingThumb((BuildingDTO) buildings.get(i));
            if (iv != null) strip.getChildren().add(iv);
        }
        if (buildings.size() > MAX_THUMBS)
            strip.getChildren().add(styledLabel("+" + (buildings.size() - MAX_THUMBS), "tribe-more-label"));

        row.getChildren().add(strip);
        tribeSection.getChildren().add(row);

        // Tooltip-style text list under the images
        for (CardDTO c : buildings) {
            tribeSection.getChildren().add(styledLabel("  › " + c, "tribe-building-text"));
        }
    }

    // =========================================================
    // Image helpers
    // =========================================================

    private void loadTotemImage(COLOR color) {
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(CardImageFactory.totemPath(color))));
            totemImageView.setImage(img);
        } catch (Exception ignored) {
        }
    }

    private ImageView cardThumb(CardDTO card) {
        String path = cardImagePath(card);
        if (path == null) return null;
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            ImageView iv = new ImageView(img);
            iv.setFitHeight(CARD_THUMB_H);
            iv.setPreserveRatio(true);
            attachTooltip(iv, tooltipText(card));
            makeClickable(iv, img, tooltipText(card));
            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    private ImageView buildingThumb(BuildingDTO bld) {
        try {
            int id = bld.getBuildingID();
            String era = id <= 6 ? "eraOne" : id <= 13 ? "eraTwo" : "eraThree";
            String path = "/images/Card/Buildings/" + era + "/" + id + "IDbuilding.png";
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            ImageView iv = new ImageView(img);
            iv.setFitHeight(CARD_THUMB_H);
            iv.setPreserveRatio(true);
            attachTooltip(iv, bld.toString());
            makeClickable(iv, img, bld.toString());
            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Adds hover glow + hand cursor + click-to-enlarge to a thumbnail.
     */
    private void makeClickable(ImageView iv, Image fullImage, String description) {
        iv.setStyle("-fx-cursor: hand;");

        DropShadow glow = new DropShadow(12, Color.web("#d4a017"));
        iv.setOnMouseEntered(e -> {
            iv.setEffect(glow);
            iv.setScaleX(1.06);
            iv.setScaleY(1.06);
        });
        iv.setOnMouseExited(e -> {
            iv.setEffect(null);
            iv.setScaleX(1.0);
            iv.setScaleY(1.0);
        });
        iv.setOnMouseClicked(e -> showEnlargedCard(fullImage, description));
    }

    /**
     * Opens a floating popup with the card image at full size + its description.
     */
    private void showEnlargedCard(Image image, String description) {
        // Large image
        ImageView bigIv = new ImageView(image);
        bigIv.setFitHeight(340);
        bigIv.setPreserveRatio(true);

        // Description lines
        VBox descBox = new VBox(4);
        descBox.setAlignment(Pos.CENTER);
        for (String line : description.split("\n")) {
            Label lbl = new Label(line);
            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #e8d4a0;");
            descBox.getChildren().add(lbl);
        }

        // Hint
        Label hint = new Label("clic o ESC per chiudere");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #6a5a40; -fx-padding: 10 0 0 0;");

        VBox root = new VBox(14, bigIv, descBox, hint);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1408;" + "-fx-background-radius: 12;" + "-fx-border-color: #3d2b10;" + "-fx-border-width: 1;" + "-fx-border-radius: 12;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 24, 0.5, 0, 8);");

        Scene scene = new Scene(root, Color.TRANSPARENT);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/FXML/PlayerCard.css")).toExternalForm());

        Stage popup = new Stage(StageStyle.TRANSPARENT);
        popup.setScene(scene);

        // Close on click anywhere or ESC
        root.setOnMouseClicked(e -> popup.close());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) popup.close();
        });

        popup.show();
    }

    /**
     * Creates and installs a styled tooltip on the given node.
     */
    private void attachTooltip(ImageView iv, String text) {
        Tooltip tt = new Tooltip(text);
        tt.setShowDelay(Duration.millis(300));
        tt.setStyle("-fx-background-color: #2a1c0e;" + "-fx-text-fill: #e8d4a0;" + "-fx-font-size: 12px;" + "-fx-border-color: #d4a017;" + "-fx-border-width: 1;" + "-fx-border-radius: 5;" + "-fx-background-radius: 5;" + "-fx-padding: 6 10 6 10;");
        Tooltip.install(iv, tt);
    }

    /**
     * Returns a human-readable description of a tribe card for its tooltip.
     */
    private String tooltipText(CardDTO card) {
        return switch (card.getCardType()) {
            case HUNTER ->
                    card.isHasIcon() ? "Cacciatore\nHa l'icona — guadagni cibo immediato" : "Cacciatore\nSenza icona";
            case GATHERER -> "Raccoglitore";
            case ARTIST -> "Artista";
            case SHAMAN -> "Sciamano\n" + shamanTooltip(card.getStarNumber());
            case BUILDER ->
                    "Costruttore\nSconto edifici: " + card.getFoodDiscount() + " cibo\nPP a fine partita: " + card.getFinalPrestigePoint();
            case INVENTOR -> "Inventore\nIcona: " + (card.getInvIcon() != null ? card.getInvIcon() : "?");
            default -> card.getCardType().name();
        };
    }

    private String shamanTooltip(SHAMAN_STAR star) {
        if (star == null) return "stelle: ?";
        return switch (star) {
            case ONE -> "1 stella  — contribuisce 1 al totale sciamano";
            case TWO -> "2 stelle — contribuisce 2 al totale sciamano";
            case THREE -> "3 stelle — contribuisce 3 al totale sciamano";
        };
    }

    /**
     * Returns the resource path for a tribe card image, mirroring CardImageFactory logic.
     */
    private String cardImagePath(CardDTO card) {
        return switch (card.getCardType()) {
            case GATHERER -> "/images/Card/gatherer/Gatherer.png";
            case HUNTER ->
                    card.isHasIcon() ? "/images/Card/hunters/hunterWIcon.png" : "/images/Card/hunters/hunterNormal.png";
            case SHAMAN -> "/images/Card/shaman/" + shamanImageName(card.getStarNumber()) + "Shaman.png";
            case INVENTOR ->
                    card.getInvIcon() == null ? null : "/images/Card/inventors/" + invImageName(card.getInvIcon()) + "Inventor.png";
            case BUILDER -> "/images/Card/builders/" + card.getBuilderID() + "IDBuilder.png";
            default -> "/images/Card/artist/Artist.png";
        };
    }

    // =========================================================
    // Computed stats
    // =========================================================

    private int computeBuildingDiscount(List<CardDTO> tribe) {
        if (tribe == null) return 0;
        return tribe.stream().filter(c -> c.getCardType() == CARD_TYPE.BUILDER).mapToInt(CardDTO::getFoodDiscount).sum();
    }

    private int computeShamanStars(List<CardDTO> tribe) {
        if (tribe == null) return 0;
        return tribe.stream().filter(c -> c.getCardType() == CARD_TYPE.SHAMAN && c.getStarNumber() != null).mapToInt(c -> shamanStarValue(c.getStarNumber())).sum();
    }

    // =========================================================
    // Small helpers
    // =========================================================

    private List<CardDTO> byType(List<CardDTO> tribe, CARD_TYPE type) {
        return tribe.stream().filter(c -> c.getCardType() == type).collect(Collectors.toList());
    }

    private int shamanStarValue(SHAMAN_STAR star) {
        return switch (star) {
            case ONE -> 1;
            case TWO -> 2;
            case THREE -> 3;
        };
    }

    private String shamanImageName(SHAMAN_STAR star) {
        if (star == null) return "oneStar";
        return switch (star) {
            case ONE -> "oneStar";
            case TWO -> "twoStar";
            case THREE -> "threeStar";
        };
    }

    private String invImageName(INV_ICON icon) {
        return switch (icon) {
            case STONE -> "stone";
            case NECKLACE -> "necklace";
            case BAIT -> "bait";
            case GHOST -> "ghost";
            case ARROW -> "arrow";
            case LEATHER -> "leather";
            case ROPE -> "rope";
            case FLUTE -> "flute";
            case BOWL -> "bowl";
            default -> "bread";
        };
    }

    private String colorName(COLOR color) {
        if (color == null) return "?";
        return switch (color) {
            case RED -> "ROSSO";
            case BLUE -> "BLU";
            case YELLOW -> "GIALLO";
            case WHITE -> "BIANCO";
            case PURPLE -> "VIOLA";
        };
    }

    private String totemStyleClass(COLOR color) {
        if (color == null) return "totem-white";
        return switch (color) {
            case RED -> "totem-red";
            case BLUE -> "totem-blue";
            case YELLOW -> "totem-yellow";
            case WHITE -> "totem-white";
            case PURPLE -> "totem-purple";
        };
    }

    private Label styledLabel(String text, String styleClass) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add(styleClass);
        return lbl;
    }
}
