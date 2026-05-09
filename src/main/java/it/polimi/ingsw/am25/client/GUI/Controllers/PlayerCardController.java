package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for PlayerCard.fxml.
 *
 * <p>Call {@link #populate(PlayerDTO, boolean, boolean)} after loading to inject data.
 * Styling comes entirely from PlayerCard.css; totem color is applied via a CSS class.
 * The tribe section shows actual card image thumbnails grouped by type.
 */
public class PlayerCardController {

    private static final double CARD_THUMB_H = 62.0;
    private static final int    MAX_THUMBS   = 8;

    @FXML private VBox      cardRoot;
    @FXML private ImageView totemImageView;
    @FXML private Label     nameLabel;
    @FXML private Label     colorLabel;
    @FXML private Label     statusLabel;
    @FXML private Label     meLabel;
    @FXML private Label     foodLabel;
    @FXML private Label     ppLabel;
    @FXML private Label     discountLabel;
    @FXML private Label     shamanLabel;
    @FXML private VBox      tribeSection;
    @FXML private Label     tribeTitleLabel;

    // =========================================================
    // Public API
    // =========================================================

    public void populate(PlayerDTO player, boolean isMe, boolean disconnected) {
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
            addTribeTypeRow("Cacciatori",   byType(tribe, CARD_TYPE.HUNTER));
            addTribeTypeRow("Raccoglitori", byType(tribe, CARD_TYPE.GATHERER));
            addTribeTypeRow("Artisti",      byType(tribe, CARD_TYPE.ARTIST));
            addTribeTypeRow("Sciamani",     byType(tribe, CARD_TYPE.SHAMAN));
            addTribeTypeRow("Costruttori",  byType(tribe, CARD_TYPE.BUILDER));
            addTribeTypeRow("Inventori",    byType(tribe, CARD_TYPE.INVENTOR));
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
            tribeSection.getChildren().add(
                styledLabel("  › " + c, "tribe-building-text"));
        }
    }

    // =========================================================
    // Image helpers
    // =========================================================

    private void loadTotemImage(COLOR color) {
        try {
            Image img = new Image(getClass().getResourceAsStream(CardImageFactory.totemPath(color)));
            totemImageView.setImage(img);
        } catch (Exception ignored) {}
    }

    private ImageView cardThumb(CardDTO card) {
        String path = cardImagePath(card);
        if (path == null) return null;
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(path)));
            iv.setFitHeight(CARD_THUMB_H);
            iv.setPreserveRatio(true);
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
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(path)));
            iv.setFitHeight(CARD_THUMB_H);
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    /** Returns the resource path for a tribe card image, mirroring CardImageFactory logic. */
    private String cardImagePath(CardDTO card) {
        return switch (card.getCardType()) {
            case GATHERER -> "/images/Card/gatherer/Gatherer.png";
            case HUNTER   -> card.isHasIcon()
                    ? "/images/Card/hunters/hunterWIcon.png"
                    : "/images/Card/hunters/hunterNormal.png";
            case SHAMAN   -> "/images/Card/shaman/" + shamanImageName(card.getStarNumber()) + "Shaman.png";
            case INVENTOR -> card.getInvIcon() == null ? null
                    : "/images/Card/inventors/" + invImageName(card.getInvIcon()) + "Inventor.png";
            case BUILDER  -> "/images/Card/builders/" + card.getBuilderID() + "IDBuilder.png";
            default       -> "/images/Card/artist/Artist.png";
        };
    }

    // =========================================================
    // Computed stats
    // =========================================================

    private int computeBuildingDiscount(List<CardDTO> tribe) {
        if (tribe == null) return 0;
        return tribe.stream()
                .filter(c -> c.getCardType() == CARD_TYPE.BUILDER)
                .mapToInt(CardDTO::getFoodDiscount).sum();
    }

    private int computeShamanStars(List<CardDTO> tribe) {
        if (tribe == null) return 0;
        return tribe.stream()
                .filter(c -> c.getCardType() == CARD_TYPE.SHAMAN && c.getStarNumber() != null)
                .mapToInt(c -> shamanStarValue(c.getStarNumber())).sum();
    }

    // =========================================================
    // Small helpers
    // =========================================================

    private List<CardDTO> byType(List<CardDTO> tribe, CARD_TYPE type) {
        return tribe.stream().filter(c -> c.getCardType() == type).collect(Collectors.toList());
    }

    private int shamanStarValue(SHAMAN_STAR star) {
        return switch (star) { case ONE -> 1; case TWO -> 2; case THREE -> 3; };
    }

    private String shamanImageName(SHAMAN_STAR star) {
        if (star == null) return "oneStar";
        return switch (star) { case ONE -> "oneStar"; case TWO -> "twoStar"; case THREE -> "threeStar"; };
    }

    private String invImageName(INV_ICON icon) {
        return switch (icon) {
            case BREAD    -> "bread";
            case STONE    -> "stone";
            case NECKLACE -> "necklace";
            case BAIT     -> "bait";
            case GHOST    -> "ghost";
            case ARROW    -> "arrow";
            case LEATHER  -> "leather";
            case ROPE     -> "rope";
            case FLUTE    -> "flute";
            case BOWL     -> "bowl";
            default       -> "bread";
        };
    }

    private String colorName(COLOR color) {
        if (color == null) return "?";
        return switch (color) {
            case RED    -> "ROSSO";
            case BLUE   -> "BLU";
            case YELLOW -> "GIALLO";
            case WHITE  -> "BIANCO";
            case PURPLE -> "VIOLA";
        };
    }

    private String totemStyleClass(COLOR color) {
        if (color == null) return "totem-white";
        return switch (color) {
            case RED    -> "totem-red";
            case BLUE   -> "totem-blue";
            case YELLOW -> "totem-yellow";
            case WHITE  -> "totem-white";
            case PURPLE -> "totem-purple";
        };
    }

    private Label styledLabel(String text, String styleClass) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add(styleClass);
        return lbl;
    }
}
