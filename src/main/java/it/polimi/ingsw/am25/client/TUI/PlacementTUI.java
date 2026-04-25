package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

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
            System.out.print("\n📍 Inserisci la casella in cui piazzare il Totem (1 a "
                    + clientHandler.getOfferTileSize() + ") oppure 'q' per annullare: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("q")) return -1;

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
}
