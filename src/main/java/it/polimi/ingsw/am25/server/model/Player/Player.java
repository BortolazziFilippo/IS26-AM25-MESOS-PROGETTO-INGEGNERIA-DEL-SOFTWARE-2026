package it.polimi.ingsw.am25.server.model.Player;

import it.polimi.ingsw.am25.server.Card.*;
import it.polimi.ingsw.am25.server.model.Card.*;
import it.polimi.ingsw.am25.server.model.Enums.CARD_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.COLOR;
import it.polimi.ingsw.am25.server.model.Enums.CONNECTION_STATUS;
import it.polimi.ingsw.am25.server.model.Enums.EVENT_TYPE;
import it.polimi.ingsw.am25.server.model.Enums.INV_ICON;
import it.polimi.ingsw.am25.server.model.Observers.PlayerObserver;
import it.polimi.ingsw.am25.server.model.Utilities.Exception.NotEnoughFoodException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player {
    private final String nickname;
    private final Totem totem;
    private int food;
    private int prestigePoint;
    private final List<Card> tribe;
    private final List<BuildingCard> buildingCards;
    private CONNECTION_STATUS connectionStatus;
    private final List<PlayerObserver> observers= new ArrayList<>();


    public String getNickname() {
        return nickname;
    }

    /**
     * default constructor of player
     * @param nickname name of the player
     * @param color color of the totem
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
     * method to manage the player's food amount. Mainly to be used when returning to the default tile
     * since if the player doesn't have enough food it automatically removes two PP per food below zero and then set food to 0.
     * This second behavior sometimes could not be wanted, if so before calling you should check the amount of food available.
     * @param foodAmount food to be removed
     */
    public void manageFoodAndPP(int foodAmount){
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
        notifyPlayerChanged();//here it notifies the changes
    }

    /**
     * this method tries to buy the card, if the player cannot afford it, it throws not enough food exception
     * @param selectedBuildingCard building to be bought
     */
    public void tryBuyBuilding( BuildingCard selectedBuildingCard) throws NotEnoughFoodException{
        int cost;
        cost=selectedBuildingCard.getFoodCost();
        cost=cost-this.getBuilderDiscount();
        if(cost<0){
            cost=0;
        }
        if(this.food-cost<0){
            throw new NotEnoughFoodException();
        }else{
            this.food-=cost;
            selectedBuildingCard.addCardToPlayer(this);
            notifyPlayerChanged();//here it notifies the changes
        }

    }

    /**
     * Method used to manage player's PP. It adds or subtracts the amount
     * @param PPamount amount to be managed
     */
    public void managePP(int PPamount){
        this.prestigePoint += PPamount;
        notifyPlayerChanged();//here it notifies the changes
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
     * method used to add a villager card to the tribe
     * @param card card to be added
     */
    public void addCardToTribe(Card card){
        this.tribe.add(card);
        notifyPlayerChanged();//here it notifies the changes
    }

    /**
     * Method to add a building card to the list of buildings
     * @param buildingCard building to be added
     */
    public void addBuilding(BuildingCard buildingCard){
        this.buildingCards.add(buildingCard);
        notifyPlayerChanged();//here it notifies the changes
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
                .map(InventorCard.class::cast)    // 1. Cast each Card to InventorCard
                .map(InventorCard::getInvIcon)    // 2. Extract the inventor icon
                .distinct()                       // 3. Keep only distinct icons
                .count();                         // 4. Count how many remain
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
     * this method trigger the end round buildings and apply their effect
     */
    public void triggerEndRoundBuilding(){
        this.buildingCards.stream()
                .filter(buildingCard -> buildingCard.getApplyOn()== EVENT_TYPE.END_ROUND )
                .forEach(buildingCard -> buildingCard.applyBuildingEffect(this));
        notifyPlayerChanged();//here it notifies the changes
    }

    /**
     * this method trigger the end round buildings and apply their effect
     */
    public void triggerEndGameBuilding(){
        this.buildingCards
                .stream()
                .filter(buildingCard -> buildingCard.getApplyOn()==EVENT_TYPE.END_GAME)
                .forEach(buildingCard -> buildingCard.applyBuildingEffect(this));
        notifyPlayerChanged();//here it notifies the changes
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
     * Returns the list of building cards the player owns.
     *
     * @return building card list
     */
    public List<BuildingCard> getBuildingCards() {
        return buildingCards;
    }

    /**
     * thi method subscribe an observer
     * @param observerToAdd observer to subscribe
     */
    public void addObserver(PlayerObserver observerToAdd){
        if(observerToAdd!=null && !observers.contains(observerToAdd)){
            observers.add(observerToAdd);
        }
    }

    /**
     * this method unsubscribe an observer
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Player player)) return false;
        return Objects.equals(nickname, player.nickname) && totem.equals(player.totem);
    }

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
