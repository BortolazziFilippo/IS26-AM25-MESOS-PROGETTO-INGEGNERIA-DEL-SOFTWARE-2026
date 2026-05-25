package it.polimi.ingsw.am25.server.model.Factory.Building;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Effect.Building.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.BuildingDTO;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction.shuffledFromYToXExclusive;

/**
 * Factory that reads building definitions from JSON and assembles the correct set of
 * {@link it.polimi.ingsw.am25.server.model.Card.BuildingCard}s for the given player count,
 * binding each card to its corresponding {@link it.polimi.ingsw.am25.server.model.Effect.Building.BuildingEffect}.
 */
public class BuildingFactory {
    private static final String LOG_PREFIX = "[SERVER][BUILDING_FACTORY]";

    /**
     * Creates a new building factory instance.
     */
    public BuildingFactory() {
    }

    // Number of buildings to pick per era, indexed by (playerNumber - 2).
    // Columns: Era 1 [0,6), Era 2 [6,13), Era 3 [13,21).
    private static final int[][] ERA_COUNTS = {
        {1, 2, 3},  // 2 players
        {2, 2, 4},  // 3 players
        {2, 3, 4},  // 4 players
        {2, 3, 5},  // 5 players
    };

    /**
     * Builds the building deck for the given player count, selecting the correct number of
     * buildings per era and binding each to its effect.
     *
     * @param playerNumber the number of players in the game (2–5).
     * @param boardView    read-only board reference needed by position-dependent building effects.
     * @return list of {@link BuildingCard}s ordered by era.
     */
    public List<BuildingCard> createBuildingDeck(int playerNumber, BoardView boardView) {
        List<BuildingCard> tempList = new ArrayList<>();
        List<BuildingCard> listToReturn = new ArrayList<>();

        InputStream inputStream = BuildingFactory.class.getResourceAsStream("/CardResources/json/building.json");
        if (inputStream == null) {
            throw new RuntimeException(getClass() + ": Errore apertura file building.json");
        }
        BuildingDTO[] tempCatalogue = new Gson().fromJson(new InputStreamReader(inputStream), BuildingDTO[].class);
        for (BuildingDTO dto : tempCatalogue) {
            tempList.add(new BuildingCard(dto.getEra(), CARD_TYPE.BUILDING, dto.getBuildingID(), dto.getFoodCost(), dto.getEndGamePP(), dto.getApplyOn()));
        }

        if (playerNumber < 2 || playerNumber > 5) {
            logServerError("Invalid player number: " + playerNumber);
            return listToReturn;
        }
        int[] counts = ERA_COUNTS[playerNumber - 2];
        pickFromEra(tempList, listToReturn, 0,  6,  counts[0]);
        pickFromEra(tempList, listToReturn, 6,  13, counts[1]);
        pickFromEra(tempList, listToReturn, 13, 21, counts[2]);

        for (BuildingCard n : listToReturn) {
            n.setBuildingEffect(returnCorrectBuildingEffect(n, boardView));
        }
        return listToReturn;
    }

    /**
     * Picks {@code count} buildings at random from {@code source} in the index range
     * {@code [from, to)} and appends them to {@code dest}.
     *
     * @param source the full building catalogue.
     * @param dest   the deck being assembled.
     * @param from   inclusive start index in {@code source}.
     * @param to     exclusive end index in {@code source}.
     * @param count  number of buildings to pick.
     */
    private void pickFromEra(List<BuildingCard> source, List<BuildingCard> dest, int from, int to, int count) {
        List<Integer> indices = shuffledFromYToXExclusive(from, to);
        for (int i = 0; i < count; i++) {
            dest.add(source.get(indices.get(i)));
        }
    }

    /**
     * private method use to get the right effect-ID bind
     *
     * @param buildingToSetEffect building to bind the effect
     * @return return the Right Building effect for the building
     */
    private BuildingEffect returnCorrectBuildingEffect(BuildingCard buildingToSetEffect, BoardView boardView) {
        return switch (buildingToSetEffect.getBuildingID()) {
            case 1  -> new SixFoodCompletedSet();
            case 2  -> new DiscountFoodOnSustenance(CARD_TYPE.GATHERER);
            case 3  -> new DiscountFoodOnSustenance(CARD_TYPE.ARTIST);
            case 4  -> new NoPPLostOnShaman();
            case 5  -> {
                PlusOneFoodOnReturnDefaultTile e = new PlusOneFoodOnReturnDefaultTile();
                e.setBoardView(boardView);
                yield e;
            }
            case 6  -> new FoodOnNewCoupleInventors();
            case 7  -> new DoublePPOnShamanEvent();
            case 8  -> new ThreeMoreShamanStar();
            case 9  -> new DiscountFoodOnSustenance(CARD_TYPE.INVENTOR);
            case 10 -> new OnEventHuntOneFoodAndOnePPPerHunter();
            case 11 -> new BuilderDoublePP();
            case 12 -> new OnEventPaintingsOneFoodPerArtist();
            case 13 -> new SetSixCard();
            case 14 -> new PPPerCharType(3, CARD_TYPE.HUNTER);
            case 15 -> new PPPerCharType(4, CARD_TYPE.GATHERER);
            case 16 -> new PPPerCharType(4, CARD_TYPE.SHAMAN);
            case 17 -> new PPPerCharType(4, CARD_TYPE.BUILDER);
            case 18 -> new PPPerCharType(4, CARD_TYPE.ARTIST);
            case 19 -> new PPPerCharType(2, CARD_TYPE.INVENTOR);
            case 20 -> new DrawOneMoreCard();
            case 21 -> new TwentyFivePPEndGame();
            default -> {
                // Unrecognised building ID — this should never happen if the JSON is correct.
                logServerError("Unrecognised building ID: " + buildingToSetEffect.getBuildingID());
                yield null;
            }
        };
    }

    /**
     * Creates a single BuildingCard with its effect bound, looked up by building ID.
     *
     * @param id        the building ID to look up.
     * @param boardView read-only board reference needed by position-dependent effects.
     * @return the matching BuildingCard with its effect bound.
     */
    public BuildingCard createBuildingById(int id, BoardView boardView) {
        InputStream inputStream = BuildingFactory.class.getResourceAsStream("/CardResources/json/building.json");
        if (inputStream == null) {
            throw new RuntimeException(getClass() + ": Errore apertura file building.json");
        }
        BuildingDTO[] catalogue = new Gson().fromJson(new InputStreamReader(inputStream), BuildingDTO[].class);
        for (BuildingDTO dto : catalogue) {
            if (dto.getBuildingID() == id) {
                BuildingCard card = new BuildingCard(dto.getEra(), CARD_TYPE.BUILDING, dto.getBuildingID(),
                        dto.getFoodCost(), dto.getEndGamePP(), dto.getApplyOn());
                card.setBuildingEffect(returnCorrectBuildingEffect(card, boardView));
                return card;
            }
        }
        throw new IllegalArgumentException("Unknown building ID: " + id);
    }

    /**
     * Executes log server error.
     *
     * @param message parameter message.
     */
    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }
}
