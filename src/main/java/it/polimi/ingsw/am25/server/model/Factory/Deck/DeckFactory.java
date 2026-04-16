package it.polimi.ingsw.am25.server.model.Factory.Deck;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.webLayer.DTOs.CardDTO;
import it.polimi.ingsw.am25.server.model.Factory.DefaultTile.DefaultTileFactory;
import it.polimi.ingsw.am25.server.model.Factory.Event.EventFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class DeckFactory {
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
                System.err.println(getClass() +": Errore numero giocatori");

        }
        if(inputStream==null){
            throw new RuntimeException(getClass() + ": errore apertura file");
        }
        Reader reader = new InputStreamReader(inputStream);
        Gson gson = new Gson();
        CardDTO[] cardDTOS= gson.fromJson(reader, CardDTO[].class);

        for (CardDTO temp: cardDTOS){
            switch (temp.getCardType()){
                case CARD_TYPE.ARTIST :
                    cardToReturn.add(new ArtistCard(temp.getEra(),temp.getCardType()));
                    break;
                case CARD_TYPE.BUILDER:
                    cardToReturn.add(new BuilderCard(temp.getEra(),temp.getCardType(),temp.getFoodDiscount(),temp.getFinalPrestigePoint()));
                    break;
                case CARD_TYPE.GATHERER:
                    cardToReturn.add(new GathererCard(temp.getEra(),temp.getCardType()));
                    break;
                case CARD_TYPE.HUNTER:
                    cardToReturn.add(new HuntersCard(temp.getEra(),temp.getCardType(),temp.isHasIcon()));
                    break;
                case CARD_TYPE.INVENTOR:
                    cardToReturn.add(new InventorCard(temp.getEra(),temp.getCardType(),temp.getInvIcon()));
                    break;
                case CARD_TYPE.SHAMAN:
                    cardToReturn.add(new ShamanCard(temp.getEra(),temp.getCardType(),temp.getStarNumber()));
                    break;
                default:
                    System.err.println(getClass()+ ": Errore creazione carte" + temp.getCardType().toString());
            }
        }

        List<EventCard> listEventToMerge = new EventFactory().createEvent();
        cardToReturn.addAll(listEventToMerge);
        return cardToReturn;

    }
}
