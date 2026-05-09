package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for PlayerCard.fxml.
 *
 * <p>After FXMLLoader creates this controller via the no-arg constructor,
 * call {@link #populate(PlayerDTO, boolean, boolean)} to inject player data.
 * The method fills all @FXML labels and builds the dynamic tribe rows.
 */
public class PlayerCardController {

    @FXML private VBox  cardRoot;
    @FXML private HBox  headerBox;
    @FXML private Label nameLabel;
    @FXML private Label colorLabel;
    @FXML private Label statusLabel;
    @FXML private Label meLabel;
    @FXML private Label foodLabel;
    @FXML private Label ppLabel;
    @FXML private Label discountLabel;
    @FXML private Label shamanLabel;
    @FXML private VBox  tribeSection;
    @FXML private Label tribeTitleLabel;

    // =========================================================
    // Public API
    // =========================================================

    /**
     * Fills the card with the given player's data.
     * Must be called after FXMLLoader has loaded the FXML and run initialize().
     *
     * @param player       the player to display.
     * @param isMe         true if this player is the local player.
     * @param disconnected true if this player is currently disconnected.
     */
    public void populate(PlayerDTO player, boolean isMe, boolean disconnected) {
        List<CardDTO> tribe = player.getCardDtoList();

        applyColors(player.getColorTotem());

        nameLabel.setText(player.getNickName());
        colorLabel.setText("[" + colorName(player.getColorTotem()) + "]");
        statusLabel.setText(disconnected ? " [DISCONNESSO]" : " [ONLINE]");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: "
                + (disconnected ? "#ffcc44" : "#ccffcc") + ";");

        meLabel.setVisible(isMe);
        meLabel.setManaged(isMe);

        foodLabel.setText(String.valueOf(player.getFood()));
        ppLabel.setText(String.valueOf(player.getPrestigePoint()));
        discountLabel.setText(computeBuildingDiscount(tribe) + " cibo");
        shamanLabel.setText(String.valueOf(computeShamanStars(tribe)));

        int total = tribe == null ? 0 : tribe.size();
        tribeTitleLabel.setText("TRIBU' (" + total + " carte):");

        if (tribe == null || tribe.isEmpty()) {
            tribeSection.getChildren().add(
                    makeLabel("  (nessuna carta)", 12, false, "#888888"));
        } else {
            addHunterRow(tribe);
            addGathererRow(tribe);
            addArtistRow(tribe);
            addShamanRow(tribe);
            addBuilderRow(tribe);
            addInventorRow(tribe);
            addBuildingRows(tribe);
        }
    }

    // =========================================================
    // Color application
    // =========================================================

    private void applyColors(COLOR color) {
        String border = borderColor(color);
        String bg     = cardBackground(color);
        cardRoot.setStyle(
            "-fx-border-color: " + border + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 8;" +
            "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: 8;"
        );
        headerBox.setStyle(
            "-fx-padding: 8 12 8 12;" +
            "-fx-alignment: CENTER_LEFT;" +
            "-fx-background-color: " + border + ";" +
            "-fx-background-radius: 5 5 0 0;"
        );
    }

    // =========================================================
    // Tribe rows
    // =========================================================

    private void addHunterRow(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.HUNTER);
        if (list.isEmpty()) return;
        long withIcon    = list.stream().filter(CardDTO::isHasIcon).count();
        long withoutIcon = list.size() - withIcon;
        tribeSection.getChildren().add(tribeRow(
            "Cacciatori: " + list.size(),
            "(con icona: " + withIcon + " | senza icona: " + withoutIcon + ")"
        ));
    }

    private void addGathererRow(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.GATHERER);
        if (!list.isEmpty())
            tribeSection.getChildren().add(tribeRow("Raccoglitori: " + list.size(), ""));
    }

    private void addArtistRow(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.ARTIST);
        if (!list.isEmpty())
            tribeSection.getChildren().add(tribeRow("Artisti: " + list.size(), ""));
    }

    private void addShamanRow(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.SHAMAN);
        if (list.isEmpty()) return;
        String detail = list.stream()
                .map(c -> shamanStarLabel(c.getStarNumber()))
                .collect(Collectors.joining("  "));
        tribeSection.getChildren().add(tribeRow("Sciamani: " + list.size(), "[" + detail + "]"));
    }

    private void addBuilderRow(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.BUILDER);
        if (list.isEmpty()) return;
        String detail = list.stream()
                .map(c -> "sc." + c.getFoodDiscount() + " PP" + c.getFinalPrestigePoint())
                .collect(Collectors.joining(" | "));
        tribeSection.getChildren().add(tribeRow("Costruttori: " + list.size(), "[" + detail + "]"));
    }

    private void addInventorRow(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.INVENTOR);
        if (list.isEmpty()) return;
        String icons = list.stream()
                .map(c -> c.getInvIcon() == null ? "?" : iconName(c.getInvIcon()))
                .collect(Collectors.joining("  "));
        tribeSection.getChildren().add(tribeRow("Inventori: " + list.size(), icons));
    }

    private void addBuildingRows(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.BUILDING);
        if (list.isEmpty()) return;
        tribeSection.getChildren().add(tribeRow("Edifici: " + list.size(), ""));
        for (CardDTO card : list)
            tribeSection.getChildren().add(makeLabel("     > " + card, 11, false, "#444444"));
    }

    private HBox tribeRow(String left, String right) {
        HBox row = new HBox(8);
        row.getChildren().add(makeLabel(left, 12, true, "#222222"));
        if (!right.isEmpty()) row.getChildren().add(makeLabel(right, 12, false, "#555555"));
        return row;
    }

    // =========================================================
    // Computed stats
    // =========================================================

    private int computeBuildingDiscount(List<CardDTO> tribe) {
        if (tribe == null) return 0;
        return tribe.stream()
                .filter(c -> c.getCardType() == CARD_TYPE.BUILDER)
                .mapToInt(CardDTO::getFoodDiscount)
                .sum();
    }

    private int computeShamanStars(List<CardDTO> tribe) {
        if (tribe == null) return 0;
        return tribe.stream()
                .filter(c -> c.getCardType() == CARD_TYPE.SHAMAN && c.getStarNumber() != null)
                .mapToInt(c -> shamanStarValue(c.getStarNumber()))
                .sum();
    }

    // =========================================================
    // Helpers
    // =========================================================

    private List<CardDTO> byType(List<CardDTO> tribe, CARD_TYPE type) {
        return tribe.stream().filter(c -> c.getCardType() == type).collect(Collectors.toList());
    }

    private int shamanStarValue(SHAMAN_STAR star) {
        return switch (star) { case ONE -> 1; case TWO -> 2; case THREE -> 3; };
    }

    private String shamanStarLabel(SHAMAN_STAR star) {
        if (star == null) return "?";
        return switch (star) { case ONE -> "1*"; case TWO -> "2**"; case THREE -> "3***"; };
    }

    private String iconName(INV_ICON icon) {
        return switch (icon) {
            case BREAD    -> "PANE";
            case STONE    -> "PIETRA";
            case NECKLACE -> "COLLANA";
            case BAIT     -> "ESCA";
            case GHOST    -> "SPIRITO";
            case ARROW    -> "FRECCIA";
            case LEATHER  -> "CUOIO";
            case ROPE     -> "CORDA";
            case FLUTE    -> "FLAUTO";
            case BOWL     -> "CIOTOLA";
            default       -> icon.name();
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

    private String borderColor(COLOR color) {
        if (color == null) return "#666666";
        return switch (color) {
            case RED    -> "#cc3333";
            case BLUE   -> "#2255cc";
            case YELLOW -> "#bb9900";
            case WHITE  -> "#888888";
            case PURPLE -> "#8822cc";
        };
    }

    private String cardBackground(COLOR color) {
        if (color == null) return "#f5f5f5";
        return switch (color) {
            case RED    -> "#fdf0f0";
            case BLUE   -> "#f0f3fd";
            case YELLOW -> "#fdfbf0";
            case WHITE  -> "#f8f8f8";
            case PURPLE -> "#f7f0fd";
        };
    }

    private Label makeLabel(String text, double size, boolean bold, String color) {
        Label lbl = new Label(text);
        lbl.setStyle(
            "-fx-font-size: " + size + "px;" +
            (bold ? "-fx-font-weight: bold;" : "") +
            "-fx-text-fill: " + color + ";"
        );
        return lbl;
    }
}
