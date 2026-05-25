package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * Handles all market interactions: drawing cards from the top or bottom of the
 * market, passing the turn, picking an extra card from a special effect, and
 * printing the current market state.
 */
public class MarketTUI {

    private final ServerRemoteInterface serverStub;
    private final ClientVirtualView clientHandler;
    private final Scanner scanner;
    private final TUIUtils utils;
    private final PlayerDTO myPlayer;
    private final BoardTUI boardTUI;

    /**
     * Creates a new MarketTUI instance.
     *
     * @param serverStub    the remote server interface.
     * @param clientHandler the client's virtual view.
     * @param scanner       the shared input scanner.
     * @param utils         the shared TUI utilities.
     * @param myPlayer      the local player DTO.
     */
    public MarketTUI(ServerRemoteInterface serverStub, ClientVirtualView clientHandler, Scanner scanner, TUIUtils utils, PlayerDTO myPlayer) {
        this.serverStub = serverStub;
        this.clientHandler = clientHandler;
        this.scanner = scanner;
        this.utils = utils;
        this.myPlayer = myPlayer;
        this.boardTUI = new BoardTUI(clientHandler, utils);
    }

    /**
     * Asks the player whether to draw a tribe or building card from the top,
     * then delegates to the appropriate method.
     */
    public void drawTopCard() {
        drawCard(true);
    }

    /**
     * Asks the player whether to draw a tribe or building card from the bottom,
     * then delegates to the appropriate method.
     */
    public void drawBottomCard() {
        drawCard(false);
    }

    // ==========================================================
    // DRAW FROM TOP / BOTTOM  (public API — thin wrappers)
    // ==========================================================

    /**
     * Shows the "draw from top/bottom" menu and delegates the actual draw
     * to {@link #drawCardFromMarket}.
     *
     * @param isTop {@code true} for the top row, {@code false} for the bottom row.
     */
    private void drawCard(boolean isTop) {
        String layer = isTop ? "SOPRA" : "SOTTO";
        String header = "--- PESCA CARTA (" + layer + ") ---";

        // Precompute all variant parameters once so the loop body stays clean.
        CardGetter tribeCards = isTop ? clientHandler::getTopCards : clientHandler::getBottomCards;
        CardGetter buildingCards = isTop ? clientHandler::getTopBuildings : clientHandler::getBottomBuildings;
        Supplier<Integer> tribeSize = isTop ? clientHandler::getTopCardSize : clientHandler::getBottomCardSize;
        Supplier<Integer> buildingSize = isTop ? clientHandler::getTopBuildingSize : clientHandler::getBottomBuildingSize;
        ServerDrawCall tribeCall = isTop ? (p, pos) -> serverStub.selectCardFromTopList(p, CARD_TYPE.ARTIST, pos) : (p, pos) -> serverStub.selectCardFromBottomList(p, CARD_TYPE.ARTIST, pos);
        ServerDrawCall buildingCall = isTop ? (p, pos) -> serverStub.selectCardFromTopList(p, CARD_TYPE.BUILDING, pos) : (p, pos) -> serverStub.selectCardFromBottomList(p, CARD_TYPE.BUILDING, pos);

        utils.clearScreen();
        System.out.println(header);

        boolean isDrawn = false;
        while (!isDrawn) {
            System.out.println("\nCosa vuoi pescare?");
            System.out.println("[1] - Carta Tribù");
            System.out.println("[2] - Carta Edificio");
            System.out.println("[Q] - Annulla e torna al menu principale");
            printCardList("CARTE TRIBÙ DISPONIBILI (" + layer + ")", tribeCards.get());
            printCardList("CARTE EDIFICIO DISPONIBILI (" + layer + ")", buildingCards.get());
            System.out.print("\nScelta: ");

            String input = scanner.nextLine();
            if (clientHandler.isServerDead()) return;
            if (input.equalsIgnoreCase("q")) {
                System.out.println("\nAzione annullata.");
                utils.pauseAndClear();
                return;
            } else if (input.equalsIgnoreCase("b")) {
                utils.clearScreen();
                boardTUI.printBoard();
                utils.pauseAndClear();
                utils.clearScreen();
                System.out.println(header);
            } else if (input.equals("1")) {
                isDrawn = drawCardFromMarket("Tribù", layer, tribeCards, tribeSize, tribeCall);
            } else if (input.equals("2")) {
                isDrawn = drawCardFromMarket("Edificio", layer, buildingCards, buildingSize, buildingCall);
            } else {
                System.err.println("\n❌ Errore: Inserisci 1 o 2.");
                utils.pauseAndClear();
                utils.clearScreen();
                System.out.println(header);
            }
        }
    }

