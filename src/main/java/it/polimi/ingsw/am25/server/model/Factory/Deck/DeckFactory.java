package it.polimi.ingsw.am25.server.model.Factory.Deck;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Board.Board;
import it.polimi.ingsw.am25.server.model.Board.BoardView;
import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Factory.Building.BuildingFactory;
import it.polimi.ingsw.am25.server.model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Factory.Event.EventFactory;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the Mesos card deck for a given player count by loading card definitions
 * from JSON resources and merging in the era event cards.
 */
public class DeckFactory {
    private static final String LOG_PREFIX = "[SERVER][DECK_FACTORY]";

    /**
     * Creates a new deck factory instance.
     */
    public DeckFactory() {

    }

    /**
     * This method is used to create the deck of card and event
     *
     * @param playerNumber number of player
     * @return return a List with the right amount of card and event, grouped by type
     */
    public List<Card> createDeck(int playerNumber) {
        List<Card> cardToReturn = new ArrayList<>();
        InputStream inputStream = null;
        switch (playerNumber) {
            case 2:
                inputStream = DefaultTileFactory.class.getResourceAsStream("/CardResources/json/TwoPlayersCard.json");
                break;
            case 3:
                inputStream = DefaultTileFactory.class.getResourceAsStream("/CardResources/json/ThreePlayersCard.json");
                break;
            case 4:
                inputStream = DefaultTileFactory.class.getResourceAsStream("/CardResources/json/FourPlayersCard.json");
                break;
            case 5:
                inputStream = DefaultTileFactory.class.getResourceAsStream("/CardResources/json/FivePlayersCard.json");
                break;
            default:
                logServerError("Invalid player number: " + playerNumber);

        }
        if (inputStream == null) {
            throw new RuntimeException(getClass() + ": errore apertura file");
        }
        Reader reader = new InputStreamReader(inputStream);
        Gson gson = new Gson();
        CardDTO[] cardDTOS = gson.fromJson(reader, CardDTO[].class);


        for (CardDTO temp : cardDTOS) {
            cardToReturn.add(cardBinder(temp ));
        }

        List<EventCard> listEventToMerge = new EventFactory().createEvent();
        cardToReturn.addAll(listEventToMerge);
        return cardToReturn;

    }
    private Card cardBinder(CardDTO cardDTO) {
        return switch (cardDTO.getCardType()) {
            case ARTIST   -> new ArtistCard(cardDTO.getEra(), cardDTO.getCardType());
            case BUILDER  -> new BuilderCard(cardDTO.getEra(), cardDTO.getCardType(), cardDTO.getFoodDiscount(), cardDTO.getFinalPrestigePoint(), cardDTO.getBuilderID());
            case GATHERER -> new GathererCard(cardDTO.getEra(), cardDTO.getCardType());
            case HUNTER   -> new HuntersCard(cardDTO.getEra(), cardDTO.getCardType(), cardDTO.isHasIcon());
            case INVENTOR -> new InventorCard(cardDTO.getEra(), cardDTO.getCardType(), cardDTO.getInvIcon());
            case SHAMAN   -> new ShamanCard(cardDTO.getEra(), cardDTO.getCardType(), cardDTO.getStarNumber());
            default -> throw new IllegalArgumentException("Unrecognised card type: " + cardDTO.getCardType());
        };
    }

    /**
     * Rebuilds a list of Cards from their DTO snapshots during game restore.
     * Handles tribe cards and event cards; building cards are restored separately via loadBuildingDeck.
     *
     * @param cards     list of CardDTO snapshots from the memento.
     * @return list of fully initialised Card instances.
     */
    public List<Card> loadDeck(List<CardDTO> cards) {
        EventFactory eventFactory = new EventFactory();
        List<Card> deckToReturn = new ArrayList<>();
        for (CardDTO temp : cards) {
            if (temp.getCardType() == CARD_TYPE.EVENT) {
                deckToReturn.add(eventFactory.createEventById(temp.getEventID()));
            } else {
                deckToReturn.add(cardBinder(temp));
            }
        }
        return deckToReturn;
    }
    /**
     * Rebuilds a list of buildings from their IDs during game restore.
     * Each ID is used to create a fully initialised {@link it.polimi.ingsw.am25.server.model.Card.BuildingCard}
     * via the {@link it.polimi.ingsw.am25.server.model.Factory.Building.BuildingFactory}.
     *
     * @param buildingIds list of building IDs to reconstruct.
     * @param boardView   board view passed to the factory to wire up building effects.
     * @return list of fully initialised {@link it.polimi.ingsw.am25.server.model.Card.BuildingCard} instances.
     */
    public List<BuildingCard> loadBuidlingDeck(List<Integer> buildingIds, BoardView boardView) {
        List<BuildingCard> buidlingDeckToReturn = new ArrayList<>();
        BuildingFactory buildingFactory = new BuildingFactory();
        for (Integer temp : buildingIds) {
            buidlingDeckToReturn.add(buildingFactory.createBuildingById(temp, boardView));
        }
        return buidlingDeckToReturn;
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
