package it.polimi.ingsw.am25.client.webLayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitors server liveness by tracking the last time any activity was received.
 * If no activity is recorded within the configured threshold, the death callback is invoked.
 * Used by both Socket ({@code ServerListener}) and RMI ({@code ClientTUI}) clients
 * to apply the same disconnection-detection logic regardless of transport.
 */
public class PongWatchdog {

    /** Interval between consecutive server pings/pongs, in seconds. */
    public static final int INTERVAL_S = 1;
    /** Number of consecutive missed pongs before the server is declared dead. */
    public static final int MISSED_PONG_THRESHOLD = 8;
    /** Silence duration in milliseconds after which the server is declared dead. */
    public static final long THRESHOLD_MS = (long) MISSED_PONG_THRESHOLD * INTERVAL_S * 1000L;

    private volatile long lastActivity = System.currentTimeMillis();
    private ScheduledExecutorService scheduler;

    /**
     * Records that the server is still alive. Must be called whenever any message
     * from the server is received (any game message for Socket; pong for RMI).
     */
    public void recordActivity() {
        lastActivity = System.currentTimeMillis();
    }

    /**
     * Starts the watchdog monitor. Checks every {@link #INTERVAL_S} second whether the server
     * has been silent for longer than {@link #THRESHOLD_MS}; if so, invokes {@code onDeath} once and stops.
     *
     * @param onDeath callback invoked when the silence threshold is exceeded.
     */
    public void start(Runnable onDeath) {
        lastActivity = System.currentTimeMillis();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "pong-watchdog");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - lastActivity > THRESHOLD_MS) {
                onDeath.run();
                scheduler.shutdownNow();
            }
        }, THRESHOLD_MS, INTERVAL_S * 1000L, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the watchdog monitor. Safe to call even if {@link #start} was never called.
     */
    public void stop() {
        if (scheduler != null) scheduler.shutdownNow();
    }
}
