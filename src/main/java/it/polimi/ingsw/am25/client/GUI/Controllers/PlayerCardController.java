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
 * <p>After FXMLLoader creates this controller, call
 * {@link #populate(PlayerDTO, boolean, boolean)} to inject player data.
 * Totem color is applied by adding a CSS class (e.g. {@code totem-red})
 * to {@code cardRoot}; all other styles come from PlayerCard.css.
 */
public class PlayerCardController {

    @FXML private VBox  cardRoot;
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
     *
     * @param player       the player to display.
     * @param isMe         true if this player is the local player.
     * @param disconnected true if this player is currently disconnected.
     */
    public void populate(PlayerDTO player, boolean isMe, boolean disconnected) {
        List<CardDTO> tribe = player.getCardDtoList();

        cardRoot.getStyleClass().add(totemStyleClass(player.getColorTotem()));

        nameLabel.setText(player.getNickName());
        colorLabel.setText("[" + colorName(player.getColorTotem()) + "]");

        if (disconnected) {
            statusLabel.setText(" [DISCONNESSO]");
            statusLabel.getStyleClass().remove("player-status-online");
            statusLabel.getStyleClass().add("player-status-disconnected");
        } else {
            statusLabel.setText(" [ONLINE]");
        }

        meLabel.setVisible(isMe);
        meLabel.setManaged(isMe);

        foodLabel.setText(String.valueOf(player.getFood()));
        ppLabel.setText(String.valueOf(player.getPrestigePoint()));
        discountLabel.setText(computeBuildingDiscount(tribe) + " cibo");
        shamanLabel.setText(String.valueOf(computeShamanStars(tribe)));

        int total = tribe == null ? 0 : tribe.size();
        tribeTitleLabel.setText("TRIBU' (" + total + " carte):");

        if (tribe == null || tribe.isEmpty()) {
            tribeSection.getChildren().add(styledLabel("  (nessuna carta)", "tribe-empty"));
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
            tribeSection.getChildren().add(styledLabel("     > " + card, "tribe-building-entry"));
    }

    private HBox tribeRow(String left, String right) {
        HBox row = new HBox(8);
        row.getChildren().add(styledLabel(left, "tribe-row-main"));
        if (!right.isEmpty()) row.getChildren().add(styledLabel(right, "tribe-row-detail"));
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

    /** Returns the CSS style class name corresponding to the given totem color. */
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

    /** Creates a Label with the given CSS style class applied. */
    private Label styledLabel(String text, String styleClass) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add(styleClass);
        return lbl;
    }
}