    /**
     * Core draw loop: shows the card list, reads the player's position choice,
     * calls the server, and waits for confirmation.
     *
     * @param displayName human-readable card type name ("Tribù" or "Edificio").
     * @param layer       "SOPRA" or "SOTTO".
     * @param cards       supplier of the card list to display.
     * @param size        supplier of the current deck size.
     * @param serverCall  the server action to invoke with the chosen position.
     * @return {@code true} if a card was successfully drawn, {@code false} to go back.
     */
    private boolean drawCardFromMarket(String displayName, String layer, CardGetter cards, Supplier<Integer> size, ServerDrawCall serverCall) {

        String header = "--- PESCA CARTA " + displayName.toUpperCase(Locale.ROOT) + " (" + layer + ") ---";
        String backHeader = "--- PESCA CARTA (" + layer + ") ---";
        String listTitle = "CARTE " + displayName.toUpperCase(Locale.ROOT) + " DISPONIBILI (" + layer + ")";

        while (true) {
            clientHandler.connectionError = false;
            utils.clearScreen();
            System.out.println(header);
            printCardList(listTitle, cards.get());

            if (size.get() == 0) {
                System.out.println("\nNon ci sono carte " + displayName + " selezionabili.");
                utils.pauseAndClear();
                return false;
            }

            System.out.print("\nInserisci la posizione della carta (1 a " + size.get() + ") oppure 'q' per tornare indietro, 'b' per board: ");
            String input = scanner.nextLine();
            if (clientHandler.isServerDead()) return false;

            if (input.equalsIgnoreCase("q")) {
                utils.clearScreen();
                System.out.println(backHeader);
                return false;
            }

            if (input.equalsIgnoreCase("b")) {
                utils.clearScreen();
                boardTUI.printBoard();
                utils.pauseAndClear();
                continue;
            }

            int prevTop = clientHandler.getDrawTop();
            int prevBot = clientHandler.getDrawBot();

            try {
                int position = Integer.parseInt(input) - 1;
                if (position < 0 || position >= size.get()) {
                    System.err.println("\n❌ Errore: Posizione fuori limite.");
                    utils.pauseAndClear();
                    continue;
                }
                serverCall.call(myPlayer, position);
            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Inserisci un NUMERO valido.");
                utils.pauseAndClear();
                continue;
            } catch (Exception e) {
                System.err.println("\n❌ Impossibile pescare: " + utils.extractCleanError(e));
                utils.pauseAndClear();
                continue;
            }

            System.out.println("\nIn attesa di conferma dal server...");

            if (waitForActionChange(prevTop, prevBot)) return false;

            System.out.println("\n✅ Carta " + displayName + " pescata con successo o turno concluso!");
            utils.pauseAndClear();
            return true;
        }
    }

    // ==========================================================
    // SHARED DRAW MENU
    // ==========================================================

