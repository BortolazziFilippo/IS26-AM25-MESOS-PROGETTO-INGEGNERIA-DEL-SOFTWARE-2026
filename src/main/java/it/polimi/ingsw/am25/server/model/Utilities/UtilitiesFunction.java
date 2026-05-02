package it.polimi.ingsw.am25.server.model.Utilities;

import it.polimi.ingsw.am25.server.model.Card.Card;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.am25.server.model.Utilities.UtilitiesConstant.*;

/**
 * Logging utilities for the Mesos server. All methods write timestamped entries
 * to {@value #LOG_FILE}; output is silently dropped if the log has not been initialised.
 */
public interface UtilitiesFunction {
    /** Log tag prepended to all messages written by this utility. */
    String LOG_PREFIX = "[SERVER][UTILS]";
    /** Path of the server log file. */
    String LOG_FILE = "server.log";
    /** Timestamp format used in every log entry. */
    DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** Shared reference to the active log writer; {@code null} until {@link #initLog()} is called. */
    AtomicReference<PrintWriter> LOG_WRITER = new AtomicReference<>(null);

    /**
     * Initialises (or resets) the log file. Call once at server startup.
     * Every call truncates the previous content.
     */
    static void initLog() {
        try {
            PrintWriter old = LOG_WRITER.getAndSet(new PrintWriter(new FileWriter(LOG_FILE, false)));
            if (old != null) {
                old.close();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to initialise log file: " + e.getMessage());
        }
    }

    /**
     * Logs an informational message to console and log file.
     * @param prefix parameter prefix.
     * @param message parameter message.
     */
    static void logInfo(String prefix, String message) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP_FMT) + "]" + prefix + " " + message;
        System.out.println(line);
        PrintWriter writer = LOG_WRITER.get();
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    /**
     * Logs an error message to console and log file.
     * @param prefix parameter prefix.
     * @param message parameter message.
     */
    static void logError(String prefix, String message) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP_FMT) + "]" + prefix + "[ERROR] " + message;
        System.err.println(line);
        PrintWriter writer = LOG_WRITER.get();
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    /**
     * Logs an error message using the default utility prefix.
     * @param message parameter message.
     */
    static void logError(String message) {
        logError(LOG_PREFIX, message);
    }

    /**
     * Returns the number of top-list cards based on player count.
     * @param playerNumber parameter playerNumber.
     * @return the result of the operation.
     */
    static int bindCorrectNumberOfTopListCard(int playerNumber) {
        return switch (playerNumber) {
            case 2 -> UtilitiesConstant.TWO_PLAYER_TOP_CARD;
            case 3 -> UtilitiesConstant.THREE_PLAYER_TOP_CARD;
            case 4 -> UtilitiesConstant.FOUR_PLAYER_TOP_CARD;
            case 5 -> UtilitiesConstant.FIVE_PLAYER_TOP_CARD;
            default -> {
                logError("Invalid player number for top-list binding: " + playerNumber);
                yield -1;
            }
        };
    }

    /**
     * Returns the number of bottom-list cards based on player count.
     * @param playerNumber parameter playerNumber.
     * @return the result of the operation.
     */
    static int bindCorrectNumberOfBottomListCard(int playerNumber) {
        return switch (playerNumber) {
            case 2 -> UtilitiesConstant.TWO_PLAYER_BOTTOM_CARD;
            case 3 -> UtilitiesConstant.THREE_PLAYER_BOTTOM_CARD;
            case 4 -> UtilitiesConstant.FOUR_PLAYER_BOTTOM_CARD;
            case 5 -> UtilitiesConstant.FIVE_PLAYER_BOTTOM_CARD;
            default -> {
                logError("Invalid player number for bottom-list binding: " + playerNumber);
                yield -1;
            }
        };
    }

    /**
     * Counts card occurrences by type into the destination counters list.
     * @param listToParse parameter listToParse.
     * @param setCards parameter setCards.
     */
    static void countOccurrence(List<Card> listToParse, List<Integer> setCards) {
        int quantity = 6;
        for (Card card : listToParse) {
            switch (card.getCardType()) {
                case BUILDER:
                    setCards.set(0, setCards.get(0) + 1);
                    break;
                case ARTIST:
                    setCards.set(1, setCards.get(1) + 1);
                    break;
                case GATHERER:
                    setCards.set(2, setCards.get(2) + 1);
                    break;
                case SHAMAN:
                    setCards.set(3, setCards.get(3) + 1);
                    break;
                case INVENTOR:
                    setCards.set(4, setCards.get(4) + 1);
                    break;
                case HUNTER:
                    setCards.set(5, setCards.get(5) + 1);
                    break;
                default:
                    logError("Unrecognised card type in occurrence count: " + card.getCardType());

            }
        }
    }

    /**
     * this method generates a shuffled array of integer from y to x-1
     * @param y lower bound
     * @param x upper bound
     * @return list of integers
     */
     static ArrayList<Integer> shuffledFromYToXExclusive(int y, int x) {
        if (y > x) {
            throw new IllegalArgumentException("y must be <= x");
        }

        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = y; i < x; i++) {
            numbers.add(i);
        }

        Collections.shuffle(numbers);
        return numbers;
    }
     static int getScore(int playerCount, int position) {
        List<Integer> table = switch (playerCount) {
            case 2 -> SCORE_TWO_PLAYERS;
            case 3 -> SCORE_THREE_PLAYERS;
            case 4 -> SCORE_FOUR_PLAYERS;
            case 5 -> SCORE_FIVE_PLAYERS;
            default -> throw new IllegalArgumentException("Numero giocatori non valido: " + playerCount);
        };
        return table.get(position - 1);
    }

    static int stringToIntegerBinder(String number) throws IllegalStateException{
         switch (number){
             case "2":
                 return 2;
             case "3":
                 return 3;
             case "4":
                 return 4;
             case "5":
                 return 5;
             default:
                 throw new IllegalStateException("Errore input");
         }
    }
}
