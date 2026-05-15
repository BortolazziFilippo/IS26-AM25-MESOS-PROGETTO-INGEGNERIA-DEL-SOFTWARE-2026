package it.polimi.ingsw.am25.client.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Logging utilities for the Mesos client. All methods write timestamped entries
 * to {@code client.log}; output is silently dropped if the log has not been initialised.
 */
public interface ClientUtilitiesFunction {
    /**
     * Log tag prepended to all messages written by this utility.
     */
    int SOCKET_TIMEOUT_MS = 1000;
    int RMI_RESPONSE_TIMEOUT_MS = 1000;

    String LOG_PREFIX = "[CLIENT][UTILS]";
    /**
     * Path of the client log file.
     */
    String LOG_FILE = "client.log";
    /**
     * Timestamp format used in every log entry.
     */
    DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * Shared reference to the active log writer; {@code null} until {@link #initLog()} is called.
     */
    AtomicReference<PrintWriter> LOG_WRITER = new AtomicReference<>(null);

    /**
     * Opens (or truncates) the client log file. Must be called once before any logging.
     */
    static void initLog() {
        try {
            PrintWriter old = LOG_WRITER.getAndSet(new PrintWriter(new FileWriter(LOG_FILE, false)));
            if (old != null) {
                old.close();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Writes an info-level entry to the log.
     *
     * @param prefix  log tag identifying the caller (e.g. {@code "[CLIENT][TUI]"}).
     * @param message the message to log.
     */
    static void logInfo(String prefix, String message) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP_FMT) + "]" + prefix + " " + message;
        PrintWriter writer = LOG_WRITER.get();
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    /**
     * Writes an error-level entry to the log.
     *
     * @param prefix  log tag identifying the caller.
     * @param message the error message to log.
     */
    static void logError(String prefix, String message) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP_FMT) + "]" + prefix + "[ERROR] " + message;
        PrintWriter writer = LOG_WRITER.get();
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    /**
     * Writes an error-level entry using the default {@link #LOG_PREFIX}.
     *
     * @param message the error message to log.
     */
    static void logError(String message) {
        logError(LOG_PREFIX, message);
    }
}