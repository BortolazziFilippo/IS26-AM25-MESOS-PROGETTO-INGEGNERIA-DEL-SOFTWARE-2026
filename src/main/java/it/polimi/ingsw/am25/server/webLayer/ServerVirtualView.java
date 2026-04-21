package it.polimi.ingsw.am25.server.webLayer;

import it.polimi.ingsw.am25.server.model.Board.Action;
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
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.RMI.ClientRemoteInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerVirtualView implements BoardObserver, GameObserver, MarketObserver, PlayerObserver {
    private static final String LOG_PREFIX = "[SERVER][VIEW]";
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
    //Lock for draw one more card
    public final Object extraDrawLock = new Object();


    public ServerVirtualView(ClientRemoteInterface clientStub, String nickname) {
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
            logServerError("Failed to update food for player '" + nickname + "'");
        }
    }

    public void forceInitialPlayersSync(List<PlayerDTO> allPlayers) {
        for (PlayerDTO player : allPlayers) {
            // Keep the local virtual-view cache aligned with the current player snapshot.
            this.playersMap.put(player.getNickName(), player);
            // Push the player snapshot to the client.
            try {
                clientStub.playerAdded(player);
            } catch (Exception e) {
                logServerError("Failed to sync player '" + nickname + "'");
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
        // FIX: Avvolgiamo tutto in new ArrayList<>() per renderli modificabili dal .remove()
        this.topCards = new ArrayList<>(topCards.stream().map(Card::toDTO).toList());
        this.bottomCards = new ArrayList<>(bottomCards.stream().map(Card::toDTO).toList());
        this.topBuildings = new ArrayList<>(topBuildings.stream().map(b -> (BuildingDTO) b.toDTO()).toList());
        try {
            clientStub.initializeMarket(this.topCards, this.bottomCards, this.topBuildings);
        } catch (RemoteException e) {
            System.err.println("Errore di connessione: initializeMarket");
        }

    }

    @Override
    public void onTopCardRefreshed(List<Card> topCards) {
        if (this.topCards != null) {
            this.bottomCards = new ArrayList<>(this.topCards);
        }
        this.topCards = new ArrayList<>(topCards.stream().map(Card::toDTO).toList());
        try {
            clientStub.topCardRefreshed(this.topCards);
        } catch (RemoteException e) {
            System.err.println("Errore di connessione: topCardRefreshed");
        }
    }

    @Override
    public void onBoardChanged(List<OfferTile> offerTileList, List<DefaultTile> defaultTileList) {
        this.offerTileList=offerTileList.stream().map(OffertileDTO::new).toList();
        this.defaultTileList=defaultTileList.stream().map(DefaultTileDTO::new).toList();

        try{
            clientStub.boardInitialize(this.offerTileList,this.defaultTileList);
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to sync board for player '" + nickname + "'");
        }
    }

    @Override
    public void playerToDefaultTile(List<Player> playerOrder) {
        List<PlayerDTO> order=playerOrder.stream().map(PlayerDTO::new).toList();
        try {
            clientStub.orderOnDefaultTile(order);
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to send default tile order for player '" + nickname + "'");
        }
    }

    @Override
    public void playerPlacedOnOffertile(Player player, int tilePosition) {
        try {
            clientStub.playerPlacedOnOffertile(player.getNickname(),tilePosition);
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to notify player placed on offer tile for player '" + nickname + "'");
        }

    }

    @Override
    public void gameWinners(List<Player> winners) {
        this.winners=winners.stream().map(PlayerDTO::new).toList();
        try{
            clientStub.gameWinners(this.winners);
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to notify game winners");
        }
    }

    @Override
    public void onGameChanged(ERA currentEra, List<Player> players, GAME_PHASE gamePhase, Player playerToPlace, Player playerToPlay) {
        this.currentEra=currentEra;
        this.currentGamePhase=gamePhase;
        this.playerToPlace=null;
        this.playerToPlay=null;

        try {
            clientStub.initializeGame(this.currentEra,this.currentGamePhase,null,null);
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to send game initialization for player '" + nickname + "'");
        }
    }

    @Override
    public void onPlayerAdded(Player playerAdded) {
        PlayerDTO player=new PlayerDTO(playerAdded);
        playersMap.put(playerAdded.getNickname(),player);
        try{
            clientStub.playerAdded(player);
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to notify player added for player '" + nickname + "'");
        }

    }

    @Override
    public void onEraChanged(ERA currentEra) {
        this.currentEra=currentEra;
        try {
            clientStub.eraChanged(this.currentEra);
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to notify era change for player '" + nickname + "'");
        }

    }

    @Override
    public void onGamePhaseChanged(GAME_PHASE gamePhase) {
        this.currentGamePhase=gamePhase;
        try {
            clientStub.gamePhaseChanged(currentGamePhase);
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to notify game phase change for player '" + nickname + "'");
        }

    }

    @Override
    public void onPlayerToPlaceChanged(Player newPlayerToPlace) {
        this.playerToPlay=null;
        this.playerToPlace=newPlayerToPlace.getNickname();
        try {
            clientStub.playerToPlaceChanged(new PlayerDTO(newPlayerToPlace));
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to notify player-to-place change for player '" + nickname + "'");
        }
    }

    @Override
    public void onPlayerToPlayChanged(Player newPlayerToPlay) {
        this.playerToPlace=null;
        this.playerToPlay=newPlayerToPlay.getNickname();
        try {
            clientStub.playerToPlayChanged(new PlayerDTO(newPlayerToPlay));
        }catch (java.rmi.RemoteException e) {
            logServerError("Failed to notify player-to-play change for player '" + nickname + "'");
        }

    }

    @Override
    public void onTopBuildingRefreshed(List<BuildingCard> topBuildingCards) {
        if (this.topBuildings != null) {
            this.bottomBuildings = new ArrayList<>(this.topBuildings);
        }

        // FIX: Cast esplicito a BuildingDTO aggiunto!
        this.topBuildings = new ArrayList<>(topBuildingCards.stream().map(b -> (BuildingDTO) b.toDTO()).toList());

        try {
            clientStub.topBuildingRefreshed(this.topBuildings);
        } catch (RemoteException e) {
            System.err.println("Errore di connessione: topBuildingRefreshed");
        }
    }

    @Override
    public void onCardRemovedFromTop(int position, CARD_TYPE cardType) {
        if(cardType==CARD_TYPE.BUILDING){
            this.topBuildings.remove(position);
            try {
                clientStub.topBuildRemoved(position);
            }catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify top building removed at position " + position + " for player '" + nickname + "'");
            }

        }else{
            this.topCards.remove(position);
            try {
                clientStub.topCardRemoved(position);
            }catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify top card removed at position " + position + " for player '" + nickname + "'");
            }
        }

    }

    @Override
    public void onCardRemovedFromBottom(int position, CARD_TYPE cardType) {
        if(cardType==CARD_TYPE.BUILDING){
            this.bottomBuildings.remove(position);
            try {
                clientStub.bottomBuildRemoved(position);
            }catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify bottom building removed at position " + position + " for player '" + nickname + "'");
            }
        }else{
            this.bottomCards.remove(position);
            try {
                clientStub.bottomCardRemoved(position);
            }catch (java.rmi.RemoteException e) {
                logServerError("Failed to notify bottom card removed at position " + position + " for player '" + nickname + "'");
            }
        }
    }

    @Override
    public void notifyCardAddedToTribe(String playername, Card cardAdded) {
        try {
            clientStub.addedCardToTribe(playername,cardAdded.toDTO());
        } catch (RemoteException e) {
            logServerError("Failed to notify card added to tribe for player '" + playername + "'");
        }

    }



    @Override
    public void requestExtraDraw(String nickname) {
        try {
            //ask client
            clientStub.askExtraDraw();
            //wait for answer
            synchronized (extraDrawLock) {
                extraDrawLock.wait();
            }
        } catch (RemoteException e) {
            System.err.println("Errore di connessione con il client " + nickname + " per extra draw.");
            logServerError("Error comunicating draw one more card");
            UtilitiesFunction.logError(LOG_PREFIX,"Error comunicating draw one more card");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logServerError("Error managing thread");
            UtilitiesFunction.logError(LOG_PREFIX,"Error managing thread");
        }
    }

    @Override
    public void actionOfferTileChanged(int drawTop, int drawBottom) {
        try {
            clientStub.actionAvailableChanged(new ActionDTO(drawTop,drawBottom));
        } catch (RemoteException e) {
            logServerError("Error comunicating offertile changed");
            UtilitiesFunction.logError(LOG_PREFIX,"Error comunicating offertile changed");
        }

    }

    private void logServerEvent(String message) {
        System.out.println(LOG_PREFIX + " " + message);
    }

    private void logServerError(String message) {
        System.err.println(LOG_PREFIX + "[ERROR] " + message);
    }
}
