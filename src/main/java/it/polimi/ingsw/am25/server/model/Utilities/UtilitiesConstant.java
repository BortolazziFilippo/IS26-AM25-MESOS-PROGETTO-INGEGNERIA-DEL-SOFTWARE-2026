package it.polimi.ingsw.am25.server.model.Utilities;

/**
 * Compile-time constants shared across the Mesos server model.
 * All values relate to market sizing and food rewards for different player counts.
 */
public final class UtilitiesConstant {
    /** Number of cards in the top market row for a 2-player game. */
    public static final int TWO_PLAYER_TOP_CARD = 6;
    /** Number of cards in the top market row for a 3-player game. */
    public static final int THREE_PLAYER_TOP_CARD = 7;
    /** Number of cards in the top market row for a 4-player game. */
    public static final int FOUR_PLAYER_TOP_CARD = 8;
    /** Number of cards in the top market row for a 5-player game. */
    public static final int FIVE_PLAYER_TOP_CARD = 9;
    /** Number of cards in the bottom market row for a 2-player game. */
    public static final int TWO_PLAYER_BOTTOM_CARD = 3;
    /** Number of cards in the bottom market row for a 3-player game. */
    public static final int THREE_PLAYER_BOTTOM_CARD = 4;
    /** Number of cards in the bottom market row for a 4-player game. */
    public static final int FOUR_PLAYER_BOTTOM_CARD = 5;
    /** Number of cards in the bottom market row for a 5-player game. */
    public static final int FIVE_PLAYER_BOTTOM_CARD = 6;
    /** Food granted to a player who places their totem on offer tile A (no card draws). */
    public static final int FOOD_OFFERTILE_A = 3;

    /** Utility class — not instantiable. */
    private UtilitiesConstant() {}
}