    /**
     * Sends a "do nothing" action to the server to pass the current turn.
     */
    public void passTurn() {
        clientHandler.connectionError = false;
        utils.clearScreen();
        System.out.println("--- PASSA TURNO ---");

        try {
            serverStub.playerDoNothing(myPlayer);
        } catch (Exception e) {
            System.err.println("\n❌ Impossibile passare il turno: " + utils.extractCleanError(e));
            utils.pauseAndClear();
            return;
        }

        System.out.println("\nIn attesa di conferma dal server...");

        synchronized (clientHandler.turnLock) {
            while (!clientHandler.connectionError && (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) && clientHandler.getPlayerToPlay() != null && clientHandler.getPlayerToPlay().equals(myPlayer.getNickName())) {
                try {
                    clientHandler.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (clientHandler.connectionError) {
            System.err.println("\n❌ Impossibile passare il turno.");
            utils.pauseAndClear();
        } else {
            System.out.println("\n✅ Turno terminato.");
            // Do not call pauseAndClear() here: blocking the thread on scanner.nextLine()
            // would cause missed turnLock.notifyAll() calls from SOLVING_EVENTS
            // (eventResolved, gamePhaseChanged). Returning immediately to waitForMyTurn()
            // puts the thread back into turnLock.wait() and handles incoming notifications correctly.
        }
    }

    /**
     * Handles the "Draw One More Card" special building effect.
     * The player picks one extra card from the top of any deck.
     */
    public void handleExtraDraw() {
        utils.clearScreen();
        System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");

        boolean isDrawn = false;
        while (!isDrawn) {
            System.out.println("\nScegli da quale mazzo in CIMA vuoi pescare la carta extra:");
            System.out.println("[1] - Carta Tribù");
            System.out.println("[2] - Carta Edificio");
            System.out.println("[0] - Salta (non pescare)");
            System.out.print("Scelta: ");
            String scelta = scanner.nextLine();
            if (clientHandler.isServerDead()) return;

            if (scelta.equals("0")) {
                try {
                    serverStub.skipExtraDraw(myPlayer);
                    clientHandler.needsExtraDraw = false;
                    return;
                } catch (Exception e) {
                    System.err.println("\n❌ Non puoi saltare: " + utils.extractCleanError(e));
                    utils.pauseAndClear();
                    utils.clearScreen();
                    System.out.println(" EFFETTO ATTIVATO: Draw One More Card! ");
                    continue;
                }
            }

            int maxLimit;
            if (scelta.equals("1")) {
                printCardList("CARTE TRIBÙ DISPONIBILI", clientHandler.getExtraDrawCards());
                maxLimit = clientHandler.getExtraDrawCardSize();
            } else if (scelta.equals("2")) {
                printCardList("CARTE EDIFICIO DISPONIBILI", clientHandler.getExtraDrawBuildings());
                maxLimit = clientHandler.getExtraDrawBuildingSize();
            } else {
                System.err.println("\n❌ Scelta mazzo non valida. Digita 0, 1 o 2.");
                utils.pauseAndClear();
                utils.clearScreen();
                System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
                continue;
            }

            System.out.print("\nInserisci la posizione della carta (1 a " + maxLimit + ") oppure 'q' per cambiare mazzo: ");
            String inputPos = scanner.nextLine();
            if (clientHandler.isServerDead()) return;

            if (inputPos.equalsIgnoreCase("q")) {
                utils.clearScreen();
                System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
                continue;
            }

            try {
                int position = Integer.parseInt(inputPos) - 1;
                if (position < 0 || position >= maxLimit) {
                    System.err.println("\n❌ Errore: Posizione fuori limite.");
                    utils.pauseAndClear();
                    utils.clearScreen();
                    System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
                    continue;
                }

                clientHandler.connectionError = false;
                if (scelta.equals("1")) {
                    serverStub.selectExtraCard(myPlayer, CARD_TYPE.ARTIST, position);
                } else {
                    serverStub.selectExtraCard(myPlayer, CARD_TYPE.BUILDING, position);
                }

                System.out.println("\n✅ Carta extra pescata con successo!");
                utils.pauseAndClear();
                isDrawn = true;

            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Inserisci un NUMERO valido.");
                utils.pauseAndClear();
                utils.clearScreen();
                System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
            } catch (Exception e) {
                System.err.println("\n❌ Impossibile pescare: " + utils.extractCleanError(e));
                utils.pauseAndClear();
                utils.clearScreen();
                System.out.println("✨ EFFETTO ATTIVATO: Draw One More Card! ✨");
            }
        }

        clientHandler.needsExtraDraw = false;
    }

    // ==========================================================
    // PASS TURN
    // ==========================================================

    /**
     * Prints the full market state (top and bottom rows for both tribes and buildings).
     */
    public void printMarket() {
        System.out.println("\n=============================================================");
        System.out.println("                       🏪 IL MERCATO 🏪                      ");
        System.out.println("=============================================================");

        printCardList("CARTE NORMALI (SOPRA)", clientHandler.getTopCards());
        printCardList("CARTE EDIFICIO (SOPRA)", clientHandler.getTopBuildings());

        System.out.println("-------------------------------------------------------------");

        printCardList("CARTE NORMALI (SOTTO)", clientHandler.getBottomCards());
        printCardList("CARTE EDIFICIO (SOTTO)", clientHandler.getBottomBuildings());

        System.out.println("=============================================================\n");
    }

    // ==========================================================
    // EXTRA DRAW (special building effect)
    // ==========================================================

    /**
     * Prints a formatted table for a list of cards.
     *
     * @param title the label to display above the table.
     * @param cards the list of cards to show.
     */
    private void printCardList(String title, List<? extends CardDTO> cards) {
        System.out.println("\n▶ " + title + ":");

        if (cards == null || cards.isEmpty()) {
            System.out.println("   [Nessuna carta disponibile in questa fila]");
            return;
        }

        System.out.printf("   %-4s | %-15s | %s\n", "Pos", "Tipo Carta", "Descrizione");
        System.out.println("   " + "-".repeat(90));

        for (int i = 0; i < cards.size(); i++) {
            CardDTO card = cards.get(i);
            System.out.printf("   [%d]  | %-15s | %s\n", (i + 1), card.getCardType(), card);
        }
    }

    // ==========================================================
    // MARKET DISPLAY
    // ==========================================================

    /**
     * Blocks until the server acknowledges the action (action points change,
     * phase changes, or connection error).
     *
     * <p>Phase 1: wait for the draw counter to change from its pre-action value
     * (normal case: server decremented it after accepting the card selection).
     *
     * <p>Phase 2: if both draw counters are now 0 and it is still this player's turn,
     * the server has already called {@code advanceTurnOrRound()} and is about to
     * deliver a {@code playerToPlayChanged} notification. We wait for that notification
     * before returning so the main game loop never re-enters the action menu in the
     * brief transient window before the turn-change notification arrives.
     *
     * @param prevTop the previous draw-top count before the action.
     * @param prevBot the previous draw-bot count before the action.
     * @return {@code true} if a connection error occurred.
     */
    private boolean waitForActionChange(int prevTop, int prevBot) {
        // --- Phase 1: wait for draw-counter change or any other exit condition ---
        synchronized (clientHandler.turnLock) {
            while (!clientHandler.connectionError && clientHandler.getDrawTop() == prevTop && clientHandler.getDrawBot() == prevBot && (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION) && clientHandler.getPlayerToPlay() != null && clientHandler.getPlayerToPlay().equals(myPlayer.getNickName())) {
                try {
                    clientHandler.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return true;
                }
            }
        }

        if (clientHandler.connectionError) {
            System.err.println("\n❌ Impossibile pescare.");
            utils.pauseAndClear();
            return true;
        }

        // --- Phase 2: if both counters are now 0 and the turn hasn't formally changed
        //     yet, wait for the playerToPlayChanged notification so the main loop
        //     doesn't re-enter the draw menu in a stale state. ---
        if (clientHandler.getDrawTop() == 0 && clientHandler.getDrawBot() == 0) {
            synchronized (clientHandler.turnLock) {
                while (!clientHandler.connectionError && clientHandler.getPlayerToPlay() != null && clientHandler.getPlayerToPlay().equals(myPlayer.getNickName()) && (clientHandler.getGamePhase() == GAME_PHASE.RESOLVE_ACTION || clientHandler.getGamePhase() == GAME_PHASE.LAST_ROUND_RESOLVE_ACTION)) {
                    try {
                        clientHandler.turnLock.wait(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Supplies a snapshot of a specific card list from the market
     * (e.g. top tribes, bottom buildings).
     * Used to pass the correct {@code clientHandler} getter as a parameter
     * without duplicating the draw loop for each combination.
     */
    @FunctionalInterface
    private interface CardGetter {
        List<? extends CardDTO> get();
    }

    // ==========================================================
    // PRIVATE HELPERS
    // ==========================================================

    /**
     * Represents a server call that draws a card at a given position.
     * Implementations wrap either {@code selectCardFromTopList} or
     * {@code selectCardFromBottomList}, with the {@link CARD_TYPE} already
     * baked in, so {@link #drawCardFromMarket} can invoke the correct
     * server method without knowing which one it is.
     */
    @FunctionalInterface
    private interface ServerDrawCall {
        void call(PlayerDTO player, int position) throws Exception;
    }
}
