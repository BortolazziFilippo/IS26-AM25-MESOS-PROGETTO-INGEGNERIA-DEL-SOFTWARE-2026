package it.polimi.ingsw.am25.server.model.DBmanager;

import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all interactions with the remote AWS RDS leaderboard database.
 * Maintains a single persistent {@link Connection} that is lazily opened on first use
 * and reused for all subsequent operations.
 * Credentials are loaded from a local {@code DBcred} file in the resources directory,
 * formatted as {@code KEY=VALUE} pairs ({@code DB_USER} and {@code DB_PASSWORD}).
 */
public class DBManager {
    private static final String URL = "jdbc:mysql://am25-leaderboard.c6b8uwgkopvm.us-east-1.rds.amazonaws.com:3306/leaderboardDB?connectTimeout=5000&socketTimeout=10000";
    private static final String LOG_PREFIX = "[SERVER][DB]";
    private static Connection connection;

    /**
     * Default constructor. An instance is not required to use this class
     * since all methods are static.
     */
    private DBManager() {
    }

    private static Map<String, String> loadCredentials() throws IOException {
        Map<String, String> creds = new HashMap<>();
        try (InputStream inputStream = DBManager.class.getClassLoader().getResourceAsStream("DBcred")) {
            if (inputStream == null) throw new IOException("File DBcred not found in classpath");
            for (String line : new String(inputStream.readAllBytes()).lines().toList()) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    creds.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return creds;
    }

    /**
     * Returns the active database connection, opening a new one if needed.
     *
     * @return an open {@link Connection} to the leaderboard database.
     * @throws SQLException if the JDBC driver cannot establish the connection.
     * @throws IOException  if the {@code DBcred} credentials file cannot be read.
     */
    public static synchronized Connection getConnection() throws SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            UtilitiesFunction.logInfo(LOG_PREFIX, "Opening connection to database...");
            Map<String, String> creds = loadCredentials();
            connection = DriverManager.getConnection(URL, creds.get("DB_USER"), creds.get("DB_PASSWORD"));
            UtilitiesFunction.logInfo(LOG_PREFIX, "Connection established.");
        }
        return connection;
    }

    /**
     * Closes the active database connection if one is open.
     * Should be called only at server shutdown.
     *
     * @throws SQLException if closing the connection fails.
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            UtilitiesFunction.logInfo(LOG_PREFIX, "Connection closed.");
        }
    }

    /**
     * Persists the result of a completed match to the database.
     * Inserts a new row in the {@code game} table, registers any unknown players
     * in the {@code player} table, and records each player's score in the {@code result}
     * table. The list must contain all players ordered by final ranking (1st to last),
     * so that the correct score table is applied via {@link UtilitiesFunction#getScore}.
     * If any SQL operation fails the cached connection is reset to {@code null} so that
     * the next call attempts a fresh connection rather than reusing a stale TCP socket.
     *
     * @param players all players sorted by finishing position (the best first).
     * @throws SQLException if any database operation fails.
     * @throws IOException  if the credentials file cannot be read.
     */
    public static void logMatch(List<Player> players) throws SQLException, IOException {
        UtilitiesFunction.logInfo(LOG_PREFIX, "Saving game with " + players.size() + " players...");
        Connection conn;
        try {
            conn = getConnection();
        } catch (SQLException e) {
            connection = null;
            throw e;
        }

        String insertGame = "INSERT INTO game (played_at, player_count) VALUES (NOW(), ?)";
        int gameId;
        try (PreparedStatement ps = conn.prepareStatement(insertGame, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, players.size());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            gameId = rs.getInt(1);
        } catch (SQLException e) {
            connection = null;
            throw e;
        }
        UtilitiesFunction.logInfo(LOG_PREFIX, "Game inserted with ID=" + gameId + ".");

        String insertPlayer = "INSERT IGNORE INTO player (nickname) VALUES (?)";
        String insertResult = "INSERT INTO result (game_id, player_nick, score) VALUES (?, ?, ?)";

        for (int i = 0; i < players.size(); i++) {
            String nick = players.get(i).getNickname();
            int score = UtilitiesFunction.getScore(players.size(), i + 1);
            try (PreparedStatement ps = conn.prepareStatement(insertPlayer)) {
                ps.setString(1, nick);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(insertResult)) {
                ps.setInt(1, gameId);
                ps.setString(2, nick);
                ps.setInt(3, score);
                ps.executeUpdate();
            }
            UtilitiesFunction.logInfo(LOG_PREFIX, "  Position " + (i + 1) + ": " + nick + " → " + score + " points");
        }

        UtilitiesFunction.logInfo(LOG_PREFIX, "Game saved successfully.");
    }

    /**
     * Retrieves the all-time leaderboard for games played with the given number of players,
     * ordered by cumulative score descending.
     * Each entry is formatted as {@code "N. nickname - totalScore"}.
     *
     * @param playerCount the number of players per game to filter by (2–5).
     * @return an ordered list of leaderboard entries, empty if no data exists.
     * @throws SQLException if the query fails.
     * @throws IOException  if the credentials file cannot be read.
     */
    public static List<String> getLeaderboard(int playerCount) throws SQLException, IOException {
        UtilitiesFunction.logInfo(LOG_PREFIX, "Retrieving scoreboard for " + playerCount + " players...");
        Connection conn = getConnection();
        String query = """
                SELECT player_nick, SUM(score) AS total_score
                FROM result
                JOIN game ON result.game_id = game.id
                WHERE game.player_count = ?
                GROUP BY player_nick
                ORDER BY total_score DESC
                """;
        List<String> leaderboard = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerCount);
            ResultSet rs = ps.executeQuery();
            int position = 1;
            while (rs.next()) {
                leaderboard.add(position + ". " + rs.getString("player_nick") + " - " + rs.getInt("total_score"));
                position++;
            }
        }
        UtilitiesFunction.logInfo(LOG_PREFIX, "Scoreboard retrieved: " + leaderboard.size() + " entries.");
        return leaderboard;
    }

    /**
     * Returns the 1-based leaderboard position of the given player for games
     * played with the specified number of players.
     *
     * @param nickname    the player's nickname to look up.
     * @param playerCount the number of players per game to filter by (2–5).
     * @return the player's rank (1 = first), or {@code -1} if not found.
     * @throws SQLException if the query fails.
     * @throws IOException  if the credentials file cannot be read.
     */
    public static int getPlayerPosition(String nickname, int playerCount) throws SQLException, IOException {
        List<String> leaderboard = getLeaderboard(playerCount);
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).contains(nickname)) {
                UtilitiesFunction.logInfo(LOG_PREFIX, "Position of '" + nickname + "': " + (i + 1));
                return i + 1;
            }
        }
        UtilitiesFunction.logInfo(LOG_PREFIX, "Player '" + nickname + "' not found in scoreboard.");
        return -1;
    }


}
