package it.polimi.ingsw.am25.client.GUI.Controllers;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for PlayerStatus.fxml.
 *
 * <p>Builds one visual card per player, mirroring the information shown by
 * {@link it.polimi.ingsw.am25.client.TUI.PlayerStatusTUI}:
 * food, prestige points, building discount, shaman stars, and a full
 * tribe breakdown (Hunters, Gatherers, Artists, Shamans, Builders,
 * Inventors, Buildings).
 */
public class PlayerStatusController {

    @FXML private VBox playersVBox;

    private ClientVirtualView clientHandler;
    private PlayerDTO myPlayer;

    // =========================================================
    // Constructor (used by MarketController via loader.setController)
    // =========================================================

    /**
     * No-arg constructor required by Scene Builder and FXMLLoader.
     * Dependencies are injected via {@link #init(ClientVirtualView, PlayerDTO)}
     * before {@link #initialize()} runs.
     */
    public PlayerStatusController() {}

    /**
     * Injects runtime dependencies. Must be called before the FXML is loaded.
     *
     * @param clientHandler the source of live player data.
     * @param myPlayer      the local player DTO (used to mark "TU").
     */
    public void init(ClientVirtualView clientHandler, PlayerDTO myPlayer) {
        this.clientHandler = clientHandler;
        this.myPlayer = myPlayer;
    }

    // =========================================================
    // FXML lifecycle
    // =========================================================

    /** Builds the player cards after the FXML nodes are ready. */
    @FXML
    public void initialize() {
        if (clientHandler == null) return;
        List<PlayerDTO> players = clientHandler.getPlayers();
        if (players.isEmpty()) {
            playersVBox.getChildren().add(makeInfoLabel("Nessun dato giocatore disponibile."));
        } else {
            for (PlayerDTO player : players) {
                playersVBox.getChildren().add(buildPlayerCard(player));
            }
        }
    }

    // =========================================================
    // Button handler
    // =========================================================

    @FXML
    private void handleClose() {
        Stage stage = (Stage) playersVBox.getScene().getWindow();
        stage.close();
    }

    // =========================================================
    // Player card builder
    // =========================================================

    /** Returns a fully populated VBox representing one player's status card. */
    private VBox buildPlayerCard(PlayerDTO player) {
        boolean isMe           = myPlayer != null && myPlayer.getNickName().equals(player.getNickName());
        boolean disconnected   = clientHandler.isPlayerDisconnected(player.getNickName());
        List<CardDTO> tribe    = player.getCardDtoList();

        // Outer card container
        VBox card = new VBox(8);
        card.setStyle(
            "-fx-border-color: " + borderColor(player.getColorTotem()) + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 8;" +
            "-fx-background-color: " + cardBackground(player.getColorTotem()) + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0;"
        );

        card.getChildren().add(buildHeader(player, isMe, disconnected));
        card.getChildren().add(buildStatsRow(player, tribe));
        card.getChildren().add(buildTribeSection(tribe));

        return card;
    }

    // ── Header ───────────────────────────────────────────────

    private HBox buildHeader(PlayerDTO player, boolean isMe, boolean disconnected) {
        HBox header = new HBox(10);
        header.setStyle(
            "-fx-padding: 8 12 8 12;" +
            "-fx-background-color: " + borderColor(player.getColorTotem()) + ";" +
            "-fx-background-radius: 5 5 0 0;"
        );

        Label name = makeLabel(player.getNickName(), 16, true, "white");
        Label color = makeLabel("[" + colorName(player.getColorTotem()) + "]", 14, false, "white");
        Label status = makeLabel(disconnected ? " [DISCONNESSO]" : " [ONLINE]", 14, false,
                disconnected ? "#ffcc44" : "#ccffcc");

        header.getChildren().addAll(name, color, status);
        if (isMe) header.getChildren().add(makeLabel("  << TU >>", 14, true, "#ffee88"));

        return header;
    }

    // ── Stats row ────────────────────────────────────────────

    private HBox buildStatsRow(PlayerDTO player, List<CardDTO> tribe) {
        HBox row = new HBox(30);
        row.setStyle("-fx-padding: 8 12 4 12;");

        row.getChildren().addAll(
            statBox("Cibo",             String.valueOf(player.getFood())),
            statBox("Punti Prestigio",  String.valueOf(player.getPrestigePoint())),
            statBox("Sconto Edifici",   computeBuildingDiscount(tribe) + " cibo"),
            statBox("Stelle Sciamano",  String.valueOf(computeShamanStars(tribe)))
        );
        return row;
    }

