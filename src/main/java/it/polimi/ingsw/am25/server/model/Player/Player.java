package it.polimi.ingsw.am25.server.model.Player;

import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.CONNECTION_STATUS;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotEnoughFoodException;
import it.polimi.ingsw.am25.server.model.Utilities.UtilitiesFunction;
import it.polimi.ingsw.am25.server.webLayer.DTOs.PlayerDTO;
import it.polimi.ingsw.am25.server.webLayer.ServerVirtualView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player {
    private static final String LOG_PREFIX = "[SERVER][PLAYER]";
    private final String nickname;
    private final Totem totem;
    private int food;
    private int prestigePoint;
    private final List<Card> tribe;
    private final List<BuildingCard> buildingCards;
    private CONNECTION_STATUS connectionStatus;
    private final List<PlayerObserver> observers= new ArrayList<>();


    /**
     * Returns nickname.
     * @return the result of the operation.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Builds a player with the provided nickname and totem color.
     *
     * @param nickname player's nickname
     * @param color totem color
     */
    public Player(String nickname, COLOR color) {
            this.nickname = nickname;
            this.food=0;
            this.prestigePoint=0;
            this.tribe = new ArrayList<>();
            this.buildingCards = new ArrayList<>();
            this.totem=new Totem(color);
    }

    /**
     * Builds a player and subscribes the provided virtual view as observer.
     *
     * @param nickname player's nickname
     * @param color totem color
     * @param virtualView observer to bind
     */
    public Player(String nickname, COLOR color, ServerVirtualView virtualView) {
        this.nickname = nickname;
        this.food=0;
        this.prestigePoint=0;
        this.tribe = new ArrayList<>();
        this.buildingCards = new ArrayList<>();
        this.totem=new Totem(color);
        addObserver(virtualView);
        notifyPlayerChanged();
    }

    /**
     * Builds a player from a DTO snapshot.
     *
     * @param playerDTO player data transfer object
     */
    public Player(PlayerDTO playerDTO){
        this.nickname=playerDTO.getNickName();
        this.food=0;
        this.prestigePoint=0;
        this.tribe = new ArrayList<>();
        this.buildingCards = new ArrayList<>();
        this.totem=new Totem(playerDTO.getColorTotem());
    }

    /**
     * Updates the player's food amount.
     * If food would go below zero, the deficit is converted into prestige-point loss
     * at a ratio of 2 PP per missing food, then food is clamped to zero.
     *
     * @param foodAmount delta to apply to food (positive or negative)
     */
    public void manageFoodAndPP(int foodAmount){
        int previousFood = this.food;
        if(foodAmount < 0){
            if( (this.food + foodAmount) < 0){
                this.food += foodAmount;
                int temp = this.food*2;
                managePP(temp);
                this.food = 0;
            }else{
                this.food+=foodAmount;
            }
        }
        else{
            this.food += foodAmount;
        }
        logServerEvent(
                "Updated food for player '" + nickname + "': " + previousFood + " -> " + this.food +
                        " (delta " + foodAmount + ")"
        );
        notifyPPChanged();
        notifyFoodChanged();
    }

    /**
     * Attempts to purchase a building card.
     *
     * @param selectedBuildingCard building to buy
     * @throws NotEnoughFoodException if the player cannot afford the card
     */
    public void tryBuyBuilding( BuildingCard selectedBuildingCard) throws NotEnoughFoodException{
        int originalCost = selectedBuildingCard.getFoodCost();
        int cost = originalCost - this.getBuilderDiscount();
        if(cost<0){
            cost=0;
        }
        if(this.food-cost<0){
            throw new NotEnoughFoodException();
        }else{
            this.food-=cost;
            selectedBuildingCard.addCardToPlayer(this);
            logServerEvent(
                    "Player '" + nickname + "' bought building #" + selectedBuildingCard.getBuildingID() +
                            " (cost " + originalCost + ", discounted to " + cost + ")"
            );
            notifyFoodChanged();
        }

    }

    /**
     * Updates player's prestige points.
     *
     * @param PPamount delta to apply (positive or negative)
     */
    public void managePP(int PPamount){
        int previousPP = this.prestigePoint;
        this.prestigePoint += PPamount;
        logServerEvent(
                "Updated prestige points for player '" + nickname + "': " + previousPP + " -> " + this.prestigePoint +
                        " (delta " + PPamount + ")"
        );
        notifyPPChanged();
    }
    /**
     * Returns the current connection status of the player.
     *
     * @return the player's {@link CONNECTION_STATUS}
     */
    public CONNECTION_STATUS getConnection() {
        return connectionStatus;
    }
    /**
     * Sets the connection status of the player.
     *
     * @param connectionStatus the new {@link CONNECTION_STATUS}
     */
    public void setConnection(CONNECTION_STATUS connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    /**
     * Adds a non-building card to the player's tribe.
     *
     * @param card card to add
     */
    public void addCardToTribe(Card card){
        this.tribe.add(card);
        logServerEvent("Added " + formatCardForLog(card) + " to player '" + nickname + "'");
        notifyCardAdded(card);
    }

    /**
     * Adds a building card to the player's building area.
     *
     * @param buildingCard building card to add
     */
    public void addBuilding(BuildingCard buildingCard){
        this.buildingCards.add(buildingCard);
        logServerEvent("Added building #" + buildingCard.getBuildingID() + " to player '" + nickname + "'");
        notifyCardAdded(buildingCard);
    }
    /**
     * Returns the player's current food amount.
     *
     * @return food amount
     */
    public int getFood() {
        return food;
    }
    /**
     * Returns the player's current prestige-point total.
     *
     * @return prestige points
     */
    public int getPrestigePoint() {
        return prestigePoint;
    }
    /**
     * Returns the number of distinct inventor icons in the player's tribe.
     *
     * @return count of unique {@link INV_ICON} values
     */
    public int getNumberOfDifferentInventorIcon(){
        return (int) tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.INVENTOR)
                .map(InventorCard.class::cast)
                .map(InventorCard::getInvIcon)
                .distinct()
                .count();
    }
    /**
     *
     * @return the number of total shaman star a player has
     */
    public int getShamanStarTotal(){
        int countStar = 0;
        for(Card card : this.tribe){
            if(card.getCardType()== CARD_TYPE.SHAMAN){
                countStar= countStar + ((ShamanCard) card).getStarNumber();
            }
        }
        return countStar;
    }

    /**
     *
     * @return the total discount of the builder a player has
     */
    public int getBuilderDiscount(){

        return this.tribe.stream().
                filter(card -> card.getCardType()==CARD_TYPE.BUILDER).
                map(card -> (BuilderCard)card).
                mapToInt(BuilderCard::getFoodDiscount).
                sum();
    }

    /**
     *
     * @return return the discount given by the gatherer during sustenance event
     */
    public int getGatherDiscount(){

        return  (int) this.tribe.stream().
                filter(card -> card.getCardType()==CARD_TYPE.GATHERER).
                count()*3;
    }

    /**
     *
     * @return return the number of Hunter in tribe
     */
    public int getHunterNumber(){
        return (int) tribe.stream().
                filter(card -> card.getCardType()==CARD_TYPE.HUNTER).
                count();
    }

    /**
     *
     * @return return the number of artists in the tribe
     */
    public int getArtistNumber(){
        return (int) this.tribe.stream()
                .filter(card -> card.getCardType()==CARD_TYPE.ARTIST)
                .count();
    }

    /**
     *
     * @return the total size of the tribe
     */
    public int getNumberOfCard(){
        return this.tribe.size();
    }

    /**
     * Triggers all end-round building effects owned by this player.
     */
    public void triggerEndRoundBuilding(){
        this.buildingCards.stream()
                .filter(buildingCard -> buildingCard.getApplyOn()== EVENT_TYPE.END_ROUND )
                .forEach(buildingCard -> buildingCard.applyBuildingEffect(this));
        notifyPlayerChanged();
    }

    /**
     * Triggers all end-game building effects owned by this player.
     */
    public void triggerEndGameBuilding(){
        this.buildingCards
                .stream()
                .filter(buildingCard -> buildingCard.getApplyOn()==EVENT_TYPE.END_GAME)
                .forEach(buildingCard -> buildingCard.applyBuildingEffect(this));
        notifyPlayerChanged();
    }
    /**
     * Returns an unmodifiable view of all villager cards in the player's tribe.
     *
     * @return the tribe card list
     */
    public List<Card> getTribe() {
        return tribe;
    }

    /**
     * Returns totem.
     * @return the result of the operation.
     */
    public Totem getTotem() {
        return totem;
    }

    /**
     * Returns the list of building cards the player owns.
     *
     * @return building card list
     */

    public List<BuildingCard> getBuildingCards() {
        return buildingCards;
    }

    /**
     * Subscribes an observer.
     *
     * @param observerToAdd observer to subscribe
     */
    public void addObserver(PlayerObserver observerToAdd){
        if(observerToAdd!=null && !observers.contains(observerToAdd)){
            observers.add(observerToAdd);
        }
    }

    /**
     * Returns observers.
     * @return the result of the operation.
     */
    public List<PlayerObserver> getObservers(){
        return this.observers;
    }

    /**
     * Unsubscribes an observer.
     *
     * @param observerToRemove observer to unsubscribe
     */
    public void removeObserver(PlayerObserver observerToRemove){
        observers.remove(observerToRemove);
    }
    /**
     * Notifies all subscribed observers with a snapshot of the current player state.
     */
    public void notifyPlayerChanged(){
        List<Card> tribeSnapshot = List.copyOf(this.tribe);
        List<BuildingCard> buildingSnapshot = List.copyOf(this.buildingCards);
        for (PlayerObserver observer : observers) {
            observer.onPlayerChanged(
                    this.nickname,
                    this.totem,
                    this.food,
                    this.prestigePoint,
                    tribeSnapshot,
                    buildingSnapshot
            );
        }
    }
    /**
     * Executes notify food changed.
     */
    private void notifyFoodChanged(){
        for(PlayerObserver observer:observers){
            observer.notifyFoodChanged(this.nickname,food);
        }
    }

    /**
     * Executes notify ppchanged.
     */
    private void notifyPPChanged(){
        for(PlayerObserver observer:observers){
            observer.notifyPPChanged(this.nickname,prestigePoint);
        }
    }

    /**
     * Executes notify card added.
     * @param cardAdded parameter cardAdded.
     */
    private void notifyCardAdded(Card cardAdded){
        for(PlayerObserver observer:observers){
            observer.notifyCardAddedToTribe(this.nickname,cardAdded);
        }
    }

    /**
     * Executes format card for log.
     * @param card parameter card.
     * @return the result of the operation.
     */
    private String formatCardForLog(Card card) {
        if (card instanceof BuildingCard buildingCard) {
            return "building #" + buildingCard.getBuildingID();
        }
        return card.getCardType() + " card (" + card.getEra() + ")";
    }

    /**
     * Executes log server event.
     * @param message parameter message.
     */
    private void logServerEvent(String message) {
        UtilitiesFunction.logInfo(LOG_PREFIX, message);
    }

    /**
     * Executes equals.
     * @param o parameter o.
     * @return the result of the operation.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Player player)) return false;
        return Objects.equals(nickname, player.nickname) && totem.equals(player.totem);
    }

    /**
     * method for building Card draw one more card
     */
    public void requestExtraDraw() {
        for (PlayerObserver observer : observers) {
            observer.requestExtraDraw(this.nickname);
        }
    }
    /**
     * Executes checkpoints.
     * @return the result of the operation.
     */
    public int checkpoints(){
        int finalPoints = 0;
        long artistPoints = 0;
        int builderPoints = 0;
        long inventorPoints = 0;
        artistPoints = getArtistNumber();
        finalPoints = (int)(artistPoints/2)*10;

        builderPoints = this.tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.BUILDER)
                .mapToInt(card -> ((BuilderCard) card).getFinalPrestigePoint())
                .sum();

        inventorPoints = this.tribe.stream()
                .filter(card -> card.getCardType() == CARD_TYPE.INVENTOR)
                .count();
        inventorPoints = inventorPoints*getNumberOfDifferentInventorIcon();
        finalPoints = finalPoints + (int)inventorPoints + builderPoints;
        return finalPoints;
    }
}
