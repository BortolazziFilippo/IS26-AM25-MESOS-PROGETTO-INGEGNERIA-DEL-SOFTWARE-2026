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
import java.util.Collections;
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

    /**
     * method use to build the right amount of building
     * @param playerNumber number of players
     * @return List ordered by ERA with the right amount of building
     */
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
        List<Integer> randomNumber;
        InputStream inputStream = BuildingFactory.class.getResourceAsStream("/CardResources/json/building.json");
        if (inputStream == null) {
            throw new RuntimeException(getClass() + ": Errore apertura file building.json");
        }
        Reader reader = new InputStreamReader(inputStream);
        Gson gson = new Gson();
        BuildingDTO[] tempCatalogue = gson.fromJson(reader, BuildingDTO[].class);
        for (BuildingDTO dto : tempCatalogue) {
            tempList.add(new BuildingCard(dto.getEra(), CARD_TYPE.BUILDING, dto.getBuildingID(), dto.getFoodCost(), dto.getEndGamePP(), dto.getApplyOn()));
        }

        switch (playerNumber) {
            case 2:
                //ERA 1
                randomNumber = shuffledFromYToXExclusive(0, 6);
                listToReturn.add(tempList.get(randomNumber.get(0)));
                //ERA 2
                randomNumber = shuffledFromYToXExclusive(6, 13);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                //ERA 3
                randomNumber = shuffledFromYToXExclusive(13, 21);
                for (int i = 0; i < 3; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                break;
            case 3:
                //ERA 1
                randomNumber = shuffledFromYToXExclusive(0, 6);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                //ERA 2
                randomNumber = shuffledFromYToXExclusive(6, 13);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                //ERA 3
                randomNumber = shuffledFromYToXExclusive(13, 21);
                for (int i = 0; i < 4; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                break;
            case 4:
                //ERA 1
                randomNumber = shuffledFromYToXExclusive(0, 6);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                //ERA 2
                randomNumber = shuffledFromYToXExclusive(6, 13);
                for (int i = 0; i < 3; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                //ERA 3
                randomNumber = shuffledFromYToXExclusive(13, 21);
                for (int i = 0; i < 4; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                break;
            case 5:
                //ERA 1
                randomNumber = shuffledFromYToXExclusive(0, 6);
                for (int i = 0; i < 2; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                //ERA 2
                randomNumber = shuffledFromYToXExclusive(6, 13);
                for (int i = 0; i < 3; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                //ERA 3
                randomNumber = shuffledFromYToXExclusive(13, 21);
                for (int i = 0; i < 5; i++) {
                    listToReturn.add(tempList.get(randomNumber.get(i)));
                }
                break;
            default:
                logServerError("Invalid player number: " + playerNumber);
        }
        for (BuildingCard n : listToReturn) {
            n.setBuildingEffect(returnCorrectBuildingEffect(n, boardView));
        }
        return listToReturn;
    }

    /**
     * private method use to get the right effect-ID bind
     *
     * @param buildingToSetEffect building to bind the effect
     * @return return the Right Building effect for the building
     */
    private BuildingEffect returnCorrectBuildingEffect(BuildingCard buildingToSetEffect, BoardView boardView) {
        BuildingEffect effectToReturn = null;
        switch (buildingToSetEffect.getBuildingID()) {
            case 1:
                effectToReturn = new SixFoodCompletedSet();
                break;
            case 2:
                effectToReturn = new DiscountFoodOnSustenance(CARD_TYPE.GATHERER);
                break;
            case 3:
                effectToReturn = new DiscountFoodOnSustenance(CARD_TYPE.ARTIST);
                break;
            case 4:
                effectToReturn = new NoPPLostOnShaman();
                break;
            case 5:
                effectToReturn = new PlusOneFoodOnReturnDefaultTile();
                ((PlusOneFoodOnReturnDefaultTile) effectToReturn).setBoardView(boardView);
                break;
            case 6:
                effectToReturn = new FoodOnNewCoupleInventors();
                break;
            case 7:
                effectToReturn = new DoublePPOnShamanEvent();
                break;
            case 8:
                effectToReturn = new ThreeMoreShamanStar();
                break;
            case 9:
                effectToReturn = new DiscountFoodOnSustenance(CARD_TYPE.INVENTOR);
                break;
            case 10:
                effectToReturn = new OnEventHuntOneFoodAndOnePPPerHunter();
                break;
            case 11:
                effectToReturn = new BuilderDoublePP();
                break;
            case 12:
                effectToReturn = new OnEventPaintingsOneFoodPerArtist();
                break;
            case 13:
                effectToReturn = new SetSixCard();
                break;
            case 14:
                effectToReturn = new PPPerCharType(3, CARD_TYPE.HUNTER);
                break;
            case 15:
                effectToReturn = new PPPerCharType(4, CARD_TYPE.GATHERER);
                break;
            case 16:
                effectToReturn = new PPPerCharType(4, CARD_TYPE.SHAMAN);
                break;
            case 17:
                effectToReturn = new PPPerCharType(4, CARD_TYPE.BUILDER);
                break;
            case 18:
                effectToReturn = new PPPerCharType(4, CARD_TYPE.ARTIST);
                break;
            case 19:
                effectToReturn = new PPPerCharType(2, CARD_TYPE.INVENTOR);
                break;
            case 20:
                effectToReturn = new DrawOneMoreCard();
                break;
            case 21:
                effectToReturn = new TwentyFivePPEndGame();
                break;
            default:
                // Unrecognised building ID — this should never happen if the JSON is correct.
                logServerError("Unrecognised building ID: " + buildingToSetEffect.getBuildingID());
                break;
        }
        return effectToReturn;
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
     *
     * @param lowerBound lowerBound
     * @param upperBound upperBound
     * @return Return a shuffled list with numbers between the lower and upper bound
     */
    @Deprecated
    private List<Integer> randomNumerList(int lowerBound, int upperBound) {
        List<Integer> number = new ArrayList<>();
        for (int i = lowerBound; i <= upperBound; i++) {
            number.add(i);
        }
        Collections.shuffle(number);
        return number;
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
