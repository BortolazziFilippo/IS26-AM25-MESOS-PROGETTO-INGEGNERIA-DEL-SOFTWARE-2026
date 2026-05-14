package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;

public class SolvingEventsTUI {
    private final ClientVirtualView clientVirtualView;
    private final TUIUtils tuiUtils;

    /**
     * Creates the TUI screen for the event-resolution phase.
     *
     * @param clientVirtualView the local client view holding the game state.
     * @param tUIUtils          the shared TUI utilities for screen management.
     */
    public SolvingEventsTUI(ClientVirtualView clientVirtualView, TUIUtils tUIUtils) {
        this.clientVirtualView = clientVirtualView;
        this.tuiUtils = tUIUtils;
    }

    /**
     * Handles the event-resolution phase on the terminal.
     * Blocks the current thread until the {@code SOLVING_EVENTS} phase ends,
     * printing resolved events in real time as they arrive. When done,
     * displays the complete list and clears it for the next phase.
     */
    public void solveEvents() {
        // NB: NON chiamare clearResolvedEvents() qui all'inizio.
        // Tra la sveglia su turnLock e l'arrivo della TUI a questa riga, il
        // server (single-thread executor) puo' aver gia' inviato tutti gli
        // eventResolved della fase: cancellandoli ora li perderemmo.

        tuiUtils.clearScreen();
        System.out.println("⚙️  --- RISOLUZIONE EVENTI ---");
        System.out.println("Gli eventi dell'era sono in corso di risoluzione...");
        System.out.println("⏳ Attendi la fine della risoluzione...");

        synchronized (clientVirtualView.turnLock) {
            while (clientVirtualView.getGamePhase() == GAME_PHASE.SOLVING_EVENTS) {
                System.out.println("Eventi risolti finora: ");
                clientVirtualView.getResolvedEvents().forEach(event -> System.out.println(event));
                System.out.println();
                System.out.println(" ⏳ Risoluzione in corso... ");
                try {
                    clientVirtualView.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // Stampa finale: copre due casi che il while da solo perde
        //  1) la fase e' gia' cambiata quando arriviamo qui (loop mai eseguito)
        //  2) un eventResolved e' arrivato insieme al cambio di fase
        //     (la wait esce ma non rifa' la stampa prima del controllo while)
        tuiUtils.clearScreen();
        System.out.println("✅ Gli eventi sono stati risolti:");
        clientVirtualView.getResolvedEvents().forEach(event -> System.out.println(event));

        // Puliamo ORA, dopo che l'utente ha avuto modo di vederli, cosi'
        // la prossima fase SOLVING_EVENTS partira' da una lista vuota.
        clientVirtualView.clearResolvedEvents();

        tuiUtils.pauseAndClear();
    }
}
