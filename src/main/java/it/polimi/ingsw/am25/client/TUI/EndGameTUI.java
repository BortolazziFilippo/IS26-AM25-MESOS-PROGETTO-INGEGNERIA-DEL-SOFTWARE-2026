package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.util.List;
import java.util.Scanner;

public class EndGameTUI {
        private final ClientVirtualView clientVirtualView;
        private final TUIUtils tuiUtils;
        private final Scanner scanner;
        private final PlayerDTO myPlayer;

        public EndGameTUI(ClientVirtualView clientVirtualView, TUIUtils tuiUtils,
                          Scanner scanner, PlayerDTO myPlayer) {
            this.clientVirtualView = clientVirtualView;
            this.tuiUtils = tuiUtils;
            this.scanner = scanner;
            this.myPlayer = myPlayer;
        }

        public void finished(List<PlayerDTO> winners) {
            PlayerStatusTUI playerStatusTUI =
                    new PlayerStatusTUI(clientVirtualView, scanner, tuiUtils, myPlayer);

            printWinnersScreen(winners);

            while (true) {
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("i")) {
                    playerStatusTUI.printAllPlayersStatus();
                    printWinnersScreen(winners);
                } else {
                    break;
                }
            }

            tuiUtils.clearScreen();
        }

        private void printWinnersScreen(List<PlayerDTO> winners) {
            tuiUtils.clearScreen();
            System.out.println("=".repeat(68));
            System.out.println("                        FINE PARTITA");
            System.out.println("=".repeat(68));
            System.out.println();

            if (winners == null || winners.isEmpty()) {
                System.out.println("  Nessun vincitore disponibile.");
            } else if (winners.size() == 1) {
                PlayerDTO w = winners.get(0);
                System.out.println("  Il vincitore e': " + w.getNickName()
                        + "  (" + w.getPrestigePoint() + " PP)");
            } else {
                System.out.println("  I vincitori sono:");
                winners.forEach(p ->
                        System.out.println("    - " + p.getNickName()
                                + "  (" + p.getPrestigePoint() + " PP)"));
            }

            System.out.println();
            System.out.println("-".repeat(68));
            System.out.println("  [i] Stato tutti i giocatori   [INVIO] Esci");
            System.out.print("\nScelta: ");
        }
}
