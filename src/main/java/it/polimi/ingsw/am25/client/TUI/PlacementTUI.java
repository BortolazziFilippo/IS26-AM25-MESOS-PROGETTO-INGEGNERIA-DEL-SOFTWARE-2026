package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.util.Map;
import java.util.Scanner;

/**
 * Handles the placement phase of the game: asking the player where to place
 * their totem on the board and confirming the move with the server.
 */
public class PlacementTUI {

    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final Scanner scanner;
    private final TUIUtils utils;
    private final PlayerDTO myPlayer;
    private final PlayerStatusTUI playerStatusTUI;
    private final MarketTUI marketTUI;

    /**
     * Creates a new PlacementTUI instance.
     * @param serverStub    the remote server interface.
     * @param clientHandler the client's virtual view.
     * @param scanner       the shared input scanner.
     * @param utils         the shared TUI utilities.
     * @param myPlayer      the local player DTO.
     */
    public PlacementTUI(ServerRemoteInterface serverStub, ClientVirtualView clientHandler,
                        Scanner scanner, TUIUtils utils, PlayerDTO myPlayer) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.scanner = scanner;
        this.utils = utils;
        this.myPlayer = myPlayer;
        this.playerStatusTUI = new PlayerStatusTUI(clientHandler, scanner, utils, myPlayer);
        this.marketTUI = new MarketTUI(serverStub, clientHandler, scanner, utils, myPlayer);
    }

    /**
     * Prompts the player to choose a tile position and sends the placement
     * request to the server. Retries on error until the move is accepted.
     */
    public void placePlayer() {
        utils.clearScreen();
        System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
        boolean isPlaced = false;

        while (!isPlaced) {
            clientHandler.connectionError = false;
            int position = getPlacingIndex();
            if (position == -1) return; // user pressed 'q'

            try {
                serverStub.placingPlayer(myPlayer, position);
            } catch (Exception e) {
                System.err.println("\n❌ Errore RMI: " + utils.extractCleanError(e));
                utils.pauseAndClear();
                utils.clearScreen();
                System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
                continue;
            }

            System.out.println("\nIn attesa di conferma dal server...");

            synchronized (clientHandler.turnLock) {
                while (!clientHandler.connectionError &&
                        (clientHandler.getGamePhase() == GAME_PHASE.PLACING_PHASE ||
                                clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_PLACING_PHASE) &&
                        clientHandler.getPlayerToPlace() != null &&
                        clientHandler.getPlayerToPlace().equals(myPlayer.getNickName())) {
                    try {
                        clientHandler.turnLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            if (clientHandler.connectionError) {
                System.err.println("❌ Casella già occupata");
                utils.pauseAndClear();
                utils.clearScreen();
                System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
            } else {
                System.out.println("\n✅ Totem posizionato con successo nella casella " + (position + 1) + "!");
                utils.pauseAndClear();
                isPlaced = true;
            }
        }
    }

    /**
     * Reads and validates the tile index from user input.
     * @return the 0-based tile index, or -1 if the user cancelled.
     */
    private int getPlacingIndex() {
        while (true) {
            System.out.print("\n📍 Posiziona giocatore: (1-" + clientHandler.getOfferTileSize()
                    + ") \n altre voci: i=giocatori  b=board  M=mercato  q=annulla: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) return -1;

            if (input.equalsIgnoreCase("i")) {
                playerStatusTUI.printAllPlayersStatus();
                utils.clearScreen();
                System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
                continue;
            }

            if (input.equalsIgnoreCase("b")) {
                utils.clearScreen();
                printBoard();
                utils.pauseAndClear();
                System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
                continue;
            }

            if (input.equals("M")) {
                utils.clearScreen();
                marketTUI.printMarket();
                utils.pauseAndClear();
                System.out.println("--- POSIZIONAMENTO GIOCATORE ---");
                continue;
            }

            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < clientHandler.getOfferTileSize()) {
                    return index;
                }
                System.err.println("\n❌ Errore: L'indice deve essere tra 1 e " + clientHandler.getOfferTileSize() + ".");
                utils.pauseAndClear();
            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Devi inserire un NUMERO intero valido.");
                utils.pauseAndClear();
            }
        }
    }

    /**
     * Prints the current board state: offer tiles with the player on each one (if any),
     * followed by default tiles with their food-per-slot-position value.
     */
    private void printBoard() {
        System.out.println("\n=============================================================");
        System.out.println("                        IL BOARD                             ");
        System.out.println("=============================================================");

        var offerTiles = clientHandler.getOfferTileList();
        var occupants = clientHandler.getOfferTileOccupants();

        System.out.println("\n▶ CASELLE OFFERTA:");
        System.out.printf("   %-5s | %-4s | %s\n", "Pos", "ID", "Giocatore");
        System.out.println("   " + "-".repeat(40));
        for (int i = 0; i < offerTiles.size(); i++) {
            String player = occupants.getOrDefault(i, "[libera]");
            System.out.printf("   [%d]   | %-4s | %s\n", (i + 1), offerTiles.get(i).getOfferTileID(), player);
        }

        var defaultTiles = clientHandler.getDefaultTileList();
        System.out.println("\n▶ CASELLE DEFAULT:");
        System.out.printf("   %-5s | %s\n", "Pos", "Cibo per slot");
        System.out.println("   " + "-".repeat(40));
        for (int i = 0; i < defaultTiles.size(); i++) {
            System.out.printf("   [%d]   | %d\n", (i + 1), defaultTiles.get(i).getFoodPerSlotPosition());
        }

        System.out.println("=============================================================\n");
    }
}
