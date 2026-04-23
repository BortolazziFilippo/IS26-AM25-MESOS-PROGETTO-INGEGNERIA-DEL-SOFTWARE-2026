package it.polimi.ingsw.am25.client.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

public interface ClientUtilitiesFunction {
    String LOG_PREFIX = "[CLIENT][UTILS]";
    String LOG_FILE = "client.log";
    DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    AtomicReference<PrintWriter> LOG_WRITER = new AtomicReference<>(null);

    static void initLog() {
        try {
            PrintWriter old = LOG_WRITER.getAndSet(new PrintWriter(new FileWriter(LOG_FILE, false)));
            if (old != null) {
                old.close();
            }
        } catch (IOException ignored) {
        }
    }

    static void logInfo(String prefix, String message) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP_FMT) + "]" + prefix + " " + message;
        PrintWriter writer = LOG_WRITER.get();
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    static void logError(String prefix, String message) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP_FMT) + "]" + prefix + "[ERROR] " + message;
        PrintWriter writer = LOG_WRITER.get();
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    static void logError(String message) {
        logError(LOG_PREFIX, message);
    }
}