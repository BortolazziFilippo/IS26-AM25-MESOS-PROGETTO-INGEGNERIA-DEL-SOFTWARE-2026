package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Enums.SHAMAN_STAR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Displays the full status of all players when the user presses "i".
 *
 * <p>For each player shows:
 * <ul>
 *   <li>Food, prestige points</li>
 *   <li>Total building food discount, total shaman stars</li>
 *   <li>Full tribe breakdown:
 *     <ul>
 *       <li>Hunters   — total, with-icon count, without-icon count</li>
 *       <li>Inventors — total + list of every invention icon owned</li>
 *       <li>Shamans   — total + star detail (★/★★/★★★)</li>
 *       <li>Builders  — total + cumulative food discount + end-game PP</li>
 *       <li>Gatherers / Artists — totals</li>
 *       <li>Buildings — name / effect of each owned building</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class PlayerStatusTUI {

    private static final int BOX_WIDTH = 68;
    private static final String LINE = "=".repeat(BOX_WIDTH);
    private static final String SEPARATOR = "-".repeat(BOX_WIDTH);

    private final ClientVirtualView clientHandler;
    private final Scanner scanner;
    private final TUIUtils utils;
    private final PlayerDTO myPlayer;

    /**
     * Creates a new PlayerStatusTUI instance.
     *
     * @param clientHandler the client's virtual view (source of player data).
     * @param scanner       the shared input scanner.
     * @param utils         the shared TUI utilities.
     * @param myPlayer      the local player DTO (used to highlight "you").
     */
    public PlayerStatusTUI(ClientVirtualView clientHandler, Scanner scanner,
                           TUIUtils utils, PlayerDTO myPlayer) {
        this.clientHandler = clientHandler;
        this.scanner = scanner;
        this.utils = utils;
        this.myPlayer = myPlayer;
    }

    // ==========================================================
    // PUBLIC API
    // ==========================================================

    /**
     * Clears the screen, prints the status panel for every player,
     * then waits for ENTER before returning to the game loop.
     */
    public void printAllPlayersStatus() {
        utils.clearScreen();
        System.out.println(LINE);
        System.out.println(center("STATO DEI GIOCATORI"));
        System.out.println(LINE);

        List<PlayerDTO> players = clientHandler.getPlayers();
        if (players.isEmpty()) {
            System.out.println("  Nessun dato giocatore disponibile.");
        } else {
            for (PlayerDTO player : players) {
                printPlayerCard(player);
            }
        }

        System.out.println(LINE);
        utils.pauseAndClear();
    }

    // ==========================================================
    // PLAYER CARD
    // ==========================================================

    private void printPlayerCard(PlayerDTO player) {
        boolean isMe = myPlayer != null && myPlayer.getNickName().equals(player.getNickName());
        boolean isDisconnected = clientHandler.isPlayerDisconnected(player.getNickName());
        String colorCode = colorToAnsi(player.getColorTotem());
        String tag = isMe ? "  << TU >>" : "";
        String statusTag = isDisconnected ? "  [DISCONNESSO]" : "  [ONLINE]";

        System.out.println(SEPARATOR);
        System.out.println(colorCode
                + "  Giocatore: " + player.getNickName()
                + "  [" + colorName(player.getColorTotem()) + "]"
                + statusTag
                + tag
                + TUIUtils.RESET);
        System.out.println(SEPARATOR);

        System.out.printf("  %-28s %d%n", "Cibo:", player.getFood());
        System.out.printf("  %-28s %d%n", "Punti Prestigio:", player.getPrestigePoint());

        List<CardDTO> tribe = player.getCardDtoList();

        System.out.printf("  %-28s %d cibo%n", "Sconto Edifici totale:", computeBuildingDiscount(tribe));
        System.out.printf("  %-28s %d%n", "Stelle Sciamano totali:", computeShamanStars(tribe));

        System.out.println(SEPARATOR);
        System.out.println("  TRIBU' (" + (tribe == null ? 0 : tribe.size()) + " carte):");
        System.out.println();

        if (tribe == null || tribe.isEmpty()) {
            System.out.println("     [Nessuna carta in tribu']");
        } else {
            printHunters(tribe);
            printGatherers(tribe);
            printArtists(tribe);
            printShamans(tribe);
            printBuilders(tribe);
            printInventors(tribe);
            printBuildings(tribe);
        }
        System.out.println();
    }

    // ==========================================================
    // TRIBE SECTIONS
    // ==========================================================

    /**
     * Cacciatori: totale + quanti con/senza icona.
     */
    private void printHunters(List<CardDTO> tribe) {
        List<CardDTO> hunters = byType(tribe, CARD_TYPE.HUNTER);
        if (hunters.isEmpty()) return;

        long withIcon = hunters.stream().filter(CardDTO::isHasIcon).count();
        long withoutIcon = hunters.size() - withIcon;

        System.out.printf("  %-15s %2d   (con icona: %d | senza icona: %d)%n",
                "Cacciatori:", hunters.size(), withIcon, withoutIcon);
    }

    /**
     * Raccoglitori: solo totale.
     */
    private void printGatherers(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.GATHERER);
        if (list.isEmpty()) return;
        System.out.printf("  %-15s %2d%n", "Raccoglitori:", list.size());
    }

    /**
     * Artisti: solo totale.
     */
    private void printArtists(List<CardDTO> tribe) {
        List<CardDTO> list = byType(tribe, CARD_TYPE.ARTIST);
        if (list.isEmpty()) return;
        System.out.printf("  %-15s %2d%n", "Artisti:", list.size());
    }

    /**
     * Sciamani: totale + dettaglio stelle per ogni carta
     * (es. "1★  2★  1★★  3★★★").
     */
    private void printShamans(List<CardDTO> tribe) {
        List<CardDTO> shamans = byType(tribe, CARD_TYPE.SHAMAN);
        if (shamans.isEmpty()) return;

        String starDetail = shamans.stream()
                .map(c -> shamanStarLabel(c.getStarNumber()))
                .collect(Collectors.joining("  "));

        System.out.printf("  %-15s %2d   [%s]%n", "Sciamani:", shamans.size(), starDetail);
    }

    /**
     * Costruttori: totale + sconto cibo cumulativo + PP a fine partita
     * (es. "sc.2  PP5 | sc.1  PP3").
     */
    private void printBuilders(List<CardDTO> tribe) {
        List<CardDTO> builders = byType(tribe, CARD_TYPE.BUILDER);
        if (builders.isEmpty()) return;

        String detail = builders.stream()
                .map(c -> "sc." + c.getFoodDiscount() + " PP" + c.getFinalPrestigePoint())
                .collect(Collectors.joining(" | "));

        System.out.printf("  %-15s %2d   [%s]%n", "Costruttori:", builders.size(), detail);
    }

    /**
     * Inventori: totale + lista delle icone possedute (una per carta),
     * raggruppate su righe di max 6 icone.
     */
    private void printInventors(List<CardDTO> tribe) {
        List<CardDTO> inventors = byType(tribe, CARD_TYPE.INVENTOR);
        if (inventors.isEmpty()) return;

        List<INV_ICON> icons = inventors.stream()
                .map(CardDTO::getInvIcon)
                .collect(Collectors.toList());

        System.out.printf("  %-15s %2d%n", "Inventori:", inventors.size());

        // Print icons in rows of 6
        final int ROW_SIZE = 6;
        for (int i = 0; i < icons.size(); i += ROW_SIZE) {
            List<INV_ICON> row = icons.subList(i, Math.min(i + ROW_SIZE, icons.size()));
            String rowStr = row.stream()
                    .map(ic -> ic == null ? "?" : iconName(ic))
                    .collect(Collectors.joining("  "));
            System.out.println("                   " + rowStr);
        }
    }

    /**
     * Edifici: uno per riga con costo, PP fissi ed effetto.
     */
    private void printBuildings(List<CardDTO> tribe) {
        List<CardDTO> buildings = byType(tribe, CARD_TYPE.BUILDING);
        if (buildings.isEmpty()) return;

        System.out.printf("  %-15s %2d%n", "Edifici:", buildings.size());
        for (CardDTO card : buildings) {
            // BuildingDTO overrides toString() with full description
            System.out.println("     > " + card);
        }
    }

    // ==========================================================
    // COMPUTED STATS
    // ==========================================================

    /**
     * Sum of foodDiscount from all Builder cards.
     */
    private int computeBuildingDiscount(List<CardDTO> tribe) {
        if (tribe == null) return 0;
        return tribe.stream()
                .filter(c -> c.getCardType() == CARD_TYPE.BUILDER)
                .mapToInt(CardDTO::getFoodDiscount)
                .sum();
    }

    /**
     * Sum of shaman star values (ONE=1, TWO=2, THREE=3).
     */
    private int computeShamanStars(List<CardDTO> tribe) {
        if (tribe == null) return 0;
        return tribe.stream()
                .filter(c -> c.getCardType() == CARD_TYPE.SHAMAN && c.getStarNumber() != null)
                .mapToInt(c -> shamanStarValue(c.getStarNumber()))
                .sum();
    }

    // ==========================================================
    // SMALL HELPERS
    // ==========================================================

    /**
     * Filters the tribe by card type.
     */
    private List<CardDTO> byType(List<CardDTO> tribe, CARD_TYPE type) {
        return tribe.stream()
                .filter(c -> c.getCardType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Numeric value of a shaman star.
     */
    private int shamanStarValue(SHAMAN_STAR star) {
        switch (star) {
            case ONE:
                return 1;
            case TWO:
                return 2;
            case THREE:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Short display label for a shaman star (e.g. "★", "★★", "★★★").
     */
    private String shamanStarLabel(SHAMAN_STAR star) {
        if (star == null) return "?";
        switch (star) {
            case ONE:
                return "1*";
            case TWO:
                return "2**";
            case THREE:
                return "3***";
            default:
                return star.name();
        }
    }

    /**
     * Italian display name for an invention icon.
     */
    private String iconName(INV_ICON icon) {
        switch (icon) {
            case BREAD:
                return "PANE";
            case STONE:
                return "PIETRA";
            case NECKLACE:
                return "COLLANA";
            case BAIT:
                return "ESCA";
            case GHOST:
                return "SPIRITO";
            case ARROW:
                return "FRECCIA";
            case LEATHER:
                return "CUOIO";
            case ROPE:
                return "CORDA";
            case FLUTE:
                return "FLAUTO";
            case BOWL:
                return "CIOTOLA";
            default:
                return icon.name();
        }
    }

    /**
     * ANSI color code for a totem color.
     */
    private String colorToAnsi(COLOR color) {
        if (color == null) return TUIUtils.RESET;
        switch (color) {
            case RED:
                return TUIUtils.RED;
            case BLUE:
                return TUIUtils.BLUE;
            case YELLOW:
                return TUIUtils.YELLOW;
            case WHITE:
                return TUIUtils.RESET;
            case PURPLE:
                return TUIUtils.PURPLE;
            default:
                return TUIUtils.RESET;
        }
    }

    /**
     * Italian display name for a totem color.
     */
    private String colorName(COLOR color) {
        if (color == null) return "?";
        switch (color) {
            case RED:
                return "ROSSO";
            case BLUE:
                return "BLU";
            case YELLOW:
                return "GIALLO";
            case WHITE:
                return "BIANCO";
            case PURPLE:
                return "VIOLA";
            default:
                return color.name();
        }
    }

    /**
     * Centers {@code text} within {@link #BOX_WIDTH} columns (ignores ANSI codes).
     */
    private String center(String text) {
        int visibleLen = text.replaceAll("\\[[;\\d]*m", "").length();
        int padding = Math.max(0, (BOX_WIDTH - visibleLen) / 2);
        return " ".repeat(padding) + text;
    }
}
