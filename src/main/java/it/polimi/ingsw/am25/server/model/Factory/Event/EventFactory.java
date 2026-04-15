package it.polimi.ingsw.am25.server.model.Factory.Event;

import com.google.gson.Gson;
import it.polimi.ingsw.am25.server.model.Card.EventCard;
import it.polimi.ingsw.am25.server.Effect.Event.*;
import it.polimi.ingsw.am25.server.model.Effect.Event.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Factory.DTO.EventDTO;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class EventFactory {
    public EventFactory() {
    }

    /**
     *
     * @return a list with the event ordered by ERA
     */
    public List<EventCard> createEvent(){
        List<EventCard> templist=new ArrayList<>();
        List<EventCard> listToReturn=new ArrayList<>();
        InputStream inputStream = EventFactory.class.getResourceAsStream("/CardResources/json/event.json");
        if(inputStream==null) {
            throw new RuntimeException(getClass()+ ": Errore apertura file building.json");
        }
        Reader reader= new InputStreamReader(inputStream);
        Gson gson= new Gson();
        EventDTO[] eventDTOS = gson.fromJson(reader,EventDTO[].class);
        for(EventDTO event: eventDTOS ){
            templist.add(new EventCard(event.getEra(), CARD_TYPE.EVENT,event.getEventID(),event.getEventType()));
        }
        for (EventCard temp:templist){
            temp.setEventEffect(eventBinder(temp));
            listToReturn.add(temp);
        }
        return listToReturn;
    }
    private EventEffect eventBinder(EventCard eventCard){
        EventEffect eventEffect=null;
        switch (eventCard.getEventID()){
            case 1:
                eventEffect=new HuntEvent(1,1);
                break;
            case 2:
                eventEffect=new SustenanceEvent(1,1);
                break;
            case 3:
                eventEffect=new ShamanEvent(5,-3);
                break;
            case  4:
                eventEffect=new ArtistEvent(1,-2,1);
                break;
            case 5:
                eventEffect=new HuntEvent(1,2);
                break;
            case 6:
                eventEffect=new SustenanceEvent(1,2);
                break;
            case 7:
                eventEffect=new ShamanEvent(10,-5);
                break;
            case  8:
                eventEffect=new ArtistEvent(2,-2,2);
                break;
            case 9:
                eventEffect=new HuntEvent(1,3);
                break;
            case 10:
                eventEffect=new ArtistEvent(3,-2,3);
                break;
            case 11:
                eventEffect=new SustenanceEvent(1,3);
                break;
            case  12:
                eventEffect=new ShamanEvent(15,-7);
                break;
            default:
                System.err.println(getClass()+"Errore associazione eventi");
        }
        return eventEffect ;
    }
}