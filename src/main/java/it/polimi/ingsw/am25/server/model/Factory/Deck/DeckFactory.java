package it.polimi.ingsw.am25.server.model.Factory.Deck;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.server.model.Factory.Event.EventFactory;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class DeckFactory {
    private static final String LOG_PREFIX = "[SERVER][DECK_FACTORY]";

    /**
     * Creates a new deck factory instance.
     */
    public DeckFactory(){

    }

    /**
     * This method is used to create the deck of card and event
     * @param playerNumber number of player
     * @return return a List with the right amount of card and event, grouped by type
     */
    public List<Card> createDeck(int playerNumber){
        List<Card> cardToReturn = new ArrayList<>();
        InputStream inputStream=null;
        switch (playerNumber){
            case 2:
                 inputStream= DefaultTileFactory.class.getResourceAsStream("/CardResources/json/TwoPlayersCard.json");
                break;
            case 3:
                 inputStream= DefaultTileFactory.class.getResourceAsStream("/CardResources/json/ThreePlayersCard.json");
                 break;
            case 4:
                inputStream= DefaultTileFactory.class.getResourceAsStream("/CardResources/json/FourPlayersCard.json");
                break;
            case 5:
                inputStream= DefaultTileFactory.class.getResourceAsStream("/CardResources/json/FivePlayersCard.json");
                break;
            default:
                logServerError("Invalid player number: " + playerNumber);

        }
        if(inputStream==null){
            throw new RuntimeException(getClass() + ": errore apertura file");
        }
        Reader reader = new InputStreamReader(inputStream);
        Gson gson = new Gson();
        CardDTO[] cardDTOS= gson.fromJson(reader, CardDTO[].class);

        for (CardDTO temp: cardDTOS){
            switch (temp.getCardType()){
                case ARTIST :
                    cardToReturn.add(new ArtistCard(temp.getEra(),temp.getCardType()));
                    break;
                case BUILDER:
                    cardToReturn.add(new BuilderCard(temp.getEra(),temp.getCardType(),temp.getFoodDiscount(),temp.getFinalPrestigePoint()));
                    break;
                case GATHERER:
                    cardToReturn.add(new GathererCard(temp.getEra(),temp.getCardType()));
                    break;
                case HUNTER:
                    cardToReturn.add(new HuntersCard(temp.getEra(),temp.getCardType(),temp.isHasIcon()));
                    break;
                case INVENTOR:
                    cardToReturn.add(new InventorCard(temp.getEra(),temp.getCardType(),temp.getInvIcon()));
                    break;
                case SHAMAN:
                    cardToReturn.add(new ShamanCard(temp.getEra(),temp.getCardType(),temp.getStarNumber()));
                    break;
                default:
                    logServerError("Unrecognised card type: " + temp.getCardType());
            }
        }

        List<EventCard> listEventToMerge = new EventFactory().createEvent();
        cardToReturn.addAll(listEventToMerge);
        return cardToReturn;

    }

    /**
     * Executes log server error.
     * @param message parameter message.
     */
    private void logServerError(String message) {
        UtilitiesFunction.logError(LOG_PREFIX, message);
    }
}
