package it.polimi.ingsw.am25.server.webLayer;

import it.polimi.ingsw.am25.server.model.Board.DefaultTile;
import it.polimi.ingsw.am25.server.model.Board.OfferTile;
import it.polimi.ingsw.am25.server.model.Card.BuildingCard;
import it.polimi.ingsw.am25.server.model.Card.Card;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.ERA;
import it.polimi.ingsw.am25.server.model.Enums.GAME_PHASE;
import it.polimi.ingsw.am25.server.model.Observers.BoardObserver;
import it.polimi.ingsw.am25.server.model.Observers.GameObserver;
import it.polimi.ingsw.am25.server.model.Observers.MarketObserver;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Player.Player;
import it.polimi.ingsw.am25.server.model.Player.Totem;
import it.polimi.ingsw.am25.server.webLayer.DTOs.*;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VirtualView implements BoardObserver, GameObserver, MarketObserver, PlayerObserver {
    private final String nickname;
    private final ClientRemoteInterface clientStub;
    //_________________________________________________________________________________________
    private List<PlayerDTO> winners;
    private ERA currentEra;
    private GAME_PHASE currentGamePhase;
    private String playerToPlace;
    private String playerToPlay;
    //_________________________________________________________________________________________
    Map<String,PlayerDTO> playersMap= new HashMap<>();
    //_________________________________________________________________________________________
    //MARKET DTO
    private List<CardDTO> topCards;
    private List<CardDTO> bottomCards;
    private List<BuildingDTO> topBuildings;
    private List<BuildingDTO> bottomBuildings;
    //_________________________________________________________________________________________
    //BOARD DTO
    private List<OffertileDTO> offerTileList;
    private List<DefaultTileDTO> defaultTileList;
    //_________________________________________________________________________________________


    public VirtualView(ClientRemoteInterface clientStub,String nickname) {
        this.clientStub=clientStub;
        this.nickname=nickname;
    }

    @Override
    public void notifyPPChanged(String nickname,int newPP) {
        PlayerDTO pl=playersMap.get(nickname);
        pl.setPrestigePoint(newPP);
        playersMap.put(nickname,pl);
        try {
            clientStub.playerUpdatePP(nickname,newPP);
        }catch (java.rmi.RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void notifyFoodChanged(String nickname,int newFood) {
        PlayerDTO pl=playersMap.get(nickname);
        pl.setFood(newFood);
        playersMap.put(nickname,pl);
        try{
            clientStub.playerUpdateFood(nickname,newFood);
        }catch (java.rmi.RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void forceInitialPlayersSync(List<PlayerDTO> allPlayers) {
        for (PlayerDTO player : allPlayers) {
            // 1. Popoliamo la mappa locale della Virtual View
            this.playersMap.put(player.getNickName(), player);
            // 2. Usiamo il telecomando per inviare il giocatore al Client!
            try {
                clientStub.playerAdded(player);
            } catch (Exception e) {
                System.err.println("Errore di rete durante la sincronizzazione iniziale con " + nickname);
            }
        }
    }

    @Override
    @Deprecated
    public void onPlayerChanged(String nickname, Totem totem, int food, int prestigePoint, List<Card> tribe, List<BuildingCard> buildingCards) {
        playersMap.put(nickname,new PlayerDTO(nickname,food,prestigePoint,totem.getColor()));
    }

    @Override
    public void onMarketChanged(List<Card> topCards, List<Card> bottomCards, List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {
        this.topCards=topCards.stream().map(Card::toDTO).toList();
        this.bottomCards=bottomCards.stream().map(Card::toDTO).toList();
        this.topBuildings=topBuildings.stream().map(BuildingDTO::new).toList();
        this.bottomBuildings=new ArrayList<>();

    }

    @Override
    public void onTopCardRefreshed(List<Card> topCards) {
        //when this method is called the top card had been moved down and replaced with new ones
        bottomCards=List.copyOf(this.topCards);
        this.topCards=topCards.stream().map(Card::toDTO).toList();

    }

    @Override
    public void onBoardChanged(List<OfferTile> offerTileList, List<DefaultTile> defaultTileList) {
        this.offerTileList=offerTileList.stream().map(OffertileDTO::new).toList();
        this.defaultTileList=defaultTileList.stream().map(DefaultTileDTO::new).toList();
    }

    @Override
    public void playerToDefaultTile(List<Player> playerOrder) {
        //TODO:notificare ordine di gioco
    }

    @Override
    public void playerPlacedOnOffertile(Player player, int tilePosition) {
    //TODO:notificare giocatore piazzato
    }

    @Override
    public void gameWinners(List<Player> winners) {
        this.winners=winners.stream().map(PlayerDTO::new).toList();
    }

    @Override
    public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase, Player playerToPlace, Player playerToPlay) {
        this.currentEra=currentEra;
        this.currentGamePhase=gamePhase;
        this.playerToPlace=null;
        this.playerToPlay=null;

    }

    @Override
    public void onPlayerAdded(Player playerAdded) {
        playersMap.put(playerAdded.getNickname(),new PlayerDTO(playerAdded));
    }

    @Override
    public void onEraChanged(ERA currentEra) {
        this.currentEra=currentEra;

    }

    @Override
    public void onGamePhaseChanged(GAME_PHASE gamePhase) {
        this.currentGamePhase=gamePhase;
    }

    @Override
    public void onPlayerToPlaceChanged(Player newPlayerToPlace) {
        this.playerToPlay=null;
        this.playerToPlace=newPlayerToPlace.getNickname();
    }

    @Override
    public void onPlayerToPlayChanged(Player newPlayerToPlay) {
        this.playerToPlace=null;
        this.playerToPlay=newPlayerToPlay.getNickname();

    }

    @Override
    public void onTopBuildingRefreshed(List<BuildingCard> topCards) {
        this.bottomBuildings=List.copyOf(this.topBuildings);
        this.topBuildings=topCards.stream().map(BuildingDTO::new).toList();

    }

    @Override
    public void onCardRemovedFromTop(int position, CARD_TYPE cardType) {
        if(cardType==CARD_TYPE.BUILDING){
            this.topBuildings.remove(position);
            //TODO:notifica e chiamata a funzione
        }else{
            this.topCards.remove(position);
        }

    }

    @Override
    public void onCardRemovedFromBottom(int position, CARD_TYPE cardType) {
        if(cardType==CARD_TYPE.BUILDING){
            this.bottomBuildings.remove(position);
            //TODO:notifica e chiamata a funzione
        }else{
            this.bottomCards.remove(position);
        }
    }
}
