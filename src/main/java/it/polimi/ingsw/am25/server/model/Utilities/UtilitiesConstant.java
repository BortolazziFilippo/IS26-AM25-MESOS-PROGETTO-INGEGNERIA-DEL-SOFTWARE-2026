package it.polimi.ingsw.am25.server.model.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Compile-time constants shared across the Mesos server model.
 * All values relate to market sizing and food rewards for different player counts.
 */
public final class UtilitiesConstant {
    /**
     * Number of cards in the top market row for a 2-player game.
     */
    public static final int TWO_PLAYER_TOP_CARD = 6;
    /**
     * Number of cards in the top market row for a 3-player game.
     */
    public static final int THREE_PLAYER_TOP_CARD = 7;
    /**
     * Number of cards in the top market row for a 4-player game.
     */
    public static final int FOUR_PLAYER_TOP_CARD = 8;
    /**
     * Number of cards in the top market row for a 5-player game.
     */
    public static final int FIVE_PLAYER_TOP_CARD = 9;
    /**
     * Number of cards in the bottom market row for a 2-player game.
     */
    public static final int TWO_PLAYER_BOTTOM_CARD = 3;
    /**
     * Number of cards in the bottom market row for a 3-player game.
     */
    public static final int THREE_PLAYER_BOTTOM_CARD = 4;
    /**
     * Number of cards in the bottom market row for a 4-player game.
     */
    public static final int FOUR_PLAYER_BOTTOM_CARD = 5;
    /**
     * Number of cards in the bottom market row for a 5-player game.
     */
    public static final int FIVE_PLAYER_BOTTOM_CARD = 6;
    /**
     * Food granted to a player who places their totem on offer tile A (no card draws).
     */
    public static final int FOOD_OFFERTILE_A = 3;

    /**
     * Bonus points awarded by final ranking position in a 2-player game
     * (1st place, 2nd place).
     */
    public static final List<Integer> SCORE_TWO_PLAYERS = new ArrayList<>(Arrays.asList(10, 5));
    /**
     * Bonus points awarded by final ranking position in a 3-player game
     * (1st place, 2nd place, 3rd place).
     */
    public static final List<Integer> SCORE_THREE_PLAYERS = new ArrayList<>(Arrays.asList(15, 8, 3));
    /**
     * Bonus points awarded by final ranking position in a 4-player game
     * (1st place, 2nd place, 3rd place, 4th place).
     */
    public static final List<Integer> SCORE_FOUR_PLAYERS = new ArrayList<>(Arrays.asList(20, 12, 6, 2));
    /**
     * Bonus points awarded by final ranking position in a 5-player game
     * (1st place, 2nd place, 3rd place, 4th place, 5th place).
     */
    public static final List<Integer> SCORE_FIVE_PLAYERS = new ArrayList<>(Arrays.asList(25, 16, 9, 4, 1));

    /**
     * Utility class — not instantiable.
     */
    private UtilitiesConstant() {
    }
}