    private VBox statBox(String label, String value) {
        VBox box = new VBox(2);
        box.getChildren().add(makeLabel(label, 11, false, "#555555"));
        box.getChildren().add(makeLabel(value,  14, true,  "#111111"));
        return box;
    }

    // ── Tribe section ─────────────────────────────────────────

    private VBox buildTribeSection(List<CardDTO> tribe) {
        VBox section = new VBox(4);
        section.setStyle("-fx-padding: 4 12 10 12;");

        int total = tribe == null ? 0 : tribe.size();
        section.getChildren().add(makeLabel("TRIBU' (" + total + " carte):", 13, true, "#333333"));

        if (tribe == null || tribe.isEmpty()) {
            section.getChildren().add(makeLabel("  (nessuna carta)", 12, false, "#888888"));
            return section;
        }

        addHunterRow(section, tribe);
        addGathererRow(section, tribe);
        addArtistRow(section, tribe);
        addShamanRow(section, tribe);
        addBuilderRow(section, tribe);
        addInventorRow(section, tribe);
        addBuildingRows(section, tribe);

        return section;
    }

    // ── Tribe type rows ───────────────────────────────────────

    private void addHunterRow(VBox section, List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.HUNTER);
        if (list.isEmpty()) return;
        long withIcon    = list.stream().filter(CardDTO::isHasIcon).count();
        long withoutIcon = list.size() - withIcon;
        section.getChildren().add(tribeRow(
            "Cacciatori: " + list.size(),
            "(con icona: " + withIcon + " | senza icona: " + withoutIcon + ")"
        ));
    }

    private void addGathererRow(VBox section, List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.GATHERER);
        if (!list.isEmpty()) section.getChildren().add(tribeRow("Raccoglitori: " + list.size(), ""));
    }

    private void addArtistRow(VBox section, List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.ARTIST);
        if (!list.isEmpty()) section.getChildren().add(tribeRow("Artisti: " + list.size(), ""));
    }

    private void addShamanRow(VBox section, List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.SHAMAN);
        if (list.isEmpty()) return;
        String detail = list.stream()
                .map(c -> shamanStarLabel(c.getStarNumber()))
                .collect(Collectors.joining("  "));
        section.getChildren().add(tribeRow("Sciamani: " + list.size(), "[" + detail + "]"));
    }

    private void addBuilderRow(VBox section, List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.BUILDER);
        if (list.isEmpty()) return;
        String detail = list.stream()
                .map(c -> "sc." + c.getFoodDiscount() + " PP" + c.getFinalPrestigePoint())
                .collect(Collectors.joining(" | "));
        section.getChildren().add(tribeRow("Costruttori: " + list.size(), "[" + detail + "]"));
    }

    private void addInventorRow(VBox section, List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.INVENTOR);
        if (list.isEmpty()) return;
        String icons = list.stream()
                .map(c -> c.getInvIcon() == null ? "?" : iconName(c.getInvIcon()))
                .collect(Collectors.joining("  "));
        section.getChildren().add(tribeRow("Inventori: " + list.size(), icons));
    }

    private void addBuildingRows(VBox section, List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.BUILDING);
        if (list.isEmpty()) return;
        section.getChildren().add(tribeRow("Edifici: " + list.size(), ""));
        for (CardDTO card : list) {
            section.getChildren().add(makeLabel("     > " + card, 11, false, "#444444"));
        }
    }

    /** Returns a single-line HBox with a bold left label and a muted right detail. */
    private HBox tribeRow(String left, String right) {
        HBox row = new HBox(8);
        row.getChildren().add(makeLabel(left, 12, true,  "#222222"));
        if (!right.isEmpty()) row.getChildren().add(makeLabel(right, 12, false, "#555555"));
        return row;
    }

    // =========================================================
    // Computed stats (mirrors PlayerStatusTUI logic)
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
        return switch (star) {
            case ONE   -> 1;
            case TWO   -> 2;
            case THREE -> 3;
        };
    }

    private String shamanStarLabel(SHAMAN_STAR star) {
        if (star == null) return "?";
        return switch (star) {
            case ONE   -> "1*";
            case TWO   -> "2**";
            case THREE -> "3***";
        };
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

    /** CSS border/header color for a totem color. */
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

    /** Light background fill for the player card body. */
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

    // =========================================================
    // Label factory
    // =========================================================

    private Label makeLabel(String text, double size, boolean bold, String color) {
        Label lbl = new Label(text);
        lbl.setStyle(
            "-fx-font-size: " + size + "px;" +
            (bold ? "-fx-font-weight: bold;" : "") +
            "-fx-text-fill: " + color + ";"
        );
        return lbl;
    }

    private Label makeInfoLabel(String text) {
        return makeLabel(text, 14, false, "#555555");
    }
}
