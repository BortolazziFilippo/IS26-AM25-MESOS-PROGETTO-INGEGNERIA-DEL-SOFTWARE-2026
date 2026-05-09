package it.polimi.ingsw.am25.client.TUI;

import it.polimi.ingsw.am25.server.model.Enums.COLOR;

import java.util.Scanner;

/**
 * Utility class for common TUI operations: screen management,
 * input helpers, error formatting, and color constants.
 */
public class TUIUtils {

    private final Scanner scanner;

    /**
     * ANSI reset sequence — restores the default terminal colour.
     */
    public static final String RESET = "\033[0m";
    /**
     * ANSI bold-red colour sequence.
     */
    public static final String RED = "\033[31;49;1m";
    /**
     * ANSI bold-green colour sequence.
     */
    public static final String GREEN = "\033[32;49;1m";
    /**
     * ANSI bold-yellow colour sequence.
     */
    public static final String YELLOW = "\033[33;49;1m";
    /**
     * ANSI bold-blue colour sequence.
     */
    public static final String BLUE = "\033[34;49;1m";
    /**
     * ANSI bold-purple colour sequence.
     */
    public static final String PURPLE = "\033[35;49;1m";

    /**
     * Creates a new TUIUtils instance.
     *
     * @param scanner the shared Scanner for user input.
     */
    public TUIUtils(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Clears the terminal screen.
     */
    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Waits for the user to press ENTER, then clears the screen.
     */
    public void pauseAndClear() {
        System.out.println("\n(Premi INVIO per continuare...)");
        scanner.nextLine();
        clearScreen();
    }

    /**
     * Extracts a human-readable message from an exception.
     *
     * @param e the exception to parse.
     * @return the cleaned error message.
     */
    public String extractCleanError(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains(":")) {
            String[] parts = e.getMessage().split(":");
            return parts[parts.length - 1].trim();
        }
        return e.getMessage() != null ? e.getMessage() : "Errore sconosciuto";
    }

    /**
     * Asks the user to enter the number of players (2-5).
     *
     * @return the validated number of players.
     */
    public int numberOfPlayer() {
        while (true) {
            System.out.print("Quanti giocatori (2-5)? ");
            try {
                int num = Integer.parseInt(scanner.nextLine());
                if (num >= 2 && num <= 5) return num;
                System.err.println("\n❌ Errore: Il numero deve essere compreso tra 2 e 5.");
            } catch (NumberFormatException e) {
                System.err.println("\n❌ Errore: Devi inserire un NUMERO.");
            }
        }
    }

    /**
     * Asks the user to choose a totem color.
     *
     * @return the chosen COLOR enum value.
     */
    public COLOR bindTotemColor() {
        while (true) {
            System.out.println("\nScegli colore totem:");
            System.out.println(RED + "1-ROSSO" + BLUE + "\n2-BLU" + YELLOW + "\n3-GIALLO"
                    + RESET + "\n4-BIANCO" + PURPLE + "\n5-VIOLA" + RESET);
            System.out.print("Scelta: ");
            switch (scanner.nextLine()) {
                case "1":
                    return COLOR.RED;
                case "2":
                    return COLOR.BLUE;
                case "3":
                    return COLOR.YELLOW;
                case "4":
                    return COLOR.WHITE;
                case "5":
                    return COLOR.PURPLE;
                default:
                    System.err.println("\n❌ Input non valido");
            }
        }
    }

    public String getAnsiColor(COLOR color) {
        if (color == null) return RESET;
        return switch (color) {
            case RED -> RED;
            case BLUE -> BLUE;
            case YELLOW -> YELLOW;
            case WHITE -> RESET;
            case PURPLE -> PURPLE;
        };
    }

}
