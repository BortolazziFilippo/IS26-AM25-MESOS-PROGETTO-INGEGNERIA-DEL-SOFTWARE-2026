package it.polimi.ingsw.am25.Model.Utilities;

import it.polimi.ingsw.am25.Model.Card.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface UtilitiesFunction {
    static int bindCorrectNumberOfTopListCard(int playerNumber) {
        return switch (playerNumber) {
            case 2 -> UtilitiesConstant.TWO_PLAYER_TOP_CARD;
            case 3 -> UtilitiesConstant.THREE_PLAYER_TOP_CARD;
            case 4 -> UtilitiesConstant.FOUR_PLAYER_TOP_CARD;
            case 5 -> UtilitiesConstant.FIVE_PLAYER_TOP_CARD;
            default -> {
                System.err.println("Utilities error binding");
                yield -1;
            }
        };
    }

    static int bindCorrectNumberOfBottomListCard(int playerNumber) {
        return switch (playerNumber) {
            case 2 -> UtilitiesConstant.TWO_PLAYER_BOTTOM_CARD;
            case 3 -> UtilitiesConstant.THREE_PLAYER_BOTTOM_CARD;
            case 4 -> UtilitiesConstant.FOUR_PLAYER_BOTTOM_CARD;
            case 5 -> UtilitiesConstant.FIVE_PLAYER_BOTTOM_CARD;
            default -> {
                System.err.println("Utilities error binding");
                yield -1;
            }
        };
    }

    static void countOccurrence(List<Card> listToParse, List<Integer> setCards) {
        int quantity = 6;
        for (Card card : listToParse) {
            switch (card.getCardType()) {
                case BUILDER:
                    setCards.set(0, setCards.getFirst() + 1);
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
                    System.err.println(" errore identificazione carta");

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
            throw new IllegalArgumentException("y deve essere <= x");
        }

        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = y; i < x; i++) {
            numbers.add(i);
        }

        Collections.shuffle(numbers);
        return numbers;
    }
}
