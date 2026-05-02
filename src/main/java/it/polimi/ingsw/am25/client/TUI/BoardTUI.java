package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.webLayer.DTOs.DefaultTileDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.OffertileDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.util.List;
import java.util.Map;

/**
 * Handles all board display logic: offer tiles (with occupant and available actions)
 * and default tiles.
 */
public class BoardTUI {

    private final ClientVirtualView clientHandler;
    private final TUIUtils utils;

    /**
     * Creates a new BoardTUI instance.
     * @param clientHandler the client's virtual view.
     * @param utils         the shared TUI utilities.
     */
    public BoardTUI(ClientVirtualView clientHandler, TUIUtils utils) {
        this.clientHandler = clientHandler;
        this.utils = utils;
    }

    /**
     * Prints the current board state: offer tiles with occupant and draw actions,
     * followed by default tiles with their food-per-slot value.
     */
    public void printBoard() {
        System.out.println("\n=============================================================");
        System.out.println("                        IL BOARD                             ");
        System.out.println("=============================================================");

        List<OffertileDTO> offerTiles = clientHandler.getOfferTileList();
        Map<Integer, String> occupants = clientHandler.getOfferTileOccupants();

        System.out.println("\n▶ CASELLE OFFERTA:");
        System.out.printf("   %-5s | %-25s | %s\n", "Pos", "Giocatore", "Azioni");
        System.out.println("   " + "-".repeat(55));
        for (int i = 0; i < offerTiles.size(); i++) {
            OffertileDTO tile = offerTiles.get(i);
            String nick = occupants.getOrDefault(i, null);
            if (nick != null) {
                COLOR color = bindColorToPlayer(nick);
                String ansi = utils.getAnsiColor(color);
                if (nick.length() > 25) {
                    nick = nick.substring(0, 22) + "...";
                }
                nick = String.format("%-25s", nick);
                System.out.printf("   [%d]   | %-25s | pesca: %d top, %d bot\n",
                        (i + 1), ansi + nick + TUIUtils.RESET, tile.getDrawTop(), tile.getDrawBot());
            } else {
                System.out.printf("   [%d]   | %-25s | pesca: %d top, %d bot\n",
                        (i + 1), "[libera]", tile.getDrawTop(), tile.getDrawBot());
            }
        }

        List<DefaultTileDTO> defaultTiles = clientHandler.getDefaultTileList();
        List<PlayerDTO> defaultOrder = clientHandler.getDefaultTileOrder();

        System.out.println("\n▶ CASELLE DEFAULT:");
        System.out.printf("   %-5s | %-6s | %-25s\n", "Pos", "Cibo", "Giocatore");
        System.out.println("   " + "-".repeat(45));
        for (int i = 0; i < defaultTiles.size(); i++) {
            if (i < defaultOrder.size() && defaultOrder.get(i) != null) {
                PlayerDTO player = defaultOrder.get(i);
                COLOR color = bindColorToPlayer(player.getNickName());
                String ansi = utils.getAnsiColor(color);
                String nick = player.getNickName();
                if (nick.length() > 25) {
                    nick = nick.substring(0, 22) + "...";
                }
                nick = String.format("%-25s", nick);
                System.out.printf("   [%d]   | %-6d | %-25s\n",
                        (i + 1), defaultTiles.get(i).getFoodPerSlotPosition(), ansi + nick + TUIUtils.RESET);
            } else {
                System.out.printf("   [%d]   | %-6d | %-25s\n",
                        (i + 1), defaultTiles.get(i).getFoodPerSlotPosition(), "[libera]");
            }
        }

        System.out.println("=============================================================\n");
    }

    private COLOR bindColorToPlayer(String nickname) {
        return clientHandler.getPlayers().stream()
                .filter(p -> p.getNickName().equals(nickname))
                .map(PlayerDTO::getColorTotem)
                .findFirst()
                .orElse(null);
    }
}
