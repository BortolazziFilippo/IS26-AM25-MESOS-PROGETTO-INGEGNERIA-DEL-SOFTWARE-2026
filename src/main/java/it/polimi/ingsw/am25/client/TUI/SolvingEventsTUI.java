package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;

public class SolvingEventsTUI {
    private ClientVirtualView clientVirtualView;
    private TUIUtils tuiUtils;

    public SolvingEventsTUI(ClientVirtualView clientVirtualView, TUIUtils tUIUtils) {
        this.clientVirtualView = clientVirtualView;
        this.tuiUtils = tUIUtils;
    }

    public void solveEvents() {
        clientVirtualView.clearResolvedEvents();

        tuiUtils.clearScreen();
        System.out.println("⚙\uFE0F  --- RISOLUZIONE EVENTI ---");
        System.out.println("Gli eventi dell'era sono in corso di risoluzione...");
        System.out.println("⏳ Attendi la fine della risoluzione...");

        synchronized (clientVirtualView.turnLock) {
            while (clientVirtualView.getGamePhase() == GAME_PHASE.SOLVING_EVENTS){
                System.out.println("Eventi risolti finora: ");
                clientVirtualView.getResolvedEvents().forEach(event -> System.out.println(event.toString()));
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
        tuiUtils.clearScreen();
        System.out.println("Gli eventi sono stati risolti");
        tuiUtils.pauseAndClear();
    }
}
