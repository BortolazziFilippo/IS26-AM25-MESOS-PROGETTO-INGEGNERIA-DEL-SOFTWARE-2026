package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.client.webLayer.RMI.ClientVirtualView;
import it.polimi.ingsw.am25.client.webLayer.RMI.ServerRemoteInterface;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * TUI component that renders the end-game screen. Shows the winner(s),
 * the global leaderboard fetched from the server, and optionally all
 * players' final statuses on request.
 */
public class EndGameTUI {
    private final ClientVirtualView clientVirtualView;
    private final TUIUtils tuiUtils;
    private final Scanner scanner;
    private final PlayerDTO myPlayer;
    private final ServerRemoteInterface serverStub;

    /**
     * Creates the end-game TUI screen.
     *
     * @param clientVirtualView the local client view holding the game state.
     * @param tuiUtils          the shared TUI utilities for screen and input management.
     * @param scanner           the shared scanner for reading user input.
     * @param myPlayer          the local player DTO (used to highlight them in the leaderboard).
     * @param serverStub        the remote server interface used to request the global leaderboard.
     */
    public EndGameTUI(ClientVirtualView clientVirtualView, TUIUtils tuiUtils,
                      Scanner scanner, PlayerDTO myPlayer, ServerRemoteInterface serverStub) {
        this.clientVirtualView = clientVirtualView;
        this.tuiUtils = tuiUtils;
        this.scanner = scanner;
        this.myPlayer = myPlayer;
        this.serverStub = serverStub;
    }

    /**
     * Displays the end-game screen with the list of winners and the global leaderboard.
     * Waits for input: pressing "I" shows all players' statuses,
     * any other key (or ENTER) exits the screen.
     *
     * @param winners the list of winning players.
     */
    public void finished(List<PlayerDTO> winners) {
        PlayerStatusTUI playerStatusTUI =
                new PlayerStatusTUI(clientVirtualView, scanner, tuiUtils, myPlayer);

        Map<Integer, List<String>> leaderboard = fetchLeaderboard();
        printWinnersScreen(winners, leaderboard);

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("i")) {
                playerStatusTUI.printAllPlayersStatus();
                printWinnersScreen(winners, leaderboard);
            } else {
                break;
            }
        }

        tuiUtils.clearScreen();
    }

    private Map<Integer, List<String>> fetchLeaderboard() {
        int playerCount = clientVirtualView.getPlayers().size();
        clientVirtualView.clearLeaderboards();
        clientVirtualView.connectionError = false;
        try {
            serverStub.askForRank(String.valueOf(playerCount), clientVirtualView);
        } catch (RemoteException e) {
            return null;
        }
        synchronized (clientVirtualView.turnLock) {
            while (clientVirtualView.getLeaderboards() == null && !clientVirtualView.connectionError) {
                try {
                    clientVirtualView.turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        if (clientVirtualView.connectionError) return null;
        return clientVirtualView.getLeaderboards();
    }

    private void printWinnersScreen(List<PlayerDTO> winners, Map<Integer, List<String>> leaderboard) {
        tuiUtils.clearScreen();
        System.out.println("=".repeat(68));
        System.out.println("                        FINE PARTITA");
        System.out.println("=".repeat(68));
        System.out.println();

        if (winners == null || winners.isEmpty()) {
            System.out.println("  Nessun vincitore disponibile.");
        } else if (winners.size() == 1) {
            PlayerDTO w = winners.getFirst();
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

        int playerCount = clientVirtualView.getPlayers().size();
        System.out.println("  CLASSIFICA (" + playerCount + " giocatori):");
        System.out.println();

        if (leaderboard == null) {
            System.out.println("  Classifica non disponibile.");
        } else {
            List<String> entries = leaderboard.get(playerCount);
            if (entries == null || entries.isEmpty()) {
                System.out.println("  Nessun dato disponibile.");
            } else {
                String myColor = tuiUtils.getAnsiColor(myPlayer != null ? myPlayer.getColorTotem() : null);
                for (String entry : entries) {
                    if (myPlayer != null && entry.contains(". " + myPlayer.getNickName() + " - ")) {
                        System.out.println("  " + myColor + entry + TUIUtils.RESET);
                    } else {
                        System.out.println("  " + entry);
                    }
                }
            }
        }

        System.out.println();
        System.out.println("-".repeat(68));
        System.out.println("  [I] Stato tutti i giocatori   [INVIO] Esci");
        System.out.print("\nScelta: ");
    }
}
